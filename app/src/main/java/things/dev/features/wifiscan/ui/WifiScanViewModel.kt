package things.dev.features.wifiscan.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import things.dev.features.oldwifi.framework.models.WifiScanResult
import javax.inject.Inject

@HiltViewModel
class WifiScanViewModel @Inject constructor(): ViewModel() {
    private val deviceSsid = "things.dev"

    val scanResults: MutableLiveData<List<WifiScanResult>> by lazy {
        MutableLiveData<List<WifiScanResult>>()
    }
    val loading: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }
    val scanResultSelected: MutableLiveData<WifiScanResult> by lazy {
        MutableLiveData<WifiScanResult>()
    }
    val isEmpty: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }
    val fabClicked: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }
    val nextPage: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }
    val isVisible: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    fun setScanResults(scanResults: List<WifiScanResult>) {
        this.scanResults.value = filterScanResults(scanResults)
        setIsEmpty(this.scanResults.value!!)
        loading.value = false
    }

    private fun filterScanResults(scanResults: List<WifiScanResult>): List<WifiScanResult> {
        return scanResults.filter { it.ssid != deviceSsid }
            .sortedByDescending { it.exactLevel }
            .sortedBy { it.freq }
    }

    private fun setIsEmpty(scanResults: List<WifiScanResult>) {
        isEmpty.value = scanResults.isEmpty()
    }
}