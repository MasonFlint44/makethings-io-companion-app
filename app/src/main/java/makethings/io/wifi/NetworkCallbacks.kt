package makethings.io.wifi

import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn

class NetworkCallbacks {
    data class Available(val network: Network)
    data class BlockedStatusChanged(val network: Network, val blocked: Boolean)
    data class CapabilitiesChanged(val network: Network, val networkCapabilities: NetworkCapabilities)
    data class LinkPropertiesChanged(val network: Network, val linkProperties: LinkProperties)
    data class Losing(val network: Network, val maxMsToLive: Int)
    data class Lost(val network: Network)
    class Unavailable()

    @ExperimentalCoroutinesApi
    val available: Flow<Available> = callbackFlow {
        _networkCallback.availableCallback = {
            offer(Available(it))
        }
    }.flowOn(Dispatchers.IO)

    @ExperimentalCoroutinesApi
    val blockedStatusChanged: Flow<BlockedStatusChanged> = callbackFlow {
        _networkCallback.blockedStatusChangedCallback = { network, blocked ->
            offer(BlockedStatusChanged(network, blocked))
        }
    }.flowOn(Dispatchers.IO)

    @ExperimentalCoroutinesApi
    val capabilitiesChanged: Flow<CapabilitiesChanged> = callbackFlow {
        _networkCallback.capabilitiesChangedCallback = { network, capabilities ->
            offer(CapabilitiesChanged(network, capabilities))
        }
    }.flowOn(Dispatchers.IO)

    @ExperimentalCoroutinesApi
    val linkPropertiesChanged: Flow<LinkPropertiesChanged> = callbackFlow {
        _networkCallback.linkPropertiesChangedCallback = { network, linkProperties ->
            offer(LinkPropertiesChanged(network, linkProperties))
        }
    }.flowOn(Dispatchers.IO)

    @ExperimentalCoroutinesApi
    val losing: Flow<Losing> = callbackFlow {
        _networkCallback.losingCallback = { network, maxMsToLive ->
            offer(Losing(network, maxMsToLive))
        }
    }.flowOn(Dispatchers.IO)

    @ExperimentalCoroutinesApi
    val lost: Flow<Lost> = callbackFlow {
        _networkCallback.lostCallback = {
            offer(Lost(it))
        }
    }.flowOn(Dispatchers.IO)

    @ExperimentalCoroutinesApi
    val unavailable: Flow<Unavailable> = callbackFlow {
        _networkCallback.unavailableCallback = {
            offer(Unavailable())
        }
    }

    private val _networkCallback = object: ConnectivityManager.NetworkCallback() {
        var availableCallback: ((Network) -> Unit)? = null
        var blockedStatusChangedCallback: ((Network, Boolean) -> Unit)? = null
        var capabilitiesChangedCallback: ((Network, NetworkCapabilities) -> Unit)? = null
        var linkPropertiesChangedCallback: ((Network, LinkProperties) -> Unit)? = null
        var losingCallback: ((Network, Int) -> Unit)? = null
        var lostCallback: ((Network) -> Unit)? = null
        var unavailableCallback: (() -> Unit)? = null

        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            availableCallback?.invoke(network)
        }

        override fun onBlockedStatusChanged(network: Network, blocked: Boolean) {
            super.onBlockedStatusChanged(network, blocked)
            blockedStatusChangedCallback?.invoke(network, blocked)
        }

        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            super.onCapabilitiesChanged(network, networkCapabilities)
            capabilitiesChangedCallback?.invoke(network, networkCapabilities)
        }

        override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
            super.onLinkPropertiesChanged(network, linkProperties)
            linkPropertiesChangedCallback?.invoke(network, linkProperties)
        }

        override fun onLosing(network: Network, maxMsToLive: Int) {
            super.onLosing(network, maxMsToLive)
            losingCallback?.invoke(network, maxMsToLive)
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            lostCallback?.invoke(network)
        }

        override fun onUnavailable() {
            super.onUnavailable()
            unavailableCallback?.invoke()
        }
    }
    val networkCallback: ConnectivityManager.NetworkCallback = _networkCallback
}