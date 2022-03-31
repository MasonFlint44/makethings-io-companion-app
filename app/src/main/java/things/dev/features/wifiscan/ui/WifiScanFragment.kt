package things.dev.features.wifiscan.ui

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import jp.wasabeef.recyclerview.animators.OvershootInRightAnimator
import things.dev.R
import things.dev.features.wifi.WifiService
import javax.inject.Inject

@AndroidEntryPoint
class WifiScanFragment : Fragment() {
    private val deviceSsid = "things.dev"
    private val viewModel: WifiScanViewModel by activityViewModels()
    private lateinit var scanResultsView: RecyclerView
    private lateinit var wifiScanProgress: ProgressBar
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
        viewModel.loading.observe(viewLifecycleOwner) {
            wifiScanProgress.visibility = if (it == true) View.VISIBLE else View.GONE
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
    }
}