package makethings.io.wifi

import android.content.*
import android.net.wifi.WifiManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn

class WifiService(private val appContext: Context) : ContextWrapper(appContext) {
    private var wifiManager = getWifiManager()

    private fun getWifiFreq(freq: Int): WifiFreq {
        return when {
            freq / 5000 == 1 -> WifiFreq.FREQ_5_GHZ
            freq / 2400 == 1 -> WifiFreq.FREQ_2_4_GHZ
            else -> WifiFreq.OTHER
        }
    }

    private fun getWifiManager(): WifiManager {
        return appContext.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    }

    @ExperimentalCoroutinesApi
    fun startScan(): Flow<List<WifiScanResult>> = callbackFlow {
        val scanResultsReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val results = wifiManager.scanResults
                results.sortByDescending { it.level }

                val scanResults = results.map { result -> WifiScanResult(result.SSID, getWifiFreq(result.frequency)) }
                offer(scanResults)
            }
        }

        registerReceiver(scanResultsReceiver, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
        wifiManager.startScan()

        awaitClose { unregisterReceiver(scanResultsReceiver) }
    }.flowOn(Dispatchers.IO)
}