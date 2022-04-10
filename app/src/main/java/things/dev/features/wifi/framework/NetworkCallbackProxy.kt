package things.dev.features.wifi.framework

import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities

// TODO: consider updating this to include data classes from NetworkCallbacks
interface NetworkCallbackProxy {
    var onAvailable: ((Network) -> Unit)?
    var onBlockedStatusChanged: ((Network, Boolean) -> Unit)?
    var onCapabilitiesChanged: ((Network, NetworkCapabilities) -> Unit)?
    var onLinkPropertiesChanged: ((Network, LinkProperties) -> Unit)?
    var onLosing: ((Network, Int) -> Unit)?
    var onLost: ((Network) -> Unit)?
    var onUnavailable: (() -> Unit)?
    val networkCallback: ConnectivityManager.NetworkCallback
}