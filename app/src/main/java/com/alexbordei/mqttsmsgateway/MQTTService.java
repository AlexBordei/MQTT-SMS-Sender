package com.alexbordei.mqttsmsgateway;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.util.Log;

import androidx.annotation.Nullable;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MQTTService extends Service {

    String phoneNo;
    String message;
    MqttCallback mqttCallback;
    MqttClient mqttClient;
    MQTTHelper mqttHelper;

    int qos             =  1;
    String pubID        = "AndroidAPP";
    String broker       = "tcp://broker.hivemq.com:1883";
    String topic        = "/panel/sms";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Context context = this;
        Log.d("MQTTService", "Start");
        mqttHelper = MQTTHelper.getInstance();
        mqttHelper.setInstanceData(mqttClient, broker, pubID);
        mqttHelper.connect();

        mqttCallback = new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                mqttHelper.connect();
            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                String[] data = mqttMessage.toString().split("#");
                if(data.length == 3) {
                    Log.d("MQTT", "message is here: " + mqttMessage);
                    Integer id = Integer.parseInt(data[0]);
                    phoneNo = data[1];
                    message = data[2];
                    try {
                        SMSHandler smsHandler = new SMSHandler(getApplicationContext());
                        smsHandler.sendSMS(phoneNo, message);
                        Thread.sleep(5000);
                        if(smsHandler.getSentStatus()) {
                            Log.d("MQTT", "Message Sent");
                            HTTPHelper $httpHelper;
                            $httpHelper = new HTTPHelper(getApplicationContext());
                            $httpHelper.sendSMSStatus(id, "sent", "");
                        } else {
                            Log.d("MQTT", smsHandler.getError());
                            HTTPHelper $httpHelper;
                            $httpHelper = new HTTPHelper(getApplicationContext());
                            $httpHelper.sendSMSStatus(id, "error", smsHandler.getError());
                        }

                    } catch (Exception ex) {
                        Log.e("MQTT", ex.getMessage());
                        ex.printStackTrace();
                        HTTPHelper $httpHelper;
                        $httpHelper = new HTTPHelper(getApplicationContext());
                        $httpHelper.sendSMSStatus(id, "error", ex.getMessage());
                    }
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        };
        try {
            mqttHelper.subscribe(topic, qos, mqttCallback);
        } catch (MqttException e) {
            e.printStackTrace();
        }
        return START_STICKY;
    }

    @Override

    public void onDestroy() {
        super.onDestroy();
        Log.d("MQTTService", "Destroy");
        mqttHelper = MQTTHelper.getInstance();
        try {
            mqttHelper.disconnect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
        // stopping the process

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
