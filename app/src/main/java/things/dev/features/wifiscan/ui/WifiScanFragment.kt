package things.dev.features.wifiscan.ui

import android.content.Context
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
import jp.wasabeef.recyclerview.animators.OvershootInRightAnimator
import things.dev.R

class WifiScanFragment : Fragment() {
    private val deviceSsid = "things.dev"
    private val viewModel: WifiScanViewModel by activityViewModels()
    private lateinit var scanResultsView: RecyclerView
    private lateinit var wifiScanProgress: ProgressBar
    private lateinit var scanResultsAdapter: WifiScanAdapter

    override fun onAttach(context: Context) {
        super.onAttach(context)
        scanResultsAdapter = WifiScanAdapter(context) { scanResult ->
            viewModel.scanResultSelected.value = scanResult
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel.scanResults.observe(viewLifecycleOwner, { scanResults ->
            scanResultsAdapter.scanResults = scanResults
                .filter { it.ssid != deviceSsid}
                .sortedByDescending { it.exactLevel }
                .sortedBy { it.freq }
        })
        viewModel.loading.observe(viewLifecycleOwner, {
            wifiScanProgress.visibility = if (it == true) View.VISIBLE else View.GONE
        })
        viewModel.scanResultSelected.observe(viewLifecycleOwner, {
            Log.d(tag, "wifi scan result with ssid '${it.ssid}' was clicked")
        })

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