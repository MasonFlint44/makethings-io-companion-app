package things.dev

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import things.dev.wifi.WifiScanResult

enum class FabAlignmentMode { CENTER, END, }

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
}