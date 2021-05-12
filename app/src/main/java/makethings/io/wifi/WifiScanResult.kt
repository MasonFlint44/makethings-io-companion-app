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
            level <= -42 -> WifiLevel.EXCELLENT
            level <= -54 -> WifiLevel.GOOD
            level <= -65 -> WifiLevel.FAIR
            level <= -77 -> WifiLevel.NONE
            else -> WifiLevel.UNKNOWN
        }
    }
}