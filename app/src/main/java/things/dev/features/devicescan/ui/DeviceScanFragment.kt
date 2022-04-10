package things.dev.features.devicescan.ui

import android.net.Network
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import jp.wasabeef.recyclerview.animators.OvershootInRightAnimator
import kotlinx.coroutines.flow.*
import things.dev.R
import things.dev.features.wifi.framework.WifiService
import things.dev.features.wifi.framework.models.WifiScanResult
import javax.inject.Inject

@AndroidEntryPoint
class DeviceScanFragment @Inject constructor(): Fragment() {
    private val viewModel: DeviceScanViewModel by activityViewModels()
    private lateinit var scanResultsView: RecyclerView
    private lateinit var wifiScanProgress: ProgressBar
    private lateinit var emptyResultsMessage: TextView
    @Inject lateinit var scanResultsAdapter: DeviceScanAdapter
    @Inject lateinit var wifiService: WifiService

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        scanResultsAdapter.itemClicked.observe(viewLifecycleOwner) {
            viewModel.deviceSelected.value = it
        }

        viewModel.scanResults.observe(viewLifecycleOwner) {
            scanResultsAdapter.scanResults = it
        }
        viewModel.isEmpty.observe(viewLifecycleOwner) {
            if (it) {
                emptyResultsMessage.visibility = View.VISIBLE
                scanResultsView.visibility = View.GONE
            } else {
                emptyResultsMessage.visibility = View.GONE
                scanResultsView.visibility = View.VISIBLE
            }
        }
        viewModel.loading.observe(viewLifecycleOwner) {
            if (it) {
                wifiScanProgress.visibility = View.VISIBLE
                emptyResultsMessage.visibility = View.GONE
            } else {
                wifiScanProgress.visibility = View.GONE
            }
        }
        viewModel.fabClicked.observe(viewLifecycleOwner) onFabClicked@ {
            if (viewModel.isVisible.value != true) { return@onFabClicked }
            when (viewModel.deviceConnected.value) {
                null -> {
                    viewModel.deviceSelected.value?.let { clicked ->
                        viewModel.scanResultLoading.value = ScanResultLoading(clicked.position, true)
                        connectToDevice(clicked.scanResult)
                            .onEach { viewModel.deviceConnected.value = clicked.scanResult }
                            .launchIn(lifecycleScope)
                    }
                }
                else -> {
                    viewModel.nextPage.value = true
                }
            }
            Log.d(tag, "DeviceScanFragment fab clicked")
        }
        viewModel.deviceConnected.observe(viewLifecycleOwner) {
            viewModel.deviceSelected.value?.let {
                viewModel.scanResultLoading.value = ScanResultLoading(it.position, false)
            }
        }
        viewModel.scanResultLoading.observe(viewLifecycleOwner) {
            scanResultsAdapter.setLoading(it.position, it.isLoading)
        }

        return inflater.inflate(R.layout.device_scan_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        scanResultsView = view.findViewById(R.id.scanResultsView)
        scanResultsView.adapter = scanResultsAdapter
        scanResultsView.layoutManager = LinearLayoutManager(activity)
        scanResultsView.itemAnimator = OvershootInRightAnimator()

        wifiScanProgress = view.findViewById(R.id.wifiScanProgress)
        emptyResultsMessage = view.findViewById(R.id.emptyResultsMessage)
    }

    override fun onResume() {
        super.onResume()
        viewModel.isVisible.value = true
        Log.d(tag, "DeviceScanFragment resumed")
    }

    override fun onPause() {
        super.onPause()
        viewModel.isVisible.value = false
        Log.d(tag, "DeviceScanFragment paused")
    }

    private fun connectToDevice(device: WifiScanResult): Flow<Network> {
        var callbacks = wifiService.requestNetwork(
            device.ssid,
            security = device.security,
            isLocal = true
        )
        return callbacks.available
            .take(1)
            .map { it.network }
            .onCompletion { callbacks.unregister() }
    }
}