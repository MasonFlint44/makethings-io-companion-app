package things.dev.features.mainpage.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import things.dev.features.wifi.data.models.WifiScanResult
import javax.inject.Inject

enum class FabAlignmentMode { CENTER, END, }
enum class FabIcon { NEXT, WIFI, }

@HiltViewModel
class MainPageViewModel @Inject constructor() : ViewModel() {
    val pageIndex: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>(0)
    }
    val pageCount: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>(0)
    }
    val fabClicked: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }
    val fabEnabled: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>(false)
    }
    val fabAlignment: MutableLiveData<FabAlignmentMode> by lazy {
        MutableLiveData<FabAlignmentMode>(FabAlignmentMode.END)
    }
    val scanResults: MutableLiveData<List<WifiScanResult>> by lazy {
        MutableLiveData<List<WifiScanResult>>()
    }
    val loading: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>(false)
    }
    val fabIcon: MutableLiveData<FabIcon> by lazy {
        MutableLiveData<FabIcon>(FabIcon.NEXT)
    }

    fun backPressed() {
        onBackPressed()
    }

    private fun onWifiScanStart() {
        scanResults.value = emptyList()
        loading.value = true
    }

    private fun onWifiScanComplete(scanResults: List<WifiScanResult>) {
        loading.value = false
        this.scanResults.value = scanResults
    }

    private fun onBackPressed() {
        prevPage()
        fabEnabled.value = true
    }

    private fun onPasswordUpdated(password: String) {
        fabEnabled.value = password.isNotEmpty()
    }

    private fun onScanResultSelected(scanResult: WifiScanResult) {
        fabEnabled.value = true
    }

    private fun onDeviceSelected() {
        fabIcon.value = FabIcon.WIFI
        fabAlignment.value = FabAlignmentMode.CENTER
        fabEnabled.value = true
    }

    private fun nextPage() {
        if (pageIndex.value == pageCount.value?.minus(1)) { return }
        pageIndex.value = pageIndex.value?.plus(1)
    }

    private fun prevPage() {
        if (pageIndex.value == 0) { return }
        pageIndex.value = pageIndex.value?.minus(1)
    }
}