package things.dev.fragments.devicescan

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import things.dev.R
import things.dev.wifi.WifiFreq
import things.dev.wifi.WifiLevel
import things.dev.wifi.WifiScanResult

class DeviceScanAdapter(
    private val context: Context,
    private val onItemClicked: (WifiScanResult, Int) -> Unit
): RecyclerView.Adapter<DeviceScanAdapter.ViewHolder>() {
    inner class ViewHolder(
        view: View,
        onItemClicked: (Int) -> Unit
    ): RecyclerView.ViewHolder(view) {
        val scanResultLayout: LinearLayout = itemView.findViewById(R.id.scanResultLayout)
        val scanResultText: TextView = itemView.findViewById(R.id.scanResultText)
        val scanResultLevel: ImageView = itemView.findViewById(R.id.scanResultLevel)
        val loadingSpinner: ProgressBar = itemView.findViewById(R.id.loadingSpinner)

        init {
            itemView.setOnClickListener {
                notifyItemChanged(selectedPosition)
                selectedPosition = adapterPosition
                notifyItemChanged(adapterPosition)

                onItemClicked(adapterPosition)
            }
        }

        fun setLoading(isLoading: Boolean) {
            when(isLoading) {
                true -> {
                    scanResultLevel.visibility = View.GONE
                    loadingSpinner.visibility = View.VISIBLE
                }
                false -> {
                    scanResultLevel.visibility = View.VISIBLE
                    loadingSpinner.visibility = View.GONE
                }
            }
        }

        fun enable() {
            scanResultLayout.isEnabled = true
            scanResultText.isEnabled = true
            scanResultLevel.isEnabled = true
        }

        fun disable() {
            scanResultLayout.isEnabled = false
            scanResultText.isEnabled = false
            scanResultLevel.isEnabled = false
        }
    }

    private var selectedPosition = RecyclerView.NO_POSITION
    private val isItemLoading = mutableMapOf<Int, Boolean>()
    var scanResults: List<WifiScanResult> = emptyList()
        set(value) {
            notifyItemRangeRemoved(0, field.count())
            field = value
            notifyItemRangeInserted(0, value.count())
        }

    fun setLoading(position: Int, isLoading: Boolean) {
        isItemLoading[position] = isLoading
        notifyItemChanged(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val scanResultView = inflater.inflate(R.layout.device_scan_result, parent, false)
        return ViewHolder(scanResultView) {
            onItemClicked(scanResults[it], it)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val scanResult = scanResults[position]
        holder.scanResultText.text = scanResult.ssid
        when(scanResult.freq) {
            WifiFreq.FREQ_2_4_GHZ -> {
                holder.enable()
            }
            else -> {
                holder.disable()
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
        holder.itemView.isSelected = selectedPosition == position

        val isLoading = isItemLoading[position] ?: false
        holder.setLoading(isLoading)
    }

    override fun getItemCount(): Int {
        return scanResults.count()
    }
}