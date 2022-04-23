package things.dev.features.wifiutils

import kotlinx.coroutines.flow.Flow
import things.dev.features.oldwifi.framework.models.WifiScanResult

class WifiNetwork()

interface WifiUtils {
    // NOTE: consider making this a flow - also consider coroutines
    // TODO: this one is basically WifiService.startScan
    val networks: List<WifiNetwork>
    // NOTE: consider making this a flow - also consider coroutines
    // TODO: see WifiService.getCurrentNetworkInfo
    val currentNetwork: List<WifiNetwork>
    // TODO: see WifiService.requestNetwork
    fun tryConnect()
}