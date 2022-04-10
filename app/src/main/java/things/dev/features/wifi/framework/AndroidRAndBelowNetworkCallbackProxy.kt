package things.dev.features.wifi.framework

import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import androidx.annotation.RequiresApi
import javax.inject.Inject
import javax.inject.Named

class AndroidRAndBelowNetworkCallbackProxy : ConnectivityManager.NetworkCallback(), NetworkCallbackProxy {
    override var onAvailable: ((Network) -> Unit)? = null
    override var onBlockedStatusChanged: ((Network, Boolean) -> Unit)? = null
    override var onCapabilitiesChanged: ((Network, NetworkCapabilities) -> Unit)? = null
    override var onLinkPropertiesChanged: ((Network, LinkProperties) -> Unit)? = null
    override var onLosing: ((Network, Int) -> Unit)? = null
    override var onLost: ((Network) -> Unit)? = null
    override var onUnavailable: (() -> Unit)? = null
    override val networkCallback: ConnectivityManager.NetworkCallback = this

    override fun onAvailable(network: Network) {
        super.onAvailable(network)
        onAvailable?.invoke(network)
    }

    override fun onBlockedStatusChanged(network: Network, blocked: Boolean) {
        super.onBlockedStatusChanged(network, blocked)
        onBlockedStatusChanged?.invoke(network, blocked)
    }

    override fun onCapabilitiesChanged(
        network: Network,
        networkCapabilities: NetworkCapabilities
    ) {
        super.onCapabilitiesChanged(network, networkCapabilities)
        onCapabilitiesChanged?.invoke(network, networkCapabilities)
    }

    override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
        super.onLinkPropertiesChanged(network, linkProperties)
        onLinkPropertiesChanged?.invoke(network, linkProperties)
    }

    override fun onLosing(network: Network, maxMsToLive: Int) {
        super.onLosing(network, maxMsToLive)
        onLosing?.invoke(network, maxMsToLive)
    }

    override fun onLost(network: Network) {
        super.onLost(network)
        onLost?.invoke(network)
    }

    override fun onUnavailable() {
        super.onUnavailable()
        onUnavailable?.invoke()
    }
}