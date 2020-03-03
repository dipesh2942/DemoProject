package com.globalservice.background;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.facebook.react.HeadlessJsTaskService;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.globalservice.BackgroundService;
import com.globalservice.Utility;

public class CustomReceiver extends BroadcastReceiver {

    private static ReactApplicationContext reactContext;

    public static void bind(ReactApplicationContext context) {
        reactContext = context;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        if (Utility.isAppOnForeground((context))) {
            if (reactContext != null) {

                if (intent.getAction().equals(BackgroundTask.ACTION_MODULE_RECEIVER)) {
                    WritableNativeMap receivedMessage = new WritableNativeMap();
                    receivedMessage.putString("counter", intent.getStringExtra("counter"));
                    reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                            .emit("onCountChange", receivedMessage);
                }

                if (intent.getAction().equals(BackgroundTask.ACTION_IMAGE_MODULE_RECEIVER)){
                    WritableNativeMap receivedMessage = new WritableNativeMap();
                    receivedMessage.putInt("currentProgress", intent.getIntExtra("currentProgress",0));
                    receivedMessage.putDouble("progress", intent.getDoubleExtra("progress", 0.0));
                    receivedMessage.putString("data", intent.getStringExtra("data"));
                    reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                            .emit("onDownloadComplete", receivedMessage);
                }
            }
        } else {
            Intent serviceIntent = new Intent(context, BackgroundService.class);

            if (intent.getAction().equals(BackgroundTask.ACTION_MODULE_RECEIVER)) {
                serviceIntent.putExtra("counter", intent.getStringExtra("counter"));
            }
            if (intent.getAction().equals(BackgroundTask.ACTION_IMAGE_MODULE_RECEIVER)){
                serviceIntent.putExtra("currentProgress", intent.getIntExtra("currentProgress",0));
                serviceIntent.putExtra("progress", intent.getDoubleExtra("progress", 0.0));
                serviceIntent.putExtra("data", intent.getStringExtra("data"));
            }
            context.startService(serviceIntent);
            HeadlessJsTaskService.acquireWakeLockNow(context);
        }
    }
}
