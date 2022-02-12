package com.alexbordei.mqttsmsgateway;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import static android.app.Notification.*;

import com.fasterxml.jackson.databind.ObjectMapper; // version 2.11.1
import com.fasterxml.jackson.annotation.JsonProperty; // version 2.11.1

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public class SMSThreadService extends Service {

    Thread thread;
    boolean stopped = false;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        thread = new Thread(
                () -> {
                    while (true) {
                        if (!stopped) {
                            Log.e("SMSGateway", "SMS Queue check");

                            //updating the service status
                            HTTPHelper httpHelper = new HTTPHelper(this);
                            httpHelper.isResponseReady = false;
                            httpHelper.getSMSQueue();

                            while (!httpHelper.isResponseReady) {
                            }

                            try {
                                JSONArray jsonArray = new JSONArray(httpHelper.getResponse());

                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                                    Log.d("SMSGateway", (String) jsonObject.get("from"));
                                    Integer id = jsonObject.getInt("id");
                                    String phoneNo = (String) jsonObject.get("to");
                                    String message = (String) jsonObject.get("message");
                                    HTTPHelper $httpHelper;
                                    $httpHelper = new HTTPHelper(getApplicationContext());

                                    new Thread(() -> {
                                        try {
                                            SMSHandler smsHandler = new SMSHandler(getApplicationContext());
                                            smsHandler.sendSMS(phoneNo, message);
                                            Thread.sleep(2000);
                                            if (smsHandler.getSentStatus()) {
                                                Log.d("SMSGateway", "Message Sent");
                                                $httpHelper.sendSMSStatus(id, "sent", "");
                                            } else {
                                                Log.d("SMSGateway", "Error" + smsHandler.getError());
                                                $httpHelper.sendSMSStatus(id, "error", smsHandler.getError());
                                            }

                                        } catch (Exception ex) {
                                            Log.e("SMSGateway", ex.getMessage());
                                            ex.printStackTrace();
                                            $httpHelper.sendSMSStatus(id, "error", ex.getMessage());
                                        }
                                    }).start();
                                }
                            } catch (JSONException err) {
                                Log.d("Error", err.toString());
                            }

                            httpHelper.updateSMSServiceStatus();

                            try {
                                Thread.sleep(10000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                        } else {
                            stopForeground(true);
                        }
                    }
                }
        );

        if (!stopped) {
            thread.start();
        }

        final String CHANNELID = "SMS Gateway";
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
        stopped = true;

        Log.d("SMSGateway", "Service stopped");

        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
