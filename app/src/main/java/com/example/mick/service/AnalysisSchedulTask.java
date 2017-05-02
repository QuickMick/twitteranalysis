package com.example.mick.service;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.example.mick.emotionanalizer.AnalizationHelper;
import com.example.mick.emotionanalizer.AnalizationResult;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Mick on 02.05.2017.
 */
public class AnalysisSchedulTask extends BroadcastReceiver {

    public static final String ACTION = "ANALYSIS_SCHEDULE_TASK";

    private static boolean safeFromService = false;

    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.d("analysis_schedule","Action: "+intent.getAction());
        // receive the saving
        String msg = intent.getStringExtra("MSG");
        if(msg != null && msg.equals(Constants.ANALIZATION.BROADCAST_ANALIZATION_STOPPED)){
            LocalBroadcastManager.getInstance(context).unregisterReceiver(AnalysisSchedulTask.this);
            if(!AnalizationHelper.INSTANCE().isSaved() && safeFromService) {
                Log.d("analysis_schedule","Save from scheduled task");
                this.save(context);
            }else{
                Log.d("analysis_schedule","no need to save");
            }
            safeFromService=false;
            return;
        }

        String action = intent.getAction();
        if(action == null || !action.equals(ACTION)){   //return, if the broadcast action is directed to this task
            return;
        }


        if(AnalizationHelper.INSTANCE().isRunning()){
            Toast.makeText(context, "Cannot start scheduled Analysis Task, because Analysis already running! Waiting for next interval.", Toast.LENGTH_SHORT).show();
            return;
        }

        int hour = intent.getIntExtra("hour",-1);
        int min = intent.getIntExtra("minute",-1);
        String keywords = intent.getStringExtra("keywords");

        if(hour == -1 || min == -1){
            Toast.makeText(context, "Unable to start twitter analysis - maleformed information", Toast.LENGTH_SHORT).show();
            return;
        }


        LocalBroadcastManager.getInstance(context).registerReceiver(this,new IntentFilter(Constants.ACTION.ANALIZATION));

        Log.d("analysis_schedule","start scheduled task");
        this.start(context,keywords);
        safeFromService = true;

        //schedule the stopping of the analysis
        Handler mHandler = new Handler(context.getMainLooper());
        long delayedMillis = (hour*60*60*1000)+(min*60*1000);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d("analysis_schedule","Stop scheduled task");
                AnalysisSchedulTask.this.stop(context);
                Log.d("analysis_schedule","Scheduled task stopped");
            }
        }, delayedMillis);
    }


    private void start(Context context, String kw){
        Intent startIntent = new Intent(context, ForegroundService.class);
        startIntent.setAction(ForegroundService.STARTFOREGROUND_ACTION);
        Log.d("AppD","Start scheduled task at: "+new Date()+" with keyowrds: "+kw);
        startIntent.putExtra(ForegroundService.SEARCH_CRITERIA,kw);
        context.startService(startIntent);
    }

    private void stop(Context context){
        Intent stopIntent = new Intent(context, ForegroundService.class);
        stopIntent.setAction(ForegroundService.STOPFOREGROUND_ACTION);
        stopIntent.putExtra(Constants.ANALIZATION.DIAGRAM_MODE,Constants.ANALIZATION.DIAGRAM_MODE_DONT_SHOW);
        context.startService(stopIntent);
    }

    private void save(final Context context){
        AnalizationHelper.INSTANCE().setBlocked(true);
        // Toast.makeText(BarChartActivity.this, "Start writing", Toast.LENGTH_SHORT).show();
// Caused by: java.lang.RuntimeException: Can't create handler inside thread that has not called Looper.prepare()

        new AsyncTask<Void, Void, String>(){
            @Override
            protected String doInBackground (Void...params){

               /* if(!BarChartActivity.this.requistPermission()){
                    return "External Storage unavailable";
                }*/

                //TODO: do the writing (following code) in an async task and show a "waiting" symbol - block everything else (also going back)
                AnalizationResult ar = AnalizationHelper.INSTANCE().getFinalResult();
                String json = ar.toJSON();
                //DateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm");

                //return "{\"startDate\":\""+format.format(this.startDate)+"\", "	/
                DateFormat format = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss");
                String fileName = "twitter_" + format.format(ar.startDate) + "_-_" + format.format(ar.endDate) + ".json";
                try {
                    File mydir = new File(Environment.getExternalStorageDirectory(), AnalizationHelper.INSTANCE().getAnalyzation_folder());
                    if (!mydir.exists()) {
                        mydir.mkdirs();
                    }

                    File myFile = new File(mydir, fileName);

                    Log.d("AppD", "save file: " + myFile.getAbsolutePath());

                    myFile.createNewFile();
                    FileOutputStream fOut = new FileOutputStream(myFile);
                    OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
                    myOutWriter.append(json);
                    myOutWriter.close();
                    fOut.close();

                    AnalizationHelper.INSTANCE().setSaved(true);

                    AnalizationHelper.INSTANCE().setBlocked(false);

                    return null;    // saving successfull

                } catch (Exception e) {

                    AnalizationHelper.INSTANCE().setBlocked(false);
                    return e.getMessage();   // saving not sucessful
                }
            }

            protected void onPostExecute (String result){


                if(result==null) {
                    Toast.makeText(context, "Done writing data to SD Card", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(context, "Error while saving: " + result, Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }
/*
    public static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    private boolean requistPermission(Context context){
        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)){
            // Toast.makeText(this, "Error: external storage is unavailable",Toast.LENGTH_SHORT).show();
            return false;
        }
        if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            //Toast.makeText(this, "Error: external storage is read only.",Toast.LENGTH_SHORT).show();
            return false;
        }
        Log.d("myAppName", "External storage is not read only or unavailable");

        if (ContextCompat.checkSelfPermission(context, // request permission when it is not granted.
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Toast.makeText(this, "permission:WRITE_EXTERNAL_STORAGE: NOT granted!",Toast.LENGTH_SHORT).show();
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(context,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(context,
                        new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }

            return false;
        }

        return true;
    }*/
}