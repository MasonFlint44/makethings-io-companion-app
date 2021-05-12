package makethings.io.fragments.devicescan

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import makethings.io.R
import makethings.io.wifi.WifiFreq
import makethings.io.wifi.WifiScanResult

class DeviceScanAdapter(
    private val onItemClicked: (WifiScanResult) -> Unit
): RecyclerView.Adapter<DeviceScanAdapter.ViewHolder>() {
    inner class ViewHolder(
        view: View,
        onItemClicked: (Int) -> Unit
    ): RecyclerView.ViewHolder(view) {
        val scanResultLayout: LinearLayout = itemView.findViewById(R.id.scanResultLayout)
        val scanResultText: TextView = itemView.findViewById(R.id.scanResultText)

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
    }

    override fun getItemCount(): Int {
        return scanResults.count()
    }
}