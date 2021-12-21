package things.dev.fragments.devicescan

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import things.dev.wifi.WifiScanResult

data class ScanResultLoading(val position: Int, val isLoading: Boolean)
data class ScanResultClicked(val position: Int, val scanResult: WifiScanResult)

class DeviceScanViewModel : ViewModel() {
    val scanResults: MutableLiveData<List<WifiScanResult>> by lazy {
        MutableLiveData<List<WifiScanResult>>()
    }
    val loading: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }
    val deviceSelected: MutableLiveData<ScanResultClicked> by lazy {
        MutableLiveData<ScanResultClicked>()
    }
    val scanResultLoading: MutableLiveData<ScanResultLoading> by lazy {
        MutableLiveData<ScanResultLoading>()
    }
}