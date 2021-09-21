package makethings.io.fragments.devicescan

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import jp.wasabeef.recyclerview.animators.OvershootInRightAnimator
import makethings.io.R
import makethings.io.wifi.WifiFreq
import makethings.io.wifi.WifiScanResult

// TODO: add signal strength icons
// TODO: add wifi security to ui
// TODO: might want to make scanResultsView's height match_parent - recyclerview doesn't take up entire area of fragment
class DeviceScanFragment : Fragment() {
    private val deviceSsid = "makethings-io"
    private val viewModel: DeviceScanViewModel by activityViewModels()
    private lateinit var scanResultsView: RecyclerView
    private lateinit var wifiScanProgress: ProgressBar
    private lateinit var emptyResultsMessage: TextView
    private lateinit var scanResultsAdapter: DeviceScanAdapter

    override fun onAttach(context: Context) {
        super.onAttach(context)

        scanResultsAdapter = DeviceScanAdapter(context) { scanResult ->
            viewModel.deviceClicked.value = scanResult
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel.scanResults.observe(viewLifecycleOwner, { scanResults ->
            val results = scanResults.filter { it.ssid == deviceSsid && it.freq == WifiFreq.FREQ_2_4_GHZ }
            results.sortedByDescending { it.exactLevel }
            scanResultsAdapter.scanResults = results
            when {
                results.isEmpty() -> {
                    emptyResultsMessage.visibility = View.VISIBLE
                    scanResultsView.visibility = View.GONE
                }
                else -> {
                    emptyResultsMessage.visibility = View.GONE
                    scanResultsView.visibility = View.VISIBLE
                }
            }
        })
        viewModel.loading.observe(viewLifecycleOwner, {
            when(it) {
                true -> {
                    wifiScanProgress.visibility = View.VISIBLE
                    emptyResultsMessage.visibility = View.GONE
                }
                else -> {
                    wifiScanProgress.visibility = View.GONE
                }
            }
        })
        viewModel.deviceClicked.observe(viewLifecycleOwner, {
            Log.d(tag, "device with ssid '${it.ssid}' was clicked")
        })

        return inflater.inflate(R.layout.device_scan_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        scanResultsView = view.findViewById(R.id.scanResultsView)
        scanResultsView.adapter = scanResultsAdapter
        scanResultsView.layoutManager = LinearLayoutManager(activity)
        scanResultsView.itemAnimator = OvershootInRightAnimator()

        wifiScanProgress = view.findViewById(R.id.wifiScanProgress)
        emptyResultsMessage = view.findViewById(R.id.emtpyResultsMessage)
    }

}