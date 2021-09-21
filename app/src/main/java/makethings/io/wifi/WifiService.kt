package makethings.io.wifi

import android.content.*
import android.net.wifi.WifiManager
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn

class WifiService(private val appContext: Context) : ContextWrapper(appContext) {
    private val tag = "WifiService"
    private var wifiManager = getWifiManager()

    private fun getWifiManager(): WifiManager {
        return appContext.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    }

    @ExperimentalCoroutinesApi
    fun startScan(): Flow<List<WifiScanResult>> = callbackFlow {
        val scanResultsReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                Log.d(tag,"Completed wifi network scan")
                val scanResults = wifiManager.scanResults.map { result ->
                    WifiScanResult(result.SSID, result.frequency, result.level, result.capabilities)
                }
                offer(scanResults)
            }
        }

        Log.d(tag,"Starting wifi network scan")
        registerReceiver(scanResultsReceiver, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
        wifiManager.startScan()

        awaitClose { unregisterReceiver(scanResultsReceiver) }
    }.flowOn(Dispatchers.IO)
}