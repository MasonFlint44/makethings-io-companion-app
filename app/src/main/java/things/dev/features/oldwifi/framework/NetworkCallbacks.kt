package things.dev.features.oldwifi.framework

import android.net.ConnectivityManager
import androidx.lifecycle.LifecycleCoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*

class NetworkCallbacks(
    lifecycleScope: LifecycleCoroutineScope,
    private val wifiService: WifiService,
    private val networkCallbackProxy: NetworkCallbackProxy,
){
    val networkCallback: ConnectivityManager.NetworkCallback = networkCallbackProxy.networkCallback

    @ExperimentalCoroutinesApi
    val available: Flow<Available> = callbackFlow {
        networkCallbackProxy.onAvailable = {
            offer(it)
        }
        awaitClose {networkCallbackProxy.onAvailable = null}
    }.shareIn(lifecycleScope, SharingStarted.Lazily, 1)

    @ExperimentalCoroutinesApi
    val blockedStatusChanged: Flow<BlockedStatusChanged> = callbackFlow {
        networkCallbackProxy.onBlockedStatusChanged = {
            offer(it)
        }
        awaitClose {networkCallbackProxy.onBlockedStatusChanged = null}
    }.shareIn(lifecycleScope, SharingStarted.Lazily, 1)

    @ExperimentalCoroutinesApi
    val capabilitiesChanged: Flow<CapabilitiesChanged> = callbackFlow {
        networkCallbackProxy.onCapabilitiesChanged = {
            offer(it)
        }
        awaitClose {networkCallbackProxy.onCapabilitiesChanged = null}
    }.shareIn(lifecycleScope, SharingStarted.Lazily, 1)

    @ExperimentalCoroutinesApi
    val linkPropertiesChanged: Flow<LinkPropertiesChanged> = callbackFlow {
        networkCallbackProxy.onLinkPropertiesChanged = {
            offer(it)
        }
        awaitClose {networkCallbackProxy.onLinkPropertiesChanged = null}
    }.shareIn(lifecycleScope, SharingStarted.Lazily, 1)

    @ExperimentalCoroutinesApi
    val losing: Flow<Losing> = callbackFlow {
        networkCallbackProxy.onLosing = {
            offer(it)
        }
        awaitClose {networkCallbackProxy.onLosing = null}
    }.shareIn(lifecycleScope, SharingStarted.Lazily, 1)

    @ExperimentalCoroutinesApi
    val lost: Flow<Lost> = callbackFlow {
        networkCallbackProxy.onLost = {
            offer(it)
        }
        awaitClose {networkCallbackProxy.onLost = null}
    }.shareIn(lifecycleScope, SharingStarted.Lazily, 1)

    @ExperimentalCoroutinesApi
    val unavailable: Flow<Unavailable> = callbackFlow {
        networkCallbackProxy.onUnavailable = {
            offer(it)
        }
        awaitClose {networkCallbackProxy.onUnavailable = null}
    }.shareIn(lifecycleScope, SharingStarted.Lazily, 1)

    fun unregister() {
        wifiService.restoreDisabledNetworks()
        wifiService.disableRequestedNetwork()
        wifiService.unregisterNetworkCallbacks(this)
    }
}