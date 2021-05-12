package makethings.io.fragments.wifiscan

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import jp.wasabeef.recyclerview.animators.OvershootInRightAnimator
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import makethings.io.R
import makethings.io.wifi.WifiScanResult
import makethings.io.wifi.WifiService

class WifiScanFragment : Fragment() {
    private val model: WifiScanResultViewModel by activityViewModels()
    private lateinit var wifiService: WifiService
    private lateinit var scanResultsView: RecyclerView
    private lateinit var wifiScanProgress: ProgressBar
    private lateinit var scanResultsAdapter: WifiScanResultsAdapter

    override fun onAttach(context: Context) {
        super.onAttach(context)
        wifiService = WifiService(context.applicationContext)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        model.scanResults.observe(viewLifecycleOwner, Observer {
            scanResultsAdapter.scanResults = it
        })

        return inflater.inflate(R.layout.wifi_scan_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        scanResultsView = view.findViewById(R.id.scanResultsView)
        wifiScanProgress = view.findViewById(R.id.wifiScanProgress)

        scanResultsAdapter = WifiScanResultsAdapter { scanResult -> onWifiScanResultClicked(scanResult) }
        scanResultsView.adapter = scanResultsAdapter
        scanResultsView.layoutManager = LinearLayoutManager(activity)
        scanResultsView.itemAnimator = OvershootInRightAnimator()
    }

    override fun onResume() {
        super.onResume()
        ensurePermission(Manifest.permission.ACCESS_FINE_LOCATION)
        startWifiScan()
    }

    private fun onWifiScanResultClicked(result: WifiScanResult) {
        Log.d(tag, "ssid: ${result.ssid} was clicked")
    }

    private fun startWifiScan() {
        model.scanResults.value = emptyList()
        wifiScanProgress.visibility = View.VISIBLE
        wifiService.startScan()
            .take(1)
            .onEach { results -> onWifiScanComplete(results) }
            .launchIn(lifecycleScope)
    }

    private fun onWifiScanComplete(results: List<WifiScanResult>) {
        Log.d(tag,"Completed wifi network scan")
        wifiScanProgress.visibility = View.GONE
        model.scanResults.value = results
    }

    private fun ensurePermission(permission: String) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) { return }
        if (context?.let { checkSelfPermission(it, permission) } == PackageManager.PERMISSION_GRANTED) { return }
        requestPermissions(arrayOf(permission), 87)
    }
}