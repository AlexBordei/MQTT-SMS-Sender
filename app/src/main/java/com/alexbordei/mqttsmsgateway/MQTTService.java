package com.alexbordei.mqttsmsgateway;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import static android.app.Notification.*;

public class MQTTService extends Service {

    String phoneNo;
    String message;
    MqttCallback mqttCallback;
    MqttClient mqttClient;
    MQTTHelper mqttHelper;
    Thread thread;
    boolean firstMesageSent = false;
    boolean stopped = false;
    int qos             =  1;
    String pubID        = "AndroidAPP";
    String broker       = "tcp://broker.hivemq.com:1883";
    String topic        = "/panel/36311a2d-361a-42c6-a5e8-d60c85d3a4be/sms";

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Context context = this;
        mqttCallback = new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                mqttHelper.connect();
            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                if(firstMesageSent) {
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
                            if (smsHandler.getSentStatus()) {
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
                } else {
                    firstMesageSent = true;
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        };

        thread = new Thread(
                () -> {
                    while (true) {
                        mqttHelper = MQTTHelper.getInstance(mqttClient, broker, pubID);
                        if(!stopped) {
                            Log.e("MQTT", "MQTT service check");

                            try {
                                if (mqttHelper.isConnected()) {
                                    Log.d("MQTT", "MQTT service is up");
                                    HTTPHelper httpHelper = new HTTPHelper(this);
                                    httpHelper.updateSMSServiceStatus();
                                } else {
                                    Log.d("MQTT", "MQTT service is down");
                                    mqttHelper.connect();
                                    try {
                                        mqttHelper.subscribe(topic, qos, mqttCallback);
                                    } catch (MqttException e) {
                                        e.printStackTrace();
                                    }
                                }
                            } catch (Exception ex) {
                                mqttHelper.connect();
                                try {
                                    mqttHelper.subscribe(topic, qos, mqttCallback);
                                } catch (MqttException e) {
                                    e.printStackTrace();
                                }

                            }

                            try {
                                Thread.sleep(10000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        } else {
                            try {
                                mqttHelper.unsubscribe(topic);
                                stopForeground(true);
                            } catch (MqttException e) {
                                e.printStackTrace();
                            }

                        }
                    }
                }
        );
        if(!stopped) {
            thread.start();
        }
        final String CHANNELID = "MQTT CLient";
        NotificationChannel channel = new NotificationChannel(CHANNELID, CHANNELID, NotificationManager.IMPORTANCE_LOW);
        getSystemService(NotificationManager.class).createNotificationChannel(channel);
        Builder notification = new Builder(this, CHANNELID)
                .setContentText("Service is running")
                .setContentTitle("SERVICE ENABLED")
                .setSmallIcon(R.drawable.ic_launcher_background);

        startForeground(1001, notification.build());
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        stopped=true;
        Log.d("MQTTService", "Service stopped");

        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
