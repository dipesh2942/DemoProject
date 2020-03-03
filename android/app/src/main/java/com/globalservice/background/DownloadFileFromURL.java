package com.globalservice.background;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

class DownloadFileFromURL extends AsyncTask<String, String, String> {

    private Context context;
    private File file;
    private String urlImage;
    private ArrayList<Double> progressList;
    private String placeHolder = Uri.parse("http://meeconline.com/wp-content/uploads/2014/08/placeholder.png").toString();

    DownloadFileFromURL(Context context, String url) {
        this.context = context;
        this.urlImage = url;
        this.progressList = new ArrayList<>();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        sendBroadcast(0, 0.0, placeHolder);
    }

    @Override
    protected String doInBackground(String... strings) {
        int count;
        try {
            URL url = new URL(urlImage);
            URLConnection urlConnection = url.openConnection();
            urlConnection.connect();

            // this will be useful so that you can show a typical 0-100% progress bar
            int lengthOfFile = urlConnection.getContentLength();

            String extension = urlImage.substring(urlImage.lastIndexOf("."));
            file = new File(context.getCacheDir(), "temp" + extension);
            FileOutputStream fileOutput = new FileOutputStream(file);
            InputStream inputStream = urlConnection.getInputStream();

            byte data[] = new byte[lengthOfFile];
            long total = 0;
            int bytesBuffered = 0;

            while ((count = inputStream.read(data)) != -1) {
                total += count;
                bytesBuffered += count;
                // publishing the progress....
                // After this onProgressUpdate will be called
                publishProgress("" + (int) ((total * 100) / lengthOfFile));

                // writing data to file
                fileOutput.write(data, 0, count);
                if (bytesBuffered > 1024 * 1024) { //flush after 1MB
                    bytesBuffered = 0;
                    fileOutput.flush();
                }
            }

            // flushing output
            fileOutput.flush();

            // closing streams
            fileOutput.close();
            inputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        int currentProgress = Integer.parseInt(values[0]);  //output example : 1 to 100
        double required1 = currentProgress / 10; //output example : 1.0, 10.0...,100.0
        double requiredProgress = required1 / 10; //output example : 0.1, 0.2,...1.0

        if (!progressList.contains(requiredProgress)) {
            progressList.add(requiredProgress);
            sendBroadcast(currentProgress, requiredProgress, placeHolder);
        }
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        sendBroadcast(100, 1.0,(file.length()>0) ? Uri.fromFile(file).toString() : placeHolder);
    }

    private void sendBroadcast(int currentProgress, double progress, String uri){
        Intent moduleIntent = new Intent(BackgroundTask.ACTION_IMAGE_MODULE_RECEIVER);
        moduleIntent.putExtra("currentProgress", currentProgress);
        moduleIntent.putExtra("progress", progress);
        moduleIntent.putExtra("data", uri);
        context.sendBroadcast(moduleIntent);
    }

}
