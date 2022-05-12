package things.dev.features.wifiutils.framework.networkcallback

import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.flow.Flow

data class Available(val network: Network)
data class BlockedStatusChanged(val network: Network, val blocked: Boolean)
data class CapabilitiesChanged(val network: Network, val networkCapabilities: NetworkCapabilities)
data class LinkPropertiesChanged(val network: Network, val linkProperties: LinkProperties)
data class Losing(val network: Network, val maxMsToLive: Int)
data class Lost(val network: Network)
class Unavailable

interface NetworkCallbackAdapter {
    var onAvailable: Flow<Available>
    var onBlockedStatusChanged: Flow<BlockedStatusChanged>
    var onCapabilitiesChanged: Flow<CapabilitiesChanged>
    var onLinkPropertiesChanged: Flow<LinkPropertiesChanged>
    var onLosing: Flow<Losing>
    var onLost: Flow<Lost>
    var onUnavailable: Flow<Unavailable>
//    val networkCallback: ConnectivityManager.NetworkCallback
}