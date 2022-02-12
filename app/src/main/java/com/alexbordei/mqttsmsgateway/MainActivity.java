package com.alexbordei.mqttsmsgateway;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

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

        start = (Button) findViewById( R.id.startButton );
        stop = (Button) findViewById( R.id.stopButton );

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

        if(!SMSServiceRunning()) {
            start.setEnabled(true);
        } else {
            stop.setEnabled(true);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void onClick(View view) {

        // process to be performed
        // if start button is clicked
        if(view == start){
            // starting the service
            startForegroundService(new Intent( this, SMSThreadService.class ) );
            start.setEnabled(false);
            stop.setEnabled(true);
        }

        // process to be performed
        // if stop button is clicked
        else if (view == stop){
            // stopping the service
            stopService(new Intent( this, SMSThreadService.class ) );
            stop.setEnabled(false);
            start.setEnabled(true);
        }
    }

    public boolean SMSServiceRunning(){
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for(ActivityManager.RunningServiceInfo service: activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if(SMSThreadService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}