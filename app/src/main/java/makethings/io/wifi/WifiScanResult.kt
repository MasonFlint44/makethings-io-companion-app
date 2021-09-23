package makethings.io.wifi

import android.net.MacAddress
import android.os.Build
import androidx.annotation.RequiresApi

class WifiScanResult(val ssid: String, bssid: String, exactFreq: Int, exactLevel: Int, capabilities: String) {
    val level = getWifiLevel(exactLevel)
    val freq = getWifiFreq(exactFreq)
    val security: WifiSecurity = when {
        capabilities.contains("WPA3") -> WifiSecurity.WPA3
        capabilities.contains("WPA2") -> WifiSecurity.WPA2
        capabilities.contains("WPA") -> WifiSecurity.WPA
        capabilities.contains("WEP") -> WifiSecurity.WEP
        else -> WifiSecurity.OPEN
    }
    @RequiresApi(Build.VERSION_CODES.P)
    val bssid = MacAddress.fromString(bssid)

    private fun getWifiFreq(freq: Int): WifiFreq {
        return when {
            freq / 5000 == 1 -> WifiFreq.FREQ_5_GHZ
            freq / 2400 == 1 -> WifiFreq.FREQ_2_4_GHZ
            else -> WifiFreq.OTHER
        }
    }

    private fun getWifiLevel(level: Int): WifiLevel {
        return when {
            level > -44 -> WifiLevel.EXCELLENT
            level > -55 -> WifiLevel.GOOD
            level > -72 -> WifiLevel.FAIR
            level > -86 -> WifiLevel.WEAK
            else -> WifiLevel.NONE
        }
    }
}