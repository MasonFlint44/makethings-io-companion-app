package things.dev.features.oldwifi.framework

import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities

class AndroidRAndBelowNetworkCallbackProxy : ConnectivityManager.NetworkCallback(), NetworkCallbackProxy {
    override var onAvailable: ((Available) -> Unit)? = null
    override var onBlockedStatusChanged: ((BlockedStatusChanged) -> Unit)? = null
    override var onCapabilitiesChanged: ((CapabilitiesChanged) -> Unit)? = null
    override var onLinkPropertiesChanged: ((LinkPropertiesChanged) -> Unit)? = null
    override var onLosing: ((Losing) -> Unit)? = null
    override var onLost: ((Lost) -> Unit)? = null
    override var onUnavailable: ((Unavailable) -> Unit)? = null
    override val networkCallback: ConnectivityManager.NetworkCallback = this

    override fun onAvailable(network: Network) {
        super.onAvailable(network)
        onAvailable?.invoke(Available(network))
    }

    override fun onBlockedStatusChanged(network: Network, blocked: Boolean) {
        super.onBlockedStatusChanged(network, blocked)
        onBlockedStatusChanged?.invoke(BlockedStatusChanged(network, blocked))
    }

    override fun onCapabilitiesChanged(
        network: Network,
        networkCapabilities: NetworkCapabilities
    ) {
        super.onCapabilitiesChanged(network, networkCapabilities)
        onCapabilitiesChanged?.invoke(CapabilitiesChanged(network, networkCapabilities))
    }

    override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
        super.onLinkPropertiesChanged(network, linkProperties)
        onLinkPropertiesChanged?.invoke(LinkPropertiesChanged(network, linkProperties))
    }

    override fun onLosing(network: Network, maxMsToLive: Int) {
        super.onLosing(network, maxMsToLive)
        onLosing?.invoke(Losing(network, maxMsToLive))
    }

    override fun onLost(network: Network) {
        super.onLost(network)
        onLost?.invoke(Lost(network))
    }

    override fun onUnavailable() {
        super.onUnavailable()
        onUnavailable?.invoke(Unavailable())
    }
}