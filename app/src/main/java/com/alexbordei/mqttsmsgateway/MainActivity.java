package com.alexbordei.mqttsmsgateway;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MainActivity extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 0;

    String phoneNo;
    String message;
    MqttCallback mqttCallback;
    MqttClient mqttClient;
    MQTTHelper mqttHelper;

    int qos             =  0;
    String pubID        = "AndroidAPP";
    String broker       = "tcp://broker.hivemq.com:1883";
    String topic        = "/panel/sms";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mqttHelper = new MQTTHelper(mqttClient, broker, pubID);
        mqttHelper.connect();
        mqttCallback = new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                mqttHelper.connect();
            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                String[] data = mqttMessage.toString().split("#");
                if(data.length == 2) {
                    Log.d("MQTT", "message is here: " + mqttMessage);
                    phoneNo = data[0];
                    message = data[1];
                    sendSMSMessage();
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
    }

    public void sendSMSMessage() {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.SEND_SMS)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.SEND_SMS},
                        MY_PERMISSIONS_REQUEST_SEND_SMS);
            }
        }
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNo, null, message, null, null);
            Log.d("MQTT", "Message Sent");

        } catch (Exception ex) {
            Log.e("MQTT", ex.getMessage());
            ex.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_SEND_SMS: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(phoneNo, null, message, null, null);
                    Log.d("MQTT", "SMS sent.");
                } else {
                    Log.d("MQTT", "SMS failed, please try again.");
                }
            }
        }
    }
}