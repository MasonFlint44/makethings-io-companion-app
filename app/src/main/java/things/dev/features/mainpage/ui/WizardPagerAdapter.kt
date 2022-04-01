package things.dev.features.mainpage.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import things.dev.R
import things.dev.features.devicescan.ui.DeviceScanFragment
import things.dev.features.wifilogin.view.WifiLoginFragment
import things.dev.features.wifiscan.ui.WifiScanFragment
import javax.inject.Inject

class WizardPagerAdapter @Inject constructor(
    fa: FragmentActivity,
    deviceScanFragment: DeviceScanFragment,
    wifiScanFragment: WifiScanFragment,
    wifiLoginFragment: WifiLoginFragment
) : FragmentStateAdapter(fa) {
    private val pager = fa.findViewById<ViewPager2>(R.id.wizardPager)
    private val pages: List<Fragment> = listOf(
        deviceScanFragment,
        wifiScanFragment,
        wifiLoginFragment,
    )
    var pageIndex = 0
        set(value) {
            field = value
            pager.currentItem = value
        }

    init {
        pager.adapter = this
        pager.isUserInputEnabled = false
    }

    override fun getItemCount(): Int {
        return pages.count()
    }

    override fun createFragment(position: Int): Fragment {
        return pages[position]
    }
}