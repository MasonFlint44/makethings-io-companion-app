package things.dev.features.wifiutils.framework.networkcallback

import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities

sealed class NetworkEvent {
    data class Available(val network: Network): NetworkEvent()
    data class BlockedStatusChanged(val network: Network, val blocked: Boolean): NetworkEvent()
    data class CapabilitiesChanged(val network: Network, val networkCapabilities: NetworkCapabilities): NetworkEvent()
    data class LinkPropertiesChanged(val network: Network, val linkProperties: LinkProperties): NetworkEvent()
    data class Losing(val network: Network, val maxMsToLive: Int): NetworkEvent()
    data class Lost(val network: Network): NetworkEvent()
    object Unavailable: NetworkEvent()
}