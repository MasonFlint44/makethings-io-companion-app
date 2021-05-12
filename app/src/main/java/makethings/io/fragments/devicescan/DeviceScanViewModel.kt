package makethings.io.fragments.devicescan

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import makethings.io.wifi.WifiScanResult

class DeviceScanViewModel : ViewModel() {
    val scanResults: MutableLiveData<List<WifiScanResult>> by lazy {
        MutableLiveData<List<WifiScanResult>>()
    }
    val loading: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }
    val deviceClicked: MutableLiveData<WifiScanResult> by lazy {
        MutableLiveData<WifiScanResult>()
    }
}