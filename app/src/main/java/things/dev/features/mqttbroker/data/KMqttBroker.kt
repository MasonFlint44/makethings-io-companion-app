package things.dev.features.mqttbroker.data

import android.util.Log
import androidx.lifecycle.LifecycleCoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mqtt.broker.Broker
import mqtt.packets.Qos
import mqtt.packets.mqttv5.MQTT5Properties
import javax.inject.Inject

class KMqttBroker @Inject constructor(
    private val broker: Broker,
    private val lifecycleScope: LifecycleCoroutineScope
    ): MqttBroker {

    private val tag = "MqttBroker"

    override fun startBroker(): Broker {
        lifecycleScope.launch(Dispatchers.IO) {
            Log.d(tag, "Starting MQTT broker...")
            broker.listen()
        }
        return broker
    }

    override fun publishMessage(payload: String, topic: String, retain: Boolean) {
        broker?.publish(retain, topic, Qos.AT_LEAST_ONCE, MQTT5Properties(), payload.toByteArray().toUByteArray())
    }

    override fun stopBroker(broker: Broker) {
        broker.stop()
        Log.d(tag, "Stopping MQTT broker")
    }
}