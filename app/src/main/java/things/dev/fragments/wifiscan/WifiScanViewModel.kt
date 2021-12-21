package things.dev.fragments.wifiscan

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import things.dev.wifi.WifiScanResult

class WifiScanViewModel : ViewModel() {
    val scanResults: MutableLiveData<List<WifiScanResult>> by lazy {
        MutableLiveData<List<WifiScanResult>>()
    }
    val loading: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }
    val scanResultSelected: MutableLiveData<WifiScanResult> by lazy {
        MutableLiveData<WifiScanResult>()
    }
}