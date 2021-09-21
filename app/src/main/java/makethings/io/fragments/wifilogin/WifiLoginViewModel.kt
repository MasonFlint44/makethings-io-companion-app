package makethings.io.fragments.wifilogin

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import makethings.io.wifi.WifiScanResult

class WifiLoginViewModel : ViewModel() {
    val scanResult: MutableLiveData<WifiScanResult> by lazy {
        MutableLiveData<WifiScanResult>()
    }
    val password: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
}