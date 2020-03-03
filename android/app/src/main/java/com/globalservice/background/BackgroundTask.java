package com.globalservice.background;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;


public class BackgroundTask extends ReactContextBaseJavaModule {

    public static final String ACTION_MODULE_RECEIVER = "module_receiver";
    public static final String ACTION_IMAGE_MODULE_RECEIVER = "image_download_receiver";

    public BackgroundTask(ReactApplicationContext reactContext) {
        super(reactContext);
        CustomReceiver.bind(reactContext);
    }

    @Override
    public String getName() {
        return "ImageDownloader";
    }

    @ReactMethod
    public void startService(){
        if (!isSmsServiceRunning()) {
            if (getCurrentActivity() != null) {
                getCurrentActivity().startService(new Intent(getCurrentActivity(), CustomService.class));
            }
        }
    }

    @ReactMethod
    public void startCount() {
        if (getCurrentActivity() != null)
            getCurrentActivity().sendBroadcast(new Intent(CustomService.ACTION_START_COUNT));
    }

    @ReactMethod
    public void stopCount() {
        if (getCurrentActivity() != null) {
            getCurrentActivity().sendBroadcast(new Intent(CustomService.ACTION_STOP_COUNT));
        }
    }

    @ReactMethod
    public void getImage(String url) {
        if (getCurrentActivity() != null)
            getCurrentActivity().sendBroadcast(new Intent(CustomService.ACTION_DOWNLOAD_IMAGE).putExtra("url", url));
    }

    private boolean isSmsServiceRunning() {
        if (getCurrentActivity() != null) {
            ActivityManager manager = (ActivityManager) getCurrentActivity().getSystemService(Context.ACTIVITY_SERVICE);
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (CustomService.class.getName().equals(service.service.getClassName())) {
                    return true;
                }
            }
        }
        return false;
    }
}
