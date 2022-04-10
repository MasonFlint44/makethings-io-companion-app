package things.dev.features.wifilogin.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import things.dev.features.wifi.framework.models.WifiScanResult

class WifiLoginViewModel : ViewModel() {
    val scanResult: MutableLiveData<WifiScanResult> by lazy {
        MutableLiveData<WifiScanResult>()
    }
    val password: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    val loading: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }
    val fabClicked: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }
    val isVisible: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }
    val nextPage: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }
    val networkConnected: MutableLiveData<WifiScanResult> by lazy {
        MutableLiveData<WifiScanResult>()
    }
}