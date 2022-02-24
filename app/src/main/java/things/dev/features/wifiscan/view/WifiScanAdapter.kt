package things.dev.features.wifiscan.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import things.dev.R
import things.dev.features.wifi.domain.models.WifiFreq
import things.dev.features.wifi.domain.models.WifiLevel
import things.dev.features.wifi.domain.models.WifiScanResult
import things.dev.features.wifi.domain.models.WifiSecurity

class WifiScanAdapter (
    private val context: Context,
    private val onItemClicked: (WifiScanResult) -> Unit
): RecyclerView.Adapter<WifiScanAdapter.ViewHolder>() {
    inner class ViewHolder(
            view: View,
            onItemClicked: (Int) -> Unit
    ): RecyclerView.ViewHolder(view) {
        val scanResultLayout: LinearLayout = itemView.findViewById(R.id.scanResultLayout)
        val scanResultText: TextView = itemView.findViewById(R.id.scanResultText)
        val scanResultFreq: TextView = itemView.findViewById(R.id.scanResultFreq)
        val scanResultLevel: ImageView = itemView.findViewById(R.id.scanResultLevel)

        init {
            itemView.setOnClickListener {
                notifyItemChanged(selectedPosition)
                selectedPosition = adapterPosition
                notifyItemChanged(adapterPosition)

                onItemClicked(adapterPosition)
            }
        }

        fun enable() {
            scanResultLayout.isEnabled = true
            scanResultText.isEnabled = true
            scanResultFreq.isEnabled = true
            scanResultLevel.isEnabled = true
        }

        fun disable() {
            scanResultLayout.isEnabled = false
            scanResultText.isEnabled = false
            scanResultFreq.isEnabled = false
            scanResultLevel.isEnabled = false
        }
    }

    private var selectedPosition = RecyclerView.NO_POSITION
    var scanResults: List<WifiScanResult> = emptyList()
        set(value) {
            notifyItemRangeRemoved(0, field.count())
            field = value
            notifyItemRangeInserted(0, value.count())
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val scanResultView = inflater.inflate(R.layout.wifi_scan_result, parent, false)
        return ViewHolder(scanResultView) {
            onItemClicked(scanResults[it])
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val scanResult = scanResults[position]
        holder.scanResultText.text = scanResult.ssid
        holder.scanResultFreq.text = when(scanResult.freq) {
            WifiFreq.FREQ_2_4_GHZ -> "2.4 GHz"
            WifiFreq.FREQ_5_GHZ -> "5 GHz"
            else -> "??? GHz"
        }
        when(scanResult.freq) {
            WifiFreq.FREQ_2_4_GHZ -> {
                holder.enable()
            }
            else -> {
                holder.disable()
            }
        }

        val wifiLevelDrawable = when {
            scanResult.level == WifiLevel.NONE ->
                ContextCompat.getDrawable(context, R.drawable.twotone_signal_wifi_0_bar_24)
            scanResult.level == WifiLevel.WEAK && scanResult.security != WifiSecurity.OPEN ->
                ContextCompat.getDrawable(context, R.drawable.twotone_signal_wifi_1_bar_lock_24)
            scanResult.level == WifiLevel.FAIR && scanResult.security != WifiSecurity.OPEN ->
                ContextCompat.getDrawable(context, R.drawable.twotone_signal_wifi_2_bar_lock_24)
            scanResult.level == WifiLevel.GOOD && scanResult.security != WifiSecurity.OPEN ->
                ContextCompat.getDrawable(context, R.drawable.twotone_signal_wifi_3_bar_lock_24)
            scanResult.level == WifiLevel.EXCELLENT && scanResult.security != WifiSecurity.OPEN ->
                ContextCompat.getDrawable(context, R.drawable.twotone_signal_wifi_4_bar_lock_24)
            scanResult.level == WifiLevel.WEAK && scanResult.security == WifiSecurity.OPEN ->
                ContextCompat.getDrawable(context, R.drawable.twotone_signal_wifi_1_bar_24)
            scanResult.level == WifiLevel.FAIR && scanResult.security == WifiSecurity.OPEN ->
                ContextCompat.getDrawable(context, R.drawable.twotone_signal_wifi_2_bar_24)
            scanResult.level == WifiLevel.GOOD && scanResult.security == WifiSecurity.OPEN ->
                ContextCompat.getDrawable(context, R.drawable.twotone_signal_wifi_3_bar_24)
            scanResult.level == WifiLevel.EXCELLENT && scanResult.security == WifiSecurity.OPEN ->
                ContextCompat.getDrawable(context, R.drawable.twotone_signal_wifi_4_bar_24)
            else -> ContextCompat.getDrawable(context, R.drawable.twotone_signal_wifi_0_bar_24)
        }
        holder.scanResultLevel.setImageDrawable(wifiLevelDrawable)
        holder.itemView.isSelected = selectedPosition == position
    }

    override fun getItemCount(): Int {
        return scanResults.count()
    }
}