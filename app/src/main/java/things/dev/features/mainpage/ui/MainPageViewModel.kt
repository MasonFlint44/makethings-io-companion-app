package things.dev.features.mainpage.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
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
    val loading: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>(false)
    }
    val fabIcon: MutableLiveData<FabIcon> by lazy {
        MutableLiveData<FabIcon>(FabIcon.NEXT)
    }

    fun enableFab(icon: FabIcon? = null, alignment: FabAlignmentMode? = null) {
        fabEnabled.value = true
        icon?.let { fabIcon.value = it }
        alignment?.let { fabAlignment.value = it }
    }

    fun disableFab(icon: FabIcon? = null, alignment: FabAlignmentMode? = null) {
        fabEnabled.value = false
        icon?.let { fabIcon.value = it }
        alignment?.let { fabAlignment.value = it }
    }

    fun backPressed() {
        prevPage()
        fabEnabled.value = true
    }

    fun nextPage() {
        if (pageIndex.value == pageCount.value?.minus(1)) { return }
        pageIndex.value = pageIndex.value?.plus(1)
        disableFab()
    }

    fun prevPage() {
        if (pageIndex.value == 0) { return }
        pageIndex.value = pageIndex.value?.minus(1)
    }
}