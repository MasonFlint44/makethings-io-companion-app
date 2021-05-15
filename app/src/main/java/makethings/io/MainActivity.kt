package makethings.io

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.gson.*
import kotlinx.coroutines.*
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import makethings.io.fragments.devicescan.DeviceScanViewModel
import makethings.io.fragments.wifiscan.WifiScanViewModel
import makethings.io.wifi.WifiService

data class WifiCredentials(val ssid: String, val password: String)

class MainActivity : AppCompatActivity() {
    private val tag = "MainActivity"
    private val port = 22983
    private val deviceScanViewModel: DeviceScanViewModel by viewModels()
    private val wifiScanViewModel: WifiScanViewModel by viewModels()
    private lateinit var server: NettyApplicationEngine
    private lateinit var wifiService: WifiService
    private lateinit var wifiSsid: EditText
    private lateinit var wifiPassword: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        wifiService = WifiService(applicationContext)

        wifiSsid = findViewById(R.id.wifiSsid)
        wifiPassword = findViewById(R.id.wifiPassword)

        ensurePermission(Manifest.permission.ACCESS_FINE_LOCATION)
        startWifiScan()
    }

    override fun onResume() {
        super.onResume()
        startServer()
    }

    override fun onPause() {
        super.onPause()
        pauseServer()
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

    private fun startServer() {
        lifecycleScope.launch(Dispatchers.IO) {
            Log.d(tag,"Starting server on port $port")
            server = embeddedServer(Netty, port = port) {
                install(ContentNegotiation) {
                    gson { }
                }
                routing {
                    get("/GetWifiCredentials") {
                        call.respond(WifiCredentials(wifiSsid.text.toString(), wifiPassword.text.toString()))
                    }
                }
            }.start()
        }
    }

    private fun pauseServer() {
        lifecycleScope.launch(Dispatchers.IO) {
            Log.d(tag,"Stopping server on port $port")
            server.stop(5000, 5000)
        }
    }

    private fun ensurePermission(permission: String) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) { return }
        if (ContextCompat.checkSelfPermission(applicationContext, permission) == PackageManager.PERMISSION_GRANTED) { return }
        requestPermissions(arrayOf(permission), 87)
    }
}