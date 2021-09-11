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
import makethings.io.fragments.wifiscan.WifiScanViewModel
import makethings.io.wifi.WifiService
import mqtt.broker.Broker

//data class WifiCredentials(val ssid: String, val password: String)

class MainActivity : AppCompatActivity() {
    private val tag = "MainActivity"
//    private val port = 22983
    private val deviceScanViewModel: DeviceScanViewModel by viewModels()
    private val wifiScanViewModel: WifiScanViewModel by viewModels()
//    private lateinit var server: NettyApplicationEngine
    private lateinit var broker: Broker
    private lateinit var wifiService: WifiService
//    private lateinit var wifiSsid: EditText
//    private lateinit var wifiPassword: EditText
    private lateinit var floatingActionButton: FloatingActionButton
    private lateinit var bottomAppBar: BottomAppBar

//    override fun onEnterAnimationComplete() {
//        super.onEnterAnimationComplete()
//        bottomAppBar.fabAlignmentMode = BottomAppBar.FAB_ALIGNMENT_MODE_END
//    }

    @ExperimentalCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        wifiService = WifiService(applicationContext)

//        wifiSsid = findViewById(R.id.wifiSsid)
//        wifiPassword = findViewById(R.id.wifiPassword)

        floatingActionButton = findViewById(R.id.floatingActionButton)
        floatingActionButton.isEnabled = false
        floatingActionButton.setOnClickListener {
            Log.d(tag, "button clicked")
        }

        bottomAppBar = findViewById(R.id.bottomAppBar)

        ensurePermission(Manifest.permission.ACCESS_FINE_LOCATION)
        startWifiScan()

//        lifecycleScope.launch {
//            delay(10000)
////            floatingActionButton.visibility = View.VISIBLE
//
//        }
    }

    override fun onResume() {
        super.onResume()
//        startServer()
        broker = startBroker()
    }

    override fun onPause() {
        super.onPause()
//        pauseServer()
        stopBroker(broker)
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

//    private fun startServer() {
//        lifecycleScope.launch(Dispatchers.IO) {
//            Log.d(tag,"Starting server on port $port")
//            server = embeddedServer(Netty, port = port) {
//                install(ContentNegotiation) {
//                    gson { }
//                }
//                routing {
//                    get("/GetWifiCredentials") {
////                        call.respond(WifiCredentials(wifiSsid.text.toString(), wifiPassword.text.toString()))
//                    }
//                }
//            }.start()
//        }
//    }

    private fun stopBroker(broker: Broker) {
        broker.stop()
        Log.d(tag, "Stopping MQTT broker")
    }

//    private fun pauseServer() {
//        lifecycleScope.launch(Dispatchers.IO) {
//            Log.d(tag,"Stopping server on port $port")
//            server.stop(5000, 5000)
//        }
//    }

    private fun ensurePermission(permission: String) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) { return }
        if (ContextCompat.checkSelfPermission(applicationContext, permission) == PackageManager.PERMISSION_GRANTED) { return }
        requestPermissions(arrayOf(permission), 87)
    }
}