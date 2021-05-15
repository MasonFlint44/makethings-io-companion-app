package makethings.io.fragments.devicescan

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import makethings.io.R
import makethings.io.wifi.WifiFreq
import makethings.io.wifi.WifiLevel
import makethings.io.wifi.WifiScanResult

class DeviceScanAdapter(
    private val context: Context,
    private val onItemClicked: (WifiScanResult) -> Unit
): RecyclerView.Adapter<DeviceScanAdapter.ViewHolder>() {
    inner class ViewHolder(
        view: View,
        onItemClicked: (Int) -> Unit
    ): RecyclerView.ViewHolder(view) {
        val scanResultLayout: LinearLayout = itemView.findViewById(R.id.scanResultLayout)
        val scanResultText: TextView = itemView.findViewById(R.id.scanResultText)
        val scanResultLevel: ImageView = itemView.findViewById(R.id.scanResultLevel)

        init {
            itemView.setOnClickListener {
                onItemClicked(adapterPosition)
            }
        }
    }

    var scanResults: List<WifiScanResult> = emptyList()
        set(value) {
            notifyItemRangeRemoved(0, field.count())
            field = value
            notifyItemRangeInserted(0, value.count())
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceScanAdapter.ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val scanResultView = inflater.inflate(R.layout.device_scan_result, parent, false)
        return ViewHolder(scanResultView) {
            onItemClicked(scanResults[it])
        }
    }

    override fun onBindViewHolder(holder: DeviceScanAdapter.ViewHolder, position: Int) {
        val scanResult = scanResults[position]
        holder.scanResultText.text = scanResult.ssid
        when(scanResult.freq) {
            WifiFreq.FREQ_2_4_GHZ -> {
                holder.scanResultLayout.isClickable = true
                holder.scanResultText.isEnabled = true
            }
            else -> {
                holder.scanResultLayout.isClickable = false
                holder.scanResultText.isEnabled = false
            }
        }
        val wifiLevelDrawable = when(scanResult.level) {
            WifiLevel.NONE -> ContextCompat.getDrawable(context, R.drawable.twotone_signal_wifi_0_bar_24)
            WifiLevel.WEAK -> ContextCompat.getDrawable(context, R.drawable.twotone_signal_wifi_1_bar_24)
            WifiLevel.FAIR -> ContextCompat.getDrawable(context, R.drawable.twotone_signal_wifi_2_bar_24)
            WifiLevel.GOOD -> ContextCompat.getDrawable(context, R.drawable.twotone_signal_wifi_3_bar_24)
            WifiLevel.EXCELLENT -> ContextCompat.getDrawable(context, R.drawable.twotone_signal_wifi_4_bar_24)
            else -> ContextCompat.getDrawable(context, R.drawable.twotone_signal_wifi_0_bar_24)
        }
        holder.scanResultLevel.setImageDrawable(wifiLevelDrawable)
    }

    override fun getItemCount(): Int {
        return scanResults.count()
    }
}