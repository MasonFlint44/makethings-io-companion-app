package makethings.io

import android.os.Bundle
import android.util.Log
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.gson.*
import kotlinx.coroutines.*
import androidx.lifecycle.lifecycleScope

data class WifiCredentials(val ssid: String, val password: String)

class MainActivity : AppCompatActivity() {
    private val tag = "MainActivity"
    private val port = 22983
    private var server: NettyApplicationEngine? = null
    private lateinit var wifiSsid: EditText
    private lateinit var wifiPassword: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        wifiSsid = findViewById(R.id.wifiSsid)
        wifiPassword = findViewById(R.id.wifiPassword)
    }

    override fun onResume() {
        super.onResume()
        startServer()
    }

    override fun onPause() {
        super.onPause()
        pauseServer()
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
            server?.stop(5000, 5000)
        }
    }
}