package things.dev.features.wifiutils.framework.networkspecifier

import android.net.MacAddress
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build
import androidx.annotation.RequiresApi
import things.dev.features.wifiutils.framework.models.WifiSecurity

class AndroidQNetworkSpecifierFactory: NetworkSpecifierFactory {
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun createNetworkSpecifier(
        ssid: String?,
        bssid: String?,
        password: String?,
        security: WifiSecurity?
    ): NetworkSpecifierWrapper? {
        val builder = WifiNetworkSpecifier.Builder()
        ssid?.let { builder.setSsid(it) }
        bssid?.let { builder.setBssid(MacAddress.fromString(it)) }
        password?.let {
            when(security) {
                WifiSecurity.WPA2 -> builder.setWpa2Passphrase(password)
                WifiSecurity.WPA3 -> builder.setWpa3Passphrase(password)
                else -> {/* do nothing */}
            }
        }
        return NetworkSpecifierWrapper(builder.build())
    }
}