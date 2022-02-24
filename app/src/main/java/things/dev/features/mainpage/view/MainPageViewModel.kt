package things.dev.features.mainpage.view

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import things.dev.features.wifi.domain.models.WifiScanResult

enum class FabAlignmentMode { CENTER, END, }
enum class FabIcon { NEXT, WIFI, }

class MainActivityViewModel : ViewModel() {
    val pageIndex: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>(0)
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
}