package makethings.io.fragments.wifiscan

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import jp.wasabeef.recyclerview.animators.OvershootInRightAnimator
import makethings.io.R
import makethings.io.wifi.WifiService

class WifiScanFragment : Fragment() {
    private val viewModel: WifiScanViewModel by activityViewModels()
    private lateinit var wifiService: WifiService
    private lateinit var scanResultsView: RecyclerView
    private lateinit var wifiScanProgress: ProgressBar
    private lateinit var scanResultsAdapter: WifiScanAdapter

    override fun onAttach(context: Context) {
        super.onAttach(context)
        wifiService = WifiService(context.applicationContext)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel.scanResults.observe(viewLifecycleOwner, Observer { scanResults ->
            scanResults.sortedByDescending { it.exactLevel }
            scanResultsAdapter.scanResults = scanResults
        })
        viewModel.loading.observe(viewLifecycleOwner, Observer {
            wifiScanProgress.visibility = if (it == true) View.VISIBLE else View.GONE
        })
        viewModel.scanResultClicked.observe(viewLifecycleOwner, Observer {
            Log.d(tag, "wifi scan result with ssid '${it.ssid}' was clicked")
        })

        return inflater.inflate(R.layout.wifi_scan_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        scanResultsView = view.findViewById(R.id.scanResultsView)
        wifiScanProgress = view.findViewById(R.id.wifiScanProgress)

        scanResultsAdapter = WifiScanAdapter { scanResult ->
            viewModel.scanResultClicked.value = scanResult
        }
        scanResultsView.adapter = scanResultsAdapter
        scanResultsView.layoutManager = LinearLayoutManager(activity)
        scanResultsView.itemAnimator = OvershootInRightAnimator()
    }
}