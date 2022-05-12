package things.dev.features.wifiutils.framework.networkrequest

import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import androidx.annotation.RequiresApi
import things.dev.features.wifiutils.framework.networkspecifier.NetworkSpecifierWrapper

class AndroidQNetworkRequestFactory: NetworkRequestFactory {
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun createNetworkRequest(
        networkSpecifier: NetworkSpecifierWrapper?,
        hasInternet: Boolean?
    ): NetworkRequest {
        val builder = NetworkRequest.Builder()
        builder.addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
        networkSpecifier?.let { builder.setNetworkSpecifier(it.networkSpecifier) }
        when(hasInternet) {
            true -> builder.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            false -> builder.removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            else -> {/* do nothing */}
        }
        return builder.build()
    }
}