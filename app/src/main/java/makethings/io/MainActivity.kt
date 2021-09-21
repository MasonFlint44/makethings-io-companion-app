package makethings.io

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import makethings.io.fragments.devicescan.DeviceScanViewModel
import makethings.io.fragments.wifilogin.WifiLoginViewModel
import makethings.io.fragments.wifiscan.WifiScanViewModel
import makethings.io.wifi.WifiScanResult
import makethings.io.wifi.WifiService
import mqtt.broker.Broker

class MainActivity : AppCompatActivity() {
    private val tag = "MainActivity"
    private val deviceScanViewModel: DeviceScanViewModel by viewModels()
    private val wifiScanViewModel: WifiScanViewModel by viewModels()
    private val wifiLoginViewModel: WifiLoginViewModel by viewModels()
    private lateinit var broker: Broker
    private lateinit var wifiService: WifiService
    private lateinit var floatingActionButton: FloatingActionButton
    private lateinit var bottomAppBar: BottomAppBar
    private lateinit var wizardPagerAdapter: WizardPagerAdapter

    private var selectedDevice: WifiScanResult? = null
        set(value) {
            field = value
            floatingActionButton.isEnabled = true
        }

    private var selectedNetwork: WifiScanResult? = null
        set(value) {
            field = value
            floatingActionButton.isEnabled = true
            wifiLoginViewModel.scanResult.value = value
        }

    private var wifiPassword: String? = null
        set(value) {
            field = value
            if (value != null) {
                floatingActionButton.isEnabled = value.isNotEmpty()
            }
        }

    @ExperimentalCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        wifiService = WifiService(applicationContext)

        wizardPagerAdapter = WizardPagerAdapter(this)

        floatingActionButton = findViewById(R.id.floatingActionButton)
        floatingActionButton.isEnabled = false
        floatingActionButton.setOnClickListener {
            floatingActionButton.isEnabled = false
            wizardPagerAdapter.pageIndex++
        }

        deviceScanViewModel.deviceClicked.observe(this, {
            selectedDevice = it
        })
        wifiScanViewModel.scanResultClicked.observe(this, {
            selectedNetwork = it
        })
        wifiLoginViewModel.password.observe(this, {
            wifiPassword = it
        })

        bottomAppBar = findViewById(R.id.bottomAppBar)

        ensurePermission(Manifest.permission.ACCESS_FINE_LOCATION)
        startWifiScan()
    }

    override fun onResume() {
        super.onResume()
        broker = startBroker()
    }

    override fun onPause() {
        super.onPause()
        stopBroker(broker)
    }

    override fun onBackPressed() {
        if (wizardPagerAdapter.pageIndex == 0) {
            super.onBackPressed()
            return
        }
        wizardPagerAdapter.pageIndex--
        floatingActionButton.isEnabled = true
    }

    @ExperimentalCoroutinesApi
    private fun startWifiScan() {
        deviceScanViewModel.scanResults.value = emptyList()
        wifiScanViewModel.scanResults.value = emptyList()

        deviceScanViewModel.loading.value = true
        wifiScanViewModel.loading.value = true

        wifiService.startScan()
            .take(1)
            .onEach { results ->
                deviceScanViewModel.loading.value = false
                wifiScanViewModel.loading.value = false

                deviceScanViewModel.scanResults.value = results
                wifiScanViewModel.scanResults.value = results
            }
            .launchIn(lifecycleScope)
    }

    private fun startBroker(): Broker{
        broker = Broker()
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
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) { return }
        if (ContextCompat.checkSelfPermission(applicationContext, permission) == PackageManager.PERMISSION_GRANTED) { return }
        requestPermissions(arrayOf(permission), 87)
    }
}