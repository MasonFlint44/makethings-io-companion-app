package things.dev.features.wifiutils.framework.networkspecifier

import things.dev.features.wifiutils.framework.models.WifiSecurity

class AndroidUnsuppotedNetworkSpecifierFactory : NetworkSpecifierFactory {
    override fun createNetworkSpecifier(
        ssid: String?,
        bssid: String?,
        password: String?,
        security: WifiSecurity?
    ): NetworkSpecifierWrapper? {
        return null
    }
}