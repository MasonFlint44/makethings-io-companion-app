package things.dev

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import things.dev.fragments.devicescan.DeviceScanFragment
import things.dev.fragments.wifilogin.WifiLoginFragment
import things.dev.fragments.wifiscan.WifiScanFragment

class WizardPagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
    private val pager = fa.findViewById<ViewPager2>(R.id.wizardPager)
    private val pages: List<Fragment> = listOf(
        DeviceScanFragment(),
        WifiScanFragment(),
        WifiLoginFragment()
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