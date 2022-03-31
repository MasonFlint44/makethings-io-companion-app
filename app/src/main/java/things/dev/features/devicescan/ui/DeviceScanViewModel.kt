package things.dev.features.devicescan.ui

import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import things.dev.features.wifi.framework.models.WifiFreq
import things.dev.features.wifi.framework.models.WifiScanResult
import javax.inject.Inject

data class ScanResultLoading(val position: Int, val isLoading: Boolean)
data class ScanResultClicked(val position: Int, val scanResult: WifiScanResult)

@HiltViewModel
class DeviceScanViewModel @Inject constructor(): ViewModel() {
    private val deviceSsid = "things.dev"

    val scanResults: MutableLiveData<List<WifiScanResult>> by lazy {
        MutableLiveData<List<WifiScanResult>>()
    }
    val isEmpty: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
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
    val deviceConnected: MutableLiveData<WifiScanResult> by lazy {
        MutableLiveData<WifiScanResult>()
    }
    val fabClicked: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }
    val nextPage: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    fun setScanResults(scanResults: List<WifiScanResult>) {
        this.scanResults.value = filterScanResults(scanResults)
        setIsEmpty(this.scanResults.value!!)
        loading.value = false
    }

    private fun filterScanResults(scanResults: List<WifiScanResult>): List<WifiScanResult> {
        return scanResults.filter {
            it.ssid == deviceSsid && it.freq == WifiFreq.FREQ_2_4_GHZ
        }.sortedByDescending { it.exactLevel }
    }

    private fun setIsEmpty(scanResults: List<WifiScanResult>) {
        isEmpty.value = scanResults.isEmpty()
    }
}