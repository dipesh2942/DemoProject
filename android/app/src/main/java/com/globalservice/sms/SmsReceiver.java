package com.globalservice.sms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;

import com.facebook.react.HeadlessJsTaskService;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.globalservice.Utility;
import com.globalservice.BackgroundService;

public class SmsReceiver extends BroadcastReceiver {

    private static ReactApplicationContext reactContext;

    public static void bind(ReactApplicationContext context) {
        reactContext= context;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        Bundle intentExtras = intent.getExtras();
        String smsBody = "", originatingAddress = "";
        if (intentExtras != null) {
            Object[] sms = (Object[]) intentExtras.get("pdus");
            if (sms != null) {
                for (Object sm : sms) {
                    SmsMessage smsMessage;

                    //For handling multiple API version and deprecation
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        String format = intentExtras.getString("format");
                        smsMessage = SmsMessage.createFromPdu((byte[]) sm, format);
                    } else {
                        //noinspection deprecation
                        smsMessage = SmsMessage.createFromPdu((byte[]) sm);
                    }
                    originatingAddress = smsMessage.getOriginatingAddress();
                    smsBody = smsMessage.getMessageBody();
                }
            }
        }

        if (Utility.isAppOnForeground((context))) {
            if (reactContext != null) {
                WritableNativeMap receivedMessage = new WritableNativeMap();

                receivedMessage.putString("originatingAddress", originatingAddress);
                receivedMessage.putString("body", smsBody);

                reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                        .emit("onSmsReceive", receivedMessage);
            }
        } else {
            Intent serviceIntent = new Intent(context, BackgroundService.class);
            serviceIntent.putExtra("originatingAddress", originatingAddress);
            serviceIntent.putExtra("body", smsBody);
            context.startService(serviceIntent);
            HeadlessJsTaskService.acquireWakeLockNow(context);
        }
    }
}
