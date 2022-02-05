package com.alexbordei.mqttsmsgateway;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.SmsManager;
import android.widget.Toast;

import java.util.List;

public class SMSHandler {
    private Context context;
    private String error = "";
    private boolean sentStatus = false;

    public SMSHandler(Context context) {
        this.context =  context;
    }

    public boolean getSentStatus() {
        return this.sentStatus;
    }
    public String getError() {
        return this.error;
    }


    public void sendSMS(String phoneNumber, String message)
    {
        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";

        PendingIntent sentPI = PendingIntent.getBroadcast(this.context, 0,
                new Intent(SENT), 0);

        PendingIntent deliveredPI = PendingIntent.getBroadcast(this.context, 0,
                new Intent(DELIVERED), 0);

        //---when the SMS has been sent---
        context.registerReceiver(new BroadcastReceiver(){
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        sentStatus = true;
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        sentStatus = false;
                        error = "Generic failure";
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        sentStatus = false;
                        error = "No service";
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        sentStatus = false;
                        error = "Null PDU";
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        sentStatus = false;
                        error = "Radio off";
                        break;
                }
            }
        }, new IntentFilter(SENT));

        //---when the SMS has been delivered---
        context.registerReceiver(new BroadcastReceiver(){
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        sentStatus = true;
                        break;
                    case Activity.RESULT_CANCELED:
                        sentStatus = false;
                        error = "SMS not delivered";
                        break;
                }
            }
        }, new IntentFilter(DELIVERED));

        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
    }
}
