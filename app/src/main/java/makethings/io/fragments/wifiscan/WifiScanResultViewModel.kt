package makethings.io.fragments.wifiscan

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import makethings.io.wifi.WifiScanResult

class WifiScanResultViewModel : ViewModel() {
    val scanResults: MutableLiveData<List<WifiScanResult>> by lazy {
        MutableLiveData<List<WifiScanResult>>()
    }
}