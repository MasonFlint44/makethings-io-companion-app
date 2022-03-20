package things.dev.features.wifi.data.models

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.Gson

class WifiScanResult(val ssid: String, val bssid: String, exactFreq: Int, val exactLevel: Int, capabilities: String? = null): Parcelable {
    val level = getWifiLevel(exactLevel)
    val freq = getWifiFreq(exactFreq)
    val security: WifiSecurity? = when {
        capabilities == null -> null
        capabilities.contains("WPA3") -> WifiSecurity.WPA3
        capabilities.contains("WPA2") -> WifiSecurity.WPA2
        capabilities.contains("WPA") -> WifiSecurity.WPA
        capabilities.contains("WEP") -> WifiSecurity.WEP
        else -> WifiSecurity.OPEN
    }

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

    companion object {
        val gson = Gson()

        @JvmField
        val CREATOR = object : Parcelable.Creator<WifiScanResult> {
            override fun createFromParcel(source: Parcel?): WifiScanResult {
                val json = source?.readString()
                return gson.fromJson(json, WifiScanResult::class.java)
            }

            override fun newArray(size: Int): Array<WifiScanResult?> {
                return arrayOfNulls(size)
            }

        }
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeString(gson.toJson(this))
    }
}