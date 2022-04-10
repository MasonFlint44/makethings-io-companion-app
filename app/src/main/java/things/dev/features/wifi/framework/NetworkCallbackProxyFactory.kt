package things.dev.features.wifi.framework

import android.os.Build
import javax.inject.Inject
import javax.inject.Named

class NetworkCallbackProxyFactory @Inject constructor(@Named("NetworkCallbackFlags") private val flags: Int) {
    fun getNetworkCallbackProxy(): NetworkCallbackProxy {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
            return AndroidRAndBelowNetworkCallbackProxy()
        }
        return AndroidSAndAboveNetworkCallbackProxy(flags)
    }
}