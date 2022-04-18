package things.dev.features.wifilogin.ui

import android.net.Network
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.*
import things.dev.R
import things.dev.features.oldwifi.framework.WifiService
import things.dev.features.oldwifi.framework.models.WifiFreq
import things.dev.features.oldwifi.framework.models.WifiLevel
import things.dev.features.oldwifi.framework.models.WifiScanResult
import things.dev.features.oldwifi.framework.models.WifiSecurity
import javax.inject.Inject

class WifiLoginFragment @Inject constructor() : Fragment() {
    private val viewModel: WifiLoginViewModel by activityViewModels()
    private lateinit var passwordText: TextView
    private lateinit var scanResultText: TextView
    private lateinit var scanResultFreq: TextView
    private lateinit var scanResultLevel: ImageView
    private lateinit var loadingSpinner: ProgressBar
    @Inject lateinit var wifiService: WifiService

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel.scanResult.observe(viewLifecycleOwner) {
            scanResultText.text = it.ssid
            scanResultFreq.text = when (it.freq) {
                WifiFreq.FREQ_2_4_GHZ -> "2.4 GHz"
                WifiFreq.FREQ_5_GHZ -> "5 GHz"
                else -> "??? GHz"
            }

            val wifiLevelDrawable = when {
                it.level == WifiLevel.NONE ->
                    ResourcesCompat.getDrawable(
                        requireActivity().resources,
                        R.drawable.twotone_signal_wifi_0_bar_24,
                        null
                    )
                it.level == WifiLevel.WEAK && it.security != WifiSecurity.OPEN ->
                    ResourcesCompat.getDrawable(
                        requireActivity().resources,
                        R.drawable.twotone_signal_wifi_1_bar_lock_24,
                        null
                    )
                it.level == WifiLevel.FAIR && it.security != WifiSecurity.OPEN ->
                    ResourcesCompat.getDrawable(
                        requireActivity().resources,
                        R.drawable.twotone_signal_wifi_2_bar_lock_24,
                        null
                    )
                it.level == WifiLevel.GOOD && it.security != WifiSecurity.OPEN ->
                    ResourcesCompat.getDrawable(
                        requireActivity().resources,
                        R.drawable.twotone_signal_wifi_3_bar_lock_24,
                        null
                    )
                it.level == WifiLevel.EXCELLENT && it.security != WifiSecurity.OPEN ->
                    ResourcesCompat.getDrawable(
                        requireActivity().resources,
                        R.drawable.twotone_signal_wifi_4_bar_lock_24,
                        null
                    )
                it.level == WifiLevel.WEAK && it.security == WifiSecurity.OPEN ->
                    ResourcesCompat.getDrawable(
                        requireActivity().resources,
                        R.drawable.twotone_signal_wifi_1_bar_24,
                        null
                    )
                it.level == WifiLevel.FAIR && it.security == WifiSecurity.OPEN ->
                    ResourcesCompat.getDrawable(
                        requireActivity().resources,
                        R.drawable.twotone_signal_wifi_2_bar_24,
                        null
                    )
                it.level == WifiLevel.GOOD && it.security == WifiSecurity.OPEN ->
                    ResourcesCompat.getDrawable(
                        requireActivity().resources,
                        R.drawable.twotone_signal_wifi_3_bar_24,
                        null
                    )
                it.level == WifiLevel.EXCELLENT && it.security == WifiSecurity.OPEN ->
                    ResourcesCompat.getDrawable(
                        requireActivity().resources,
                        R.drawable.twotone_signal_wifi_4_bar_24,
                        null
                    )
                else -> ResourcesCompat.getDrawable(
                    requireActivity().resources,
                    R.drawable.twotone_signal_wifi_0_bar_24,
                    null
                )
            }
            scanResultLevel.setImageDrawable(wifiLevelDrawable)
        }
        viewModel.loading.observe(viewLifecycleOwner) {
            when (it) {
                true -> {
                    loadingSpinner.visibility = View.VISIBLE
                    scanResultLevel.visibility = View.GONE
                }
                else -> {
                    loadingSpinner.visibility = View.GONE
                    scanResultLevel.visibility = View.VISIBLE
                }
            }
        }
        viewModel.fabClicked.observe(viewLifecycleOwner) onFabClicked@ {
            if (viewModel.isVisible.value != true) { return@onFabClicked }
            viewModel.nextPage.value = true

            when (viewModel.networkConnected.value) {
                null -> {
                    viewModel.scanResult.value?.let { scanResult ->
                        viewModel.loading.value = true
                        connectToNetwork(scanResult, "")
                            .onEach { viewModel.networkConnected.value = scanResult }
                            .launchIn(lifecycleScope)
                    }
                }
                else -> {
                    viewModel.nextPage.value = true
                }
            }
            Log.d(tag, "WifiLoginFragment fab clicked")
        }
        viewModel.networkConnected.observe(viewLifecycleOwner) {
            viewModel.loading.value = false
        }

        return inflater.inflate(R.layout.wifi_login_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        passwordText = view.findViewById(R.id.passwordText)
        scanResultText = view.findViewById(R.id.scanResultText)
        scanResultFreq = view.findViewById(R.id.scanResultFreq)
        scanResultLevel = view.findViewById(R.id.scanResultLevel)
        loadingSpinner = view.findViewById(R.id.loadingSpinner)
        
        passwordText.doOnTextChanged { text, start, count, after ->
            viewModel.password.value = text.toString()
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.isVisible.value = true
    }

    override fun onPause() {
        super.onPause()
        viewModel.isVisible.value = false
    }

    private fun connectToNetwork(network: WifiScanResult, password: String): Flow<Network> {
        var callbacks = wifiService.requestNetwork(
            network.ssid,
            password = password,
            security = network.security,
            isLocal = true
        )
        return callbacks.available
            .take(1)
            .map { it.network }
            .onCompletion { callbacks.unregister() }
    }
}