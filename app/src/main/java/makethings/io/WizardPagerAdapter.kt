package makethings.io

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import makethings.io.fragments.devicescan.DeviceScanFragment
import makethings.io.fragments.wifiscan.WifiScanFragment

class WizardPagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
    private val pager = fa.findViewById<ViewPager2>(R.id.wizardPager)
    private val pages: List<Fragment> = listOf(DeviceScanFragment(), WifiScanFragment())
    var pageIndex = 0
        set(value) {
            field = value
            pager.currentItem = value
        }

    override fun getItemCount(): Int {
        return pages.count()
    }

    override fun createFragment(position: Int): Fragment {
        return pages[position]
    }

    fun nextPage() {
        if (pageIndex == itemCount - 1) { return }
        pageIndex++
    }

    fun prevPage() {
        if (pageIndex == 0) { return }
        pageIndex--
    }
}