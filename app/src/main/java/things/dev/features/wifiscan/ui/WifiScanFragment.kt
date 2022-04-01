package things.dev.features.wifiscan.ui

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import jp.wasabeef.recyclerview.animators.OvershootInRightAnimator
import things.dev.R
import things.dev.features.wifi.WifiService
import javax.inject.Inject

@AndroidEntryPoint
class WifiScanFragment @Inject constructor(): Fragment() {
    private val deviceSsid = "things.dev"
    private val viewModel: WifiScanViewModel by activityViewModels()
    private lateinit var scanResultsView: RecyclerView
    private lateinit var wifiScanProgress: ProgressBar
    private lateinit var emptyResultsMessage: TextView
    @Inject lateinit var scanResultsAdapter: WifiScanAdapter
    @Inject lateinit var wifiService: WifiService

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        scanResultsAdapter.itemClicked.observe(viewLifecycleOwner) {
            viewModel.scanResultSelected.value = it.scanResult
        }
        viewModel.scanResults.observe(viewLifecycleOwner) { scanResults ->
            scanResultsAdapter.scanResults = scanResults
                .filter { it.ssid != deviceSsid }
                .sortedByDescending { it.exactLevel }
                .sortedBy { it.freq }
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
            viewModel.nextPage.value = true
            Log.d(tag, "WifiScanFragment fab clicked")
        }
        viewModel.scanResultSelected.observe(viewLifecycleOwner) {
            Log.d(tag, "wifi scan result with ssid '${it.ssid}' was clicked")
        }

        return inflater.inflate(R.layout.wifi_scan_fragment, container, false)
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
        Log.d(tag, "WifiScanFragment resumed")
    }

    override fun onPause() {
        super.onPause()
        viewModel.isVisible.value = false
        Log.d(tag, "WifiScanFragment paused")
    }
}