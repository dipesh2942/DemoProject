package com.globalservice.background;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.annotation.Nullable;

public class CustomService extends Service {

    public static final String ACTION_START_COUNT = "action_start_count";
    public static final String ACTION_STOP_COUNT = "action_stop_count";
    public static final String ACTION_DOWNLOAD_IMAGE = "action_download_image";

    private CountDownTimer countDownTimer = null;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        registerReceiver(startReceiver, new IntentFilter(ACTION_START_COUNT));
        registerReceiver(stopReceiver, new IntentFilter(ACTION_STOP_COUNT));
        registerReceiver(downloadImageReceiver, new IntentFilter(ACTION_DOWNLOAD_IMAGE));

        countDownTimer = new CountDownTimer(60000,1000){

            @Override
            public void onTick(long l) {

                Intent moduleIntent = new Intent(BackgroundTask.ACTION_MODULE_RECEIVER);
                moduleIntent.putExtra("counter", "Remaining Time : " + (l/1000));
                sendBroadcast(moduleIntent);
            }

            @Override
            public void onFinish() {
                Intent moduleIntent = new Intent(BackgroundTask.ACTION_MODULE_RECEIVER);
                moduleIntent.putExtra("counter", "Timer Finished");
                sendBroadcast(moduleIntent);
            }
        };

        return START_STICKY;
    }

    private BroadcastReceiver startReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (countDownTimer!=null)
                countDownTimer.start();
        }
    };

    private BroadcastReceiver stopReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (countDownTimer!=null)
                countDownTimer.cancel();

            Intent moduleIntent = new Intent(BackgroundTask.ACTION_MODULE_RECEIVER);
            moduleIntent.putExtra("counter", "Timer Cancelled");
            sendBroadcast(moduleIntent);
        }
    };

    private BroadcastReceiver downloadImageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, Intent intent) {

            //To download image with async task
            new DownloadFileFromURL(context,intent.getStringExtra("url")).execute();
        }
    };

    @Override
    public void onDestroy() {
        unregisterReceiver(startReceiver);
        unregisterReceiver(stopReceiver);
        unregisterReceiver(downloadImageReceiver);
        super.onDestroy();
    }

}
