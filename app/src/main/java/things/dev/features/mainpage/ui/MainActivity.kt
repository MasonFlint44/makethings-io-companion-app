package things.dev.features.mainpage.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.Lazy
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import things.dev.R
import things.dev.features.devicescan.ui.DeviceScanViewModel
import things.dev.features.oldwifi.framework.WifiService
import things.dev.features.wifilogin.ui.WifiLoginViewModel
import things.dev.features.wifiscan.ui.WifiScanViewModel
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val viewModel: MainPageViewModel by viewModels()
    private val deviceScanViewModel: DeviceScanViewModel by viewModels()
    private val wifiScanViewModel: WifiScanViewModel by viewModels()
    private val wifiLoginViewModel: WifiLoginViewModel by viewModels()

    private lateinit var floatingActionButton: FloatingActionButton
    private lateinit var bottomAppBar: BottomAppBar

    // Needs to be lazily instantiated because its constructor calls findViewById
    // which needs to be called after setContentView
    @Inject lateinit var wizardPagerAdapter: Lazy<WizardPagerAdapter>
    @Inject lateinit var wifiService: WifiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        floatingActionButton = findViewById(R.id.floatingActionButton)
        bottomAppBar = findViewById(R.id.bottomAppBar)

        floatingActionButton.setOnClickListener {
            viewModel.fabClicked.value = true
            deviceScanViewModel.fabClicked.value = true
            wifiScanViewModel.fabClicked.value = true
            wifiLoginViewModel.fabClicked.value = true
        }

        viewModel.pageIndex.observe(this) {
            wizardPagerAdapter.get().pageIndex = it
        }
        viewModel.fabEnabled.observe(this) {
            floatingActionButton.isEnabled = it
        }
        viewModel.fabAlignment.observe(this) {
            when (it) {
                FabAlignmentMode.CENTER -> bottomAppBar.fabAlignmentMode =
                    BottomAppBar.FAB_ALIGNMENT_MODE_CENTER
                FabAlignmentMode.END -> bottomAppBar.fabAlignmentMode =
                    BottomAppBar.FAB_ALIGNMENT_MODE_END
            }
        }
        viewModel.fabIcon.observe(this) {
            when (it) {
                FabIcon.NEXT -> floatingActionButton.setImageResource(R.drawable.ic_baseline_arrow_forward_24)
                FabIcon.WIFI -> floatingActionButton.setImageResource(R.drawable.twotone_signal_wifi_4_bar_24)
            }
        }

        deviceScanViewModel.deviceSelected.observe(this) {
            viewModel.enableFab(FabIcon.WIFI, FabAlignmentMode.CENTER)
        }
        deviceScanViewModel.deviceConnected.observe(this) {
            viewModel.enableFab(FabIcon.NEXT, FabAlignmentMode.END)
        }
        deviceScanViewModel.nextPage.observe(this) {
            viewModel.nextPage()
        }

        wifiScanViewModel.scanResultSelected.observe(this) {
            viewModel.enableFab(FabIcon.NEXT, FabAlignmentMode.END)
            wifiLoginViewModel.scanResult.value = it
        }
        wifiScanViewModel.nextPage.observe(this) {
            viewModel.nextPage()
        }

        wifiLoginViewModel.password.observe(this) {
            when {
                it.isNotEmpty() -> viewModel.enableFab(FabIcon.WIFI, FabAlignmentMode.CENTER)
                else -> viewModel.disableFab()
            }
        }
        deviceScanViewModel.deviceConnected.observe(this) {
            viewModel.enableFab(FabIcon.NEXT, FabAlignmentMode.END)
        }
        wifiLoginViewModel.nextPage.observe(this) {
            viewModel.nextPage()
        }

        lifecycleScope.launch {
            deviceScanViewModel.loading.value = true
            wifiScanViewModel.loading.value = true
            wifiService.startScan()
                .onEach {
                    deviceScanViewModel.setScanResults(it)
                    wifiScanViewModel.setScanResults(it)
                }
                .collect()
        }
    }

    override fun onBackPressed() {
        if (viewModel.pageIndex.value == 0) {
            super.onBackPressed()
            return
        }
        viewModel.backPressed()
    }
}
