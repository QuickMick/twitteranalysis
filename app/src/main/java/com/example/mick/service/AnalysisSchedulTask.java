package com.example.mick.service;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.example.mick.emotionanalizer.AnalizationHelper;
import com.example.mick.emotionanalizer.AnalizationResult;
import com.example.paulc.twittersentimentanalysis.Settings;

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

    public static final int ID = 234324243;

    private static boolean safeFromService = false;

    private static boolean isAnalizing=false;
    public static boolean isAnalizing(){
        return isAnalizing;
    }
/*
    private static int hour_interval = 0;
    private static int min_interval = 0;
    private static int hour_duration=0;
    private static int min_duration=0;*/

    public static boolean IS_RUNNGING(Activity i){
        boolean alarmUp = (PendingIntent.getBroadcast(i.getApplicationContext(), AnalysisSchedulTask.ID,
                new Intent(AnalysisSchedulTask.ACTION),
                PendingIntent.FLAG_NO_CREATE) != null);
        return alarmUp;
    }

    public static void stopAlarm(Activity a){
      /*  AlarmManager alarmManager = (AlarmManager) a.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(AnalysisSchedulTask.ACTION); //new Intent(a, AnalysisSchedulTask.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(a.getApplicationContext(), AnalysisSchedulTask.ID, intent,PendingIntent.FLAG_CANCEL_CURRENT);
        alarmManager.cancel(pendingIntent);


        */


        PendingIntent pi = PendingIntent.getBroadcast(a.getApplicationContext(), AnalysisSchedulTask.ID,
                new Intent(AnalysisSchedulTask.ACTION),
                PendingIntent.FLAG_NO_CREATE);

        if(pi != null){
            pi.cancel();
            Log.d("analysis_schedule","current alarm stopped");
            Toast.makeText(a,"Scheduled tasks stopped",Toast.LENGTH_SHORT).show();
        }else{
            Log.d("analysis_schedule","current alarm still runnning");
            Toast.makeText(a,"Scheduled tasks still running",Toast.LENGTH_SHORT).show();
        }
    }

    public static void startAlarm(Activity a, String keywords,int hourOfDay, int minute, int hourOfDay_duration, int minute_duration) {
/*
        SharedPreferences sharedPref = a.getPreferences(Context.MODE_PRIVATE);
        int defaultValue = a.getResources().getInteger(R.string.saved_high_score_default);
        long highScore = sharedPref.getInt(getString(R.string.saved_high_score), defaultValue);

*/
        Intent intent = new Intent(AnalysisSchedulTask.ACTION);
        intent.putExtra("hour",hourOfDay_duration);
        intent.putExtra("minute",minute_duration);
        intent.putExtra("keywords",keywords);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(a.getApplicationContext(), AnalysisSchedulTask.ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) a.getSystemService(Context.ALARM_SERVICE);
        // alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+ (15 * 1000), pendingIntent);

        long interval = (hourOfDay*60*60*1000)+(minute*60*1000);

        long start_time = System.currentTimeMillis();
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,  start_time, interval, pendingIntent);

        SharedPreferences sharedPref = a.getSharedPreferences(Settings.SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("hour_interval", hourOfDay);
        editor.putInt("hour_duration", hourOfDay_duration);
        editor.putInt("min_interval", minute);
        editor.putInt("min_duration", minute_duration);
        editor.putLong("start_time", start_time);
        editor.commit();

        Log.d("analysis_schedule","Schedule for first task: "+start_time);

        //alarmManager.set(AlarmManager.RTC_WAKEUP, new Date().getTime(), pendingIntent);
        //alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, new Date().getTime()+interval, interval, pendingIntent);
       // alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, 0, interval, pendingIntent); //TODO: to start immediatly just start wih 0 becuase its in the past?
    }




    public AnalysisSchedulTask(){
        Log.d("analysis_schedule","create schedule task");
    }

  /*  public static int getHour_interval() {
        return hour_interval;
    }

    public static int getMin_interval() {
        return min_interval;
    }

    public static int getHour_duration() {
        return hour_duration;
    }

    public static int getMin_duration() {
        return min_duration;
    }*/

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


        if(AnalizationHelper.INSTANCE().isRunning() || isAnalizing || safeFromService){
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

        isAnalizing =true;
      //  AnalizationHelper.INSTANCE().loadSettings(context);
        Intent startIntent = new Intent(context, ForegroundService.class);
        startIntent.setAction(ForegroundService.STARTFOREGROUND_ACTION);
        Log.d("AppD","Start scheduled task at: "+new Date()+" with keyowrds: "+kw);
        startIntent.putExtra(ForegroundService.SEARCH_CRITERIA,kw);
        context.startService(startIntent);
    }

    private void stop(Context context){
        isAnalizing=false;
        Intent stopIntent = new Intent(context, ForegroundService.class);
        stopIntent.setAction(ForegroundService.STOPFOREGROUND_ACTION);
        stopIntent.putExtra(Constants.ANALIZATION.DIAGRAM_MODE,Constants.ANALIZATION.DIAGRAM_MODE_DONT_SHOW);
        context.startService(stopIntent);
    }

    private void sendMessageToActivity(String msg,Context context) {
        Intent intent = new Intent("TwitterAnalizationUpdate");
        // You can also include some extra data.
        intent.setAction(Constants.ACTION.ANALIZATION);
        intent.putExtra("MSG",msg);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    private void save(final Context context){

        sendMessageToActivity(Constants.ANALIZATION.BROADCAST_ANALIZATION_SAVED,context);
        AnalizationHelper.INSTANCE().loadSettings(context);
        AnalizationHelper.INSTANCE().setBlocked(true);
        // Toast.makeText(BarChartActivity.this, "Start writing", Toast.LENGTH_SHORT).show();
// Caused by: java.lang.RuntimeException: Can't create handler inside thread that has not called Looper.prepare()

        new AsyncTask<Void, Void, String>(){
            @Override
            protected String doInBackground (Void...params){

               /* if(!BarChartActivity.this.requistPermission()){
                    return "External Storage unavailable";
                }*/

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
