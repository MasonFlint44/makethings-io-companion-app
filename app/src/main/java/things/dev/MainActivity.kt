package things.dev

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
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import things.dev.fragments.devicescan.DeviceScanViewModel
import things.dev.fragments.devicescan.ScanResultLoading
import things.dev.fragments.wifilogin.WifiLoginViewModel
import things.dev.fragments.wifiscan.WifiScanViewModel
import things.dev.wifi.NetworkCallbacks
import things.dev.wifi.WifiScanResult
import things.dev.wifi.WifiService
import mqtt.broker.Broker

// TODO: create viewModel for MainActivity
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

    @ExperimentalCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        wifiService = WifiService(this, lifecycleScope)

        wizardPagerAdapter = WizardPagerAdapter(this)
        viewModel.pageIndex.observe(this, {
            wizardPagerAdapter.pageIndex = it
        })

        floatingActionButton = findViewById(R.id.floatingActionButton)
        floatingActionButton.setOnClickListener {
            onFabClicked(it)
        }
        viewModel.fabEnabled.observe(this, {
            floatingActionButton.isEnabled = it
        })

        bottomAppBar = findViewById(R.id.bottomAppBar)

        deviceScanViewModel.deviceSelected.observe(this, {
            // TODO: set button icon to connect icon
            viewModel.fabAlignment.value = FabAlignmentMode.CENTER
            viewModel.fabEnabled.value = true
        })

        wifiScanViewModel.scanResultSelected.observe(this, {
            viewModel.fabEnabled.value = true
            wifiLoginViewModel.scanResult.value = it
        })

        wifiLoginViewModel.password.observe(this, {
            viewModel.fabEnabled.value = it.isNotEmpty()
        })

        wifiLoginViewModel.password.observe(this, {
            viewModel.fabEnabled.value = it.isNotEmpty()
        })

        viewModel.scanResults.observe(this, {
            deviceScanViewModel.scanResults.value = it
            wifiScanViewModel.scanResults.value = it
        })
        viewModel.loading.observe(this, {
            deviceScanViewModel.loading.value = it
            wifiScanViewModel.loading.value = it
        })
        viewModel.fabAlignment.observe(this, {
            when (it) {
                FabAlignmentMode.CENTER -> bottomAppBar.fabAlignmentMode = BottomAppBar.FAB_ALIGNMENT_MODE_CENTER
                FabAlignmentMode.END -> bottomAppBar.fabAlignmentMode = BottomAppBar.FAB_ALIGNMENT_MODE_END
            }
        })

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
        // TODO: check page number to determine behavior
        if (connectedDevice == null) {
            deviceScanViewModel.deviceSelected.value?.let {
                connectToDevice(it.scanResult) {
                    deviceScanViewModel.scanResultLoading.value =
                        ScanResultLoading(it.position, false)
                    viewModel.fabEnabled.value = true
                    viewModel.fabAlignment.value = FabAlignmentMode.END
                }
                deviceScanViewModel.scanResultLoading.value = ScanResultLoading(it.position, true)
            }
        } else if (connectedDevice != null) {
            nextPage()
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