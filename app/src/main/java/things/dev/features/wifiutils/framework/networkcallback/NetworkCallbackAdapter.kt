package things.dev.features.wifiutils.framework.networkcallback

import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

interface NetworkCallbackAdapter {
    fun networkCallbackFlow(): Flow<NetworkEvent>
}