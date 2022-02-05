package com.alexbordei.mqttsmsgateway;

import android.util.Log;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MQTTHelper {
    private static MQTTHelper instance = null;
    MqttClient mqttClient;
    String broker;
    String pubID;

    private MQTTHelper(MqttClient MQTTClient, String broker, String pubID) {
        this.mqttClient = MQTTClient;
        this.broker = broker;
        this.pubID = pubID;
    }

    public static MQTTHelper getInstance(MqttClient MQTTClient, String broker, String pubID)
    {
        if (instance == null) {
            instance = new MQTTHelper(MQTTClient, broker, pubID);
        }
        return instance;
    }

    public void connect() {
        MemoryPersistence persistence = new MemoryPersistence();
        try {
            mqttClient = new MqttClient(broker, pubID, persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(false);
            connOpts.setConnectionTimeout(60);
            connOpts.setKeepAliveInterval(60);
            connOpts.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1);
            Log.d("MQTT", "Connecting to broker: " + broker);
            mqttClient.connect(connOpts);
            Log.d("MQTT", "Connected");
        }  catch(MqttException ex) {
            Log.d("MQTT","Reason :"+ ex.getReasonCode());
            Log.d("MQTT","Message :"+ ex.getMessage());
            Log.d("MQTT","Local :"+ ex.getLocalizedMessage());
            Log.d("MQTT","Cause :"+ ex.getCause());
            Log.d("MQTT","Exception :"+ ex);
            ex.printStackTrace();
        }
    }

    public void subscribe(String topic, int qos, MqttCallback callback) throws MqttException {
        mqttClient.subscribe(topic, qos);

        mqttClient.setCallback(callback);
    }

    public void unsubscribe(String topic) throws MqttException {
        mqttClient.unsubscribe(topic);
    }

    public boolean isConnected() {
        return mqttClient.isConnected();
    }

    public void disconnect() throws MqttException {
        mqttClient.disconnect();
    }

}
