package things.dev.features.wifiutils.framework.networkrequest

import android.net.NetworkRequest
import things.dev.features.wifiutils.framework.networkspecifier.NetworkSpecifierWrapper

interface NetworkRequestFactory {
    fun createNetworkRequest(
        networkSpecifier: NetworkSpecifierWrapper? = null,
        hasInternet: Boolean? = null
    ): NetworkRequest
}