package things.dev.features.wifi.domain

import android.net.Network
import kotlinx.coroutines.flow.*
import things.dev.features.wifi.WifiService
import things.dev.features.wifi.data.models.WifiScanResult
import javax.inject.Inject

class WifiServiceHelper @Inject constructor(private val wifiService: WifiService) {
    private fun connectToDevice(device: WifiScanResult, onAvailable: () -> Unit): Flow<Network> {
        var callbacks = wifiService.requestNetwork(
            device.ssid,
            security = device.security,
            isLocal = true
        )
        return callbacks.available
            .take(1)
            .map { it.network }
            .onCompletion { callbacks.unregister() }
    }

    private fun connectToNetwork(network: WifiScanResult, password: String, onAvailable: () -> Unit): Flow<Network> {
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