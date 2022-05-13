package things.dev.features.wifiutils.framework.networkcallback

import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.shareIn
import javax.inject.Inject

class AndroidLNetworkCallbackAdapter @Inject constructor(
        private val connectivityManager: ConnectivityManager,
        private val externalScope: CoroutineScope,
    ): NetworkCallbackAdapter {
    override fun networkCallbackFlow(): Flow<NetworkEvent> = callbackFlow {
        val networkCallback = object: ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                offer(NetworkEvent.Available(network))
            }

            override fun onBlockedStatusChanged(network: Network, blocked: Boolean) {
                super.onBlockedStatusChanged(network, blocked)
                offer(NetworkEvent.BlockedStatusChanged(network, blocked))
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                super.onCapabilitiesChanged(network, networkCapabilities)
                offer(NetworkEvent.CapabilitiesChanged(network, networkCapabilities))
            }

            override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
                super.onLinkPropertiesChanged(network, linkProperties)
                offer(NetworkEvent.LinkPropertiesChanged(network, linkProperties))
            }

            override fun onLosing(network: Network, maxMsToLive: Int) {
                super.onLosing(network, maxMsToLive)
                offer(NetworkEvent.Losing(network, maxMsToLive))
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                offer(NetworkEvent.Lost(network))
            }

            override fun onUnavailable() {
                super.onUnavailable()
                offer(NetworkEvent.Unavailable)
            }
        }

        awaitClose {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        }
    }.shareIn(externalScope, SharingStarted.WhileSubscribed(), 1)
}