package makethings.io.wifi

data class WifiScanResult(val ssid: String, val exactFreq: Int, val exactLevel: Int) {
    val level = getWifiLevel(exactLevel)
    val freq = getWifiFreq(exactFreq)

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