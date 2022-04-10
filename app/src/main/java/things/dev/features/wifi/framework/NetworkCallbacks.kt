package things.dev.features.wifi.framework

import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import androidx.lifecycle.LifecycleCoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class NetworkCallbacks(
    lifecycleScope: LifecycleCoroutineScope,
    private val wifiService: WifiService,
    private val networkCallbackProxy: NetworkCallbackProxy,
){
    data class Available(val network: Network)
    data class BlockedStatusChanged(val network: Network, val blocked: Boolean)
    data class CapabilitiesChanged(val network: Network, val networkCapabilities: NetworkCapabilities)
    data class LinkPropertiesChanged(val network: Network, val linkProperties: LinkProperties)
    data class Losing(val network: Network, val maxMsToLive: Int)
    data class Lost(val network: Network)
    class Unavailable

    val networkCallback: ConnectivityManager.NetworkCallback = networkCallbackProxy.networkCallback

    @ExperimentalCoroutinesApi
    val available: Flow<Available> = callbackFlow {
        networkCallbackProxy.onAvailable = {
            offer(Available(it))
        }
        awaitClose {networkCallbackProxy.onAvailable = null}
    }.shareIn(lifecycleScope, SharingStarted.Lazily, 1)

    @ExperimentalCoroutinesApi
    val blockedStatusChanged: Flow<BlockedStatusChanged> = callbackFlow {
        networkCallbackProxy.onBlockedStatusChanged = { network, blocked ->
            offer(BlockedStatusChanged(network, blocked))
        }
        awaitClose {networkCallbackProxy.onBlockedStatusChanged = null}
    }.shareIn(lifecycleScope, SharingStarted.Lazily, 1)

    @ExperimentalCoroutinesApi
    val capabilitiesChanged: Flow<CapabilitiesChanged> = callbackFlow {
        networkCallbackProxy.onCapabilitiesChanged = { network, capabilities ->
            offer(CapabilitiesChanged(network, capabilities))
        }
        awaitClose {networkCallbackProxy.onCapabilitiesChanged = null}
    }.shareIn(lifecycleScope, SharingStarted.Lazily, 1)

    @ExperimentalCoroutinesApi
    val linkPropertiesChanged: Flow<LinkPropertiesChanged> = callbackFlow {
        networkCallbackProxy.onLinkPropertiesChanged = { network, linkProperties ->
            offer(LinkPropertiesChanged(network, linkProperties))
        }
        awaitClose {networkCallbackProxy.onLinkPropertiesChanged = null}
    }.shareIn(lifecycleScope, SharingStarted.Lazily, 1)

    @ExperimentalCoroutinesApi
    val losing: Flow<Losing> = callbackFlow {
        networkCallbackProxy.onLosing = { network, maxMsToLive ->
            offer(Losing(network, maxMsToLive))
        }
        awaitClose {networkCallbackProxy.onLosing = null}
    }.shareIn(lifecycleScope, SharingStarted.Lazily, 1)

    @ExperimentalCoroutinesApi
    val lost: Flow<Lost> = callbackFlow {
        networkCallbackProxy.onLost = {
            offer(Lost(it))
        }
        awaitClose {networkCallbackProxy.onLost = null}
    }.shareIn(lifecycleScope, SharingStarted.Lazily, 1)

    @ExperimentalCoroutinesApi
    val unavailable: Flow<Unavailable> = callbackFlow {
        networkCallbackProxy.onUnavailable = {
            offer(Unavailable())
        }
        awaitClose {networkCallbackProxy.onUnavailable = null}
    }.shareIn(lifecycleScope, SharingStarted.Lazily, 1)

    fun unregister() {
        wifiService.restoreDisabledNetworks()
        wifiService.disableRequestedNetwork()
        wifiService.unregisterNetworkCallbacks(this)
    }
}