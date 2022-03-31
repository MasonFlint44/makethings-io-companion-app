package things.dev.features.wifi

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.net.*
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LifecycleCoroutineScope
import dagger.hilt.android.qualifiers.ActivityContext
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import things.dev.features.wifi.framework.models.WifiScanResult
import things.dev.features.wifi.framework.models.WifiSecurity
import javax.inject.Inject

class WifiService @Inject constructor(@ActivityContext context: Context, private val lifecycleScope: LifecycleCoroutineScope) : ContextWrapper(context) {
    private val tag = "WifiService"
    private val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    private val connectivityManager = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val disabledNetworkBssids = mutableListOf<String>()
    private var requestedNetworkId: Int? = null

    @ExperimentalCoroutinesApi
    fun startScan(): Flow<List<WifiScanResult>> = callbackFlow {
        val scanResultsReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                Log.d(tag,"Completed wifi network scan")
                val scanResults = wifiManager.scanResults.map { result ->
                    WifiScanResult(result.SSID, result.BSSID, result.frequency, result.level, result.capabilities)
                }
                offer(scanResults)
            }
        }

        Log.d(tag,"Starting wifi network scan")
        registerReceiver(scanResultsReceiver, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
        wifiManager.startScan()

        awaitClose { unregisterReceiver(scanResultsReceiver) }
    }.flowOn(Dispatchers.IO)

    // TODO: need to wifiManger.setWifiEnabled() when app is running if wifiManager.isWifiEnabled() == False
    // TODO: move location permissions request to WifiService
    fun requestNetwork(
        ssid: String? = null,
        bssid: String? = null,
        password: String? = null,
        security: WifiSecurity? = null,
        isLocal: Boolean = false
    ): NetworkCallbacks {
        val network = buildNetworkSpecifier(ssid, bssid, password, security)
        val request = buildNetworkRequest(network, !isLocal)
        val callbacks = NetworkCallbacks(lifecycleScope, this)
        connectivityManager.requestNetwork(request, callbacks.networkCallback)
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            if (isLocal) {
                callbacks.available
                    .take(1)
                    .map { it.network }
                    .onEach {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            connectivityManager.bindProcessToNetwork(it)
                        } else {
                            ConnectivityManager.setProcessDefaultNetwork(it)
                        }
                    }
                    .launchIn(lifecycleScope)
            }
            requestNetworkForPAndBelow(ssid, bssid, password, security, !isLocal)
        }
        return callbacks
    }

    fun unregisterNetworkCallbacks(callbacks: NetworkCallbacks) {
        connectivityManager.unregisterNetworkCallback(callbacks.networkCallback)
    }

    fun disconnect() {
        wifiManager.disconnect()
    }

    fun restoreDisabledNetworks() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_DENIED) {
                return
        }
        for (config in wifiManager.configuredNetworks) {
            if (!disabledNetworkBssids.contains(config.BSSID)) {
                continue
            }
            wifiManager.enableNetwork(config.networkId, false)
        }
        disabledNetworkBssids.clear()
    }

    fun disableRequestedNetwork() {
        try {
            wifiManager.disableNetwork(requestedNetworkId!!)
        } catch (npe: NullPointerException) {
            // do nothing
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun getNetworkCapabilitiesSdk30AndBelow(): NetworkCapabilities? {
        val network = connectivityManager.activeNetwork
        return connectivityManager.getNetworkCapabilities(network)
    }

    @ExperimentalCoroutinesApi
    @RequiresApi(Build.VERSION_CODES.N)
    private suspend fun getNetworkCapabilitiesSdk31AndUp(): NetworkCapabilities {
        val callbacks = NetworkCallbacks(
            lifecycleScope,
            this,
            ConnectivityManager.NetworkCallback.FLAG_INCLUDE_LOCATION_INFO
        )
        connectivityManager.registerDefaultNetworkCallback(callbacks.networkCallback)

        return callbacks.capabilitiesChanged
            .map { it.networkCapabilities }
            .first()
    }

    suspend fun getCurrentNetworkInfo(): WifiScanResult? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return WifiScanResult(
                wifiManager.connectionInfo.ssid,
                wifiManager.connectionInfo.bssid,
                wifiManager.connectionInfo.frequency,
                wifiManager.connectionInfo.rssi
            )
        }

        val capabilities = if (Build.VERSION.SDK_INT < 31) getNetworkCapabilitiesSdk30AndBelow() else coroutineScope { getNetworkCapabilitiesSdk31AndUp() }

        val wifiInfo = capabilities?.transportInfo
        if (wifiInfo !is WifiInfo) {
            return null
        }
        return WifiScanResult(wifiInfo.ssid,
            wifiInfo.bssid,
            wifiInfo.frequency,
            wifiInfo.rssi
        )
//        TODO: In API > 31, use WifiInfo#getSecurityType() to set security on WifiScanResult
//        if (Build.VERSION.SDK_INT > 31) {
//            val securityType = wifiInfo.getCurrentSecurityType()
//        }
    }

    private fun requestNetworkForPAndBelow(
        ssid: String? = null,
        bssid: String? = null,
        password: String? = null,
        security: WifiSecurity? = null,
        hasInternet: Boolean = true
    ) {
        val config = WifiConfiguration()
        if (ssid != null) {
            config.SSID = "\"$ssid\""
        }
        if (bssid != null) {
            config.BSSID = bssid
        }
        if (password != null) {
            when(security) {
                WifiSecurity.WEP -> {
                    config.wepKeys[0] = "\"$password\""
                    config.wepTxKeyIndex = 0
                    config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
                    config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40)
                }
                WifiSecurity.WPA,
                WifiSecurity.WPA2,
                WifiSecurity.WPA3 -> config.preSharedKey = "\"$password\""
                else -> {/* do nothing */}
            }
        }
        if (security == WifiSecurity.OPEN) {
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
        }
        val networkId = wifiManager.addNetwork(config)
        requestedNetworkId = networkId
        wifiManager.disconnect()

        if (!hasInternet &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED) {

            for (config in wifiManager.configuredNetworks) {
                if (config.status == WifiConfiguration.Status.DISABLED) { continue }
                wifiManager.disableNetwork(config.networkId)
                if (config.BSSID == null) { continue }
                disabledNetworkBssids.add(config.BSSID)
            }
        }

        wifiManager.enableNetwork(networkId, true)
        wifiManager.reconnect()
    }

    private fun buildNetworkRequest(
        networkSpecifier: WifiNetworkSpecifier? = null,
        hasInternet: Boolean? = null
    ): NetworkRequest {
        val builder = NetworkRequest.Builder()
        builder.addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && networkSpecifier != null) {
            builder.setNetworkSpecifier(networkSpecifier)
        }
        when(hasInternet) {
            true -> builder.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            false -> builder.removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        }
        return builder.build()
    }

    private fun buildNetworkSpecifier(
        ssid: String? = null,
        bssid: String? = null,
        password: String? = null,
        security: WifiSecurity? = null
    ): WifiNetworkSpecifier? {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            return null
        }
        val builder = WifiNetworkSpecifier.Builder()
        if (ssid != null) {
            builder.setSsid(ssid)
        }
        if (bssid != null) {
            builder.setBssid(MacAddress.fromString(bssid))
        }
        if (password != null) {
            when(security) {
                WifiSecurity.WPA2 -> builder.setWpa2Passphrase(password)
                WifiSecurity.WPA3 -> builder.setWpa3Passphrase(password)
                else -> {/* do nothing */}
            }
        }
        return builder.build()
    }
}