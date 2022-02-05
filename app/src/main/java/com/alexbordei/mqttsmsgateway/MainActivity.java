package com.alexbordei.mqttsmsgateway;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MainActivity extends AppCompatActivity implements  View.OnClickListener {

    // declaring objects of Button class
    private Button start, stop;
    private boolean serviceEnabled = false;
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 0;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // assigning ID of startButton
        // to the object start
        start = (Button) findViewById( R.id.startButton );

        // assigning ID of stopButton
        // to the object stop
        stop = (Button) findViewById( R.id.stopButton );

        // declaring listeners for the
        // buttons to make them respond
        // correctly according to the process
        start.setOnClickListener(this);
        start.setEnabled(serviceEnabled);
        stop.setOnClickListener( this);
        stop.setEnabled(serviceEnabled);

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

        if(!MQTTServiceRunning()) {
            start.setEnabled(true);
        } else {
            startForegroundService(new Intent( this, MQTTService.class ) );
            stop.setEnabled(true);
        }



    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void onClick(View view) {

        // process to be performed
        // if start button is clicked
        if(view == start){
            // starting the service
            startForegroundService(new Intent( this, MQTTService.class ) );
            start.setEnabled(false);
            stop.setEnabled(true);
        }

        // process to be performed
        // if stop button is clicked
        else if (view == stop){

            // stopping the service
            stopService(new Intent( this, MQTTService.class ) );
            stop.setEnabled(false);
            start.setEnabled(true);
        }
    }

    public boolean MQTTServiceRunning(){
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for(ActivityManager.RunningServiceInfo service: activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if(MQTTService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}