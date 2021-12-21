package things.dev.fragments.wifilogin

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import things.dev.wifi.WifiScanResult

class WifiLoginViewModel : ViewModel() {
    val scanResult: MutableLiveData<WifiScanResult> by lazy {
        MutableLiveData<WifiScanResult>()
    }
    val password: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
}