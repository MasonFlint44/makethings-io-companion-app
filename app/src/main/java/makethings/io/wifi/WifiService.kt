package makethings.io.wifi

import android.content.*
import android.net.*
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn

class WifiService(appContext: Context) : ContextWrapper(appContext) {
    private val tag = "WifiService"
    private val wifiManager = appContext.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    private val connectivityManager = appContext.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

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

    @RequiresApi(Build.VERSION_CODES.Q)
    fun requestNetwork(
        ssid: String? = null,
        bssid: MacAddress? = null,
        password: String? = null,
        security: WifiSecurity? = null,
        isLocal: Boolean = true
    ): NetworkCallbacks {
        val network = buildNetworkSpecifier(ssid, bssid, password, security)
        val request = buildNetworkRequest(network, NetworkCapabilities.TRANSPORT_WIFI, isLocal)
        val callbacks = NetworkCallbacks()
        connectivityManager.requestNetwork(request, callbacks.networkCallback)
        return callbacks
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun buildNetworkRequest(
        networkSpecifier: WifiNetworkSpecifier,
        transportType: Int? = null,
        hasInternet: Boolean? = null
    ): NetworkRequest {
        val builder = NetworkRequest.Builder()
        builder.setNetworkSpecifier(networkSpecifier)
        if (transportType != null) {
            builder.addTransportType(transportType)
        }
        when(hasInternet) {
            true -> builder.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            false -> builder.removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        }
        return builder.build()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun buildNetworkSpecifier(
        ssid: String? = null,
        bssid: MacAddress? = null,
        password: String? = null,
        security: WifiSecurity? = null
    ): WifiNetworkSpecifier {
        val builder = WifiNetworkSpecifier.Builder()
        if (ssid != null) {
            builder.setSsid(ssid)
        }
        if (bssid != null) {
            builder.setBssid(bssid)
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