package things.dev.features.oldwifi.framework

import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities

data class Available(val network: Network)
data class BlockedStatusChanged(val network: Network, val blocked: Boolean)
data class CapabilitiesChanged(val network: Network, val networkCapabilities: NetworkCapabilities)
data class LinkPropertiesChanged(val network: Network, val linkProperties: LinkProperties)
data class Losing(val network: Network, val maxMsToLive: Int)
data class Lost(val network: Network)
class Unavailable

interface NetworkCallbackProxy {
    var onAvailable: ((Available) -> Unit)?
    var onBlockedStatusChanged: ((BlockedStatusChanged) -> Unit)?
    var onCapabilitiesChanged: ((CapabilitiesChanged) -> Unit)?
    var onLinkPropertiesChanged: ((LinkPropertiesChanged) -> Unit)?
    var onLosing: ((Losing) -> Unit)?
    var onLost: ((Lost) -> Unit)?
    var onUnavailable: ((Unavailable) -> Unit)?
    val networkCallback: ConnectivityManager.NetworkCallback
}