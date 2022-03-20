package things.dev.features.mqttbroker.data

import mqtt.broker.Broker

interface MqttBroker {
    fun startBroker(): Broker
    fun publishMessage(payload: String, topic: String, retain: Boolean = false)
    fun stopBroker(broker: Broker)
}