package things.dev.features.wifiutils.framework.networkspecifier

import things.dev.features.wifiutils.framework.models.WifiSecurity

interface NetworkSpecifierFactory {
    fun createNetworkSpecifier(
        ssid: String? = null,
        bssid: String? = null,
        password: String? = null,
        security: WifiSecurity? = null
    ): NetworkSpecifierWrapper?
}