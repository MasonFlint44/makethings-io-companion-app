package things.dev.features.mainpage.view

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.*
import androidx.lifecycle.lifecycleScope
import com.amplifyframework.AmplifyException
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import com.amplifyframework.core.Amplify
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import things.dev.features.devicescan.view.DeviceScanViewModel
import things.dev.features.devicescan.view.ScanResultLoading
import things.dev.features.wifilogin.view.WifiLoginViewModel
import things.dev.features.wifiscan.view.WifiScanViewModel
import things.dev.features.wifi.NetworkCallbacks
import things.dev.features.wifi.domain.models.WifiScanResult
import things.dev.features.wifi.WifiService
import mqtt.broker.Broker
import mqtt.packets.Qos
import mqtt.packets.mqttv5.MQTT5Properties
import things.dev.R

data class WifiCredentials(val ssid: String, val password: String)

class MainActivity : AppCompatActivity() {
    private val tag = "MainActivity"
    private val deviceScanViewModel: DeviceScanViewModel by viewModels()
    private val wifiScanViewModel: WifiScanViewModel by viewModels()
    private val wifiLoginViewModel: WifiLoginViewModel by viewModels()
    private val viewModel: MainActivityViewModel by viewModels()
    private var broker: Broker? = null
    private lateinit var wifiService: WifiService
    private lateinit var floatingActionButton: FloatingActionButton
    private lateinit var bottomAppBar: BottomAppBar
    private lateinit var wizardPagerAdapter: WizardPagerAdapter
    private var requestedNetworkCallbacks: NetworkCallbacks? = null
//    private var originalNetworkInfo: WifiScanResult? = null
    private var connectedDevice: WifiScanResult? = null
    private var connectedNetwork: WifiScanResult? = null
    private val gson = Gson()

//    @ExperimentalCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        wifiService = WifiService(this, lifecycleScope)

        wizardPagerAdapter = WizardPagerAdapter(this)
        viewModel.pageIndex.observe(this) {
            wizardPagerAdapter.pageIndex = it
        }

        floatingActionButton = findViewById(R.id.floatingActionButton)
        floatingActionButton.setOnClickListener {
            onFabClicked(it)
        }
        viewModel.fabEnabled.observe(this) {
            floatingActionButton.isEnabled = it
        }

        bottomAppBar = findViewById(R.id.bottomAppBar)

        deviceScanViewModel.deviceSelected.observe(this) {
            viewModel.fabIcon.value = FabIcon.WIFI
            viewModel.fabAlignment.value = FabAlignmentMode.CENTER
            viewModel.fabEnabled.value = true
        }

        wifiScanViewModel.scanResultSelected.observe(this) {
            viewModel.fabEnabled.value = true
            wifiLoginViewModel.scanResult.value = it
        }

        wifiLoginViewModel.password.observe(this) {
            viewModel.fabEnabled.value = it.isNotEmpty()
        }
        wifiLoginViewModel.password.observe(this) {
            viewModel.fabEnabled.value = it.isNotEmpty()
        }

        viewModel.scanResults.observe(this) {
            deviceScanViewModel.scanResults.value = it
            wifiScanViewModel.scanResults.value = it
        }
        viewModel.loading.observe(this) {
            deviceScanViewModel.loading.value = it
            wifiScanViewModel.loading.value = it
        }
        viewModel.fabAlignment.observe(this) {
            when (it) {
                FabAlignmentMode.CENTER -> bottomAppBar.fabAlignmentMode =
                    BottomAppBar.FAB_ALIGNMENT_MODE_CENTER
                FabAlignmentMode.END -> bottomAppBar.fabAlignmentMode =
                    BottomAppBar.FAB_ALIGNMENT_MODE_END
            }
        }
        viewModel.fabIcon.observe(this) {
            when (it) {
                FabIcon.NEXT -> floatingActionButton.setImageResource(R.drawable.ic_baseline_arrow_forward_24)
                FabIcon.WIFI -> floatingActionButton.setImageResource(R.drawable.twotone_signal_wifi_4_bar_24)
            }
        }

        try {
            Amplify.addPlugin(AWSCognitoAuthPlugin())
            Amplify.configure(applicationContext)
            Log.i(tag, "Initialized Amplify")
        } catch (error: AmplifyException) {
            Log.e(tag, "Could not initialize Amplify", error)
        }

        Amplify.Auth.fetchAuthSession(
            { Log.i("AmplifyQuickstart", "Auth session = $it") },
            { Log.e("AmplifyQuickstart", "Failed to fetch auth session", it) }
        )

        ensurePermission(Manifest.permission.ACCESS_FINE_LOCATION)
//        originalNetworkInfo = wifiService.getCurrentNetworkInfo()
    }

    override fun onResume() {
        super.onResume()

        if (viewModel.scanResults.value.isNullOrEmpty()) {
            startWifiScan()
        }

//        lifecycleScope.launch(Dispatchers.IO) {
//            val currentNetwork = wifiService.getCurrentNetworkInfo()
//            if (currentNetwork != null && currentNetwork.bssid != connectedDevice?.bssid) {
//                connectedDevice?.let {
//                    launch(Dispatchers.Main) {
//                        deviceScanViewModel.deviceSelected.value?.let { selectedDevice ->
//                            deviceScanViewModel.scanResultLoading.value = ScanResultLoading(selectedDevice.position, true)
//                        }
//                        connectToDevice(it) {
//                            deviceScanViewModel.deviceSelected.value?.let { selectedDevice ->
//                                deviceScanViewModel.scanResultLoading.value = ScanResultLoading(selectedDevice.position, false)
//                            }
////                broker = startBroker()
//                            // TODO: enable bottom button
//                        }
//                    }
//                }
//            }
//        }

        broker = startBroker()
    }

    override fun onPause() {
        super.onPause()
        broker?.let { stopBroker(it) }
//        wifiService.restoreDisabledNetworks()
        // TODO: disconnect from requested network
//        originalNetworkInfo?.let {
//            wifiService.requestNetwork(it.ssid, it.bssid)
//        }
    }

    override fun onStop() {
        super.onStop()
        requestedNetworkCallbacks?.unregister()
    }

    override fun onBackPressed() {
        if (viewModel.pageIndex.value == 0) {
            super.onBackPressed()
            return
        }
        prevPage()
        viewModel.fabEnabled.value = true
    }

//    override fun onSaveInstanceState(outState: Bundle) {
//        super.onSaveInstanceState(outState)
//        outState.putParcelableArrayList("scanResults", ArrayList(scanResults))
//    }
//
//    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
//        super.onRestoreInstanceState(savedInstanceState)
//        val scanResults = savedInstanceState.getParcelableArrayList<WifiScanResult>("scanResults")
//        if (scanResults != null) {
//            updateScanResults(scanResults.toList())
//        }
//    }

    private fun nextPage() {
        if (viewModel.pageIndex.value == wizardPagerAdapter.itemCount - 1) { return }
        viewModel.pageIndex.value = viewModel.pageIndex.value?.plus(1)
    }

    private fun prevPage() {
        if (viewModel.pageIndex.value == 0) { return }
        viewModel.pageIndex.value = viewModel.pageIndex.value?.minus(1)
    }

    private fun onFabClicked(view: View) {
        viewModel.fabEnabled.value = false
//        nextPage()

        when(viewModel.pageIndex.value) {
            0 -> {
                if (connectedDevice == null) {
                    deviceScanViewModel.deviceSelected.value?.let {
                        connectToDevice(it.scanResult) {
                            deviceScanViewModel.scanResultLoading.value =
                                ScanResultLoading(it.position, false)
                            viewModel.fabEnabled.value = true
                            viewModel.fabAlignment.value = FabAlignmentMode.END
                            viewModel.fabIcon.value = FabIcon.NEXT
                        }
                        deviceScanViewModel.scanResultLoading.value = ScanResultLoading(it.position, true)
                    }
                } else {
                    nextPage()
                }
            }
            1 -> {
                viewModel.fabIcon.value = FabIcon.WIFI
                viewModel.fabAlignment.value = FabAlignmentMode.CENTER
                nextPage()
            }
            2 -> {
                if (connectedNetwork == null) {
                    wifiLoginViewModel.loading.value = true
                    wifiLoginViewModel.scanResult.value?.let {
                        wifiLoginViewModel.password.value?.let { password ->
                            publishMessage(gson.toJson(WifiCredentials(it.ssid, password)), "wifi-credentials")
                            connectToNetwork(it, password) {
                                wifiLoginViewModel.loading.value = false
                                viewModel.fabEnabled.value = true
                                viewModel.fabAlignment.value = FabAlignmentMode.END
                                viewModel.fabIcon.value = FabIcon.NEXT
                            }
                        }
                    }
                } else {
                    nextPage()
                }
            }
        }
    }

    private fun connectToDevice(device: WifiScanResult, onAvailable: () -> Unit) {
        var callbacks = wifiService.requestNetwork(
            device.ssid,
            security = device.security,
            isLocal = true
        )
        requestedNetworkCallbacks = callbacks
        callbacks.available
            .take(1)
            .map { it.network }
            .onEach {
                connectedDevice = device
                onAvailable()
            }
            .launchIn(lifecycleScope)
    }

    private fun connectToNetwork(network: WifiScanResult, password: String, onAvailable: () -> Unit) {
        var callbacks = wifiService.requestNetwork(
            network.ssid,
            password = password,
            security = network.security,
            isLocal = true
        )
        requestedNetworkCallbacks = callbacks
        callbacks.available
            .take(1)
            .map { it.network }
            .onEach {
                connectedNetwork = network
                onAvailable()
            }
            .launchIn(lifecycleScope)
    }

    @ExperimentalCoroutinesApi
    private fun startWifiScan() {
        viewModel.scanResults.value = emptyList()
        viewModel.loading.value = true

        wifiService.startScan()
            .take(1)
            .onEach {
                viewModel.loading.value = false
                viewModel.scanResults.value = it
            }
            .launchIn(lifecycleScope)
    }

    private fun startBroker(): Broker {
        val broker = Broker()
        lifecycleScope.launch(Dispatchers.IO) {
            Log.d(tag, "Starting MQTT broker...")
            broker.listen()
        }
        return broker
    }

    private fun publishMessage(payload: String, topic: String, retain: Boolean = false) {
        broker?.publish(retain, topic, Qos.AT_LEAST_ONCE, MQTT5Properties(), payload.toByteArray().toUByteArray())
    }

    private fun stopBroker(broker: Broker) {
        broker.stop()
        Log.d(tag, "Stopping MQTT broker")
    }

    private fun ensurePermission(permission: String) {
        if (ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            return
        }
        ActivityCompat.requestPermissions(this, arrayOf(permission), 14546)
    }
}