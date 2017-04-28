package com.example.mick.service;

/**
 * Created by Mick on 27.04.2017.
 */
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.example.graphs.BarChartActivity;
import com.example.mick.emotionanalizer.AnalizationHelper;
import com.example.mick.emotionanalizer.AnalizationResult;
import com.example.paulc.twittersentimentanalysis.NewAnalysis;
import com.example.paulc.twittersentimentanalysis.R;
import com.example.paulc.twittersentimentanalysis.Settings;

import java.util.Timer;
import java.util.TimerTask;

public class ForegroundService extends Service {
    private static final String LOG_TAG = "ForegroundService";
    private static final int NOTIF_ID=101;
    public static String STARTFOREGROUND_ACTION = "FS_STARTFOREGROUND_ACTION";
    public static String STOP_ANALYSIS_ACTION = "FS_STOP_ANALYSIS_ACTION";
    public static String STOPFOREGROUND_ACTION = "FS_STOPFOREGROUND_ACTION";

    public static String GO_TO_GRAPH_ACTION = "FS_GO_TO_GRAPH_ACTION";

    private Timer updateDataTimer;


    @Override
    public void onCreate() {
        super.onCreate();

        //TODO: starta nalaization in thread here
     //   this.result = new AnalizationResult();
       // this.result_steps = new LinkedList<AnalizationResult>();

        AnalizationHelper.INSTANCE().recreate();
        AnalizationHelper.INSTANCE().init(this);
        AnalizationHelper.INSTANCE().startAnalization("start",this);


        this.updateDataTimer = new Timer();
        this.updateDataTimer .schedule(
        new TimerTask() {
            public void run() {
                ForegroundService.this.updateResult();

                //TODO: send broadcast?
               /* Log.d("analizer","Current WORDCOUNT: "+crawler.getCurrentResult().wordCount);
                System.out.println(crawler..wordCount);*/
            }
        } , 0, 1000);

        Toast.makeText(this,"Analization succesfully started.",Toast.LENGTH_SHORT).show();
    }

    private void updateResult(){
        AnalizationResult current = AnalizationHelper.INSTANCE().getTwitterCrawler().getCurrentResult();
        AnalizationHelper.INSTANCE().addNextResultToFinalResult(current);

        Log.d("analize_result",AnalizationHelper.INSTANCE().getFinalResult().toString());

        this.updateNotification("analized words: "+AnalizationHelper.INSTANCE().getFinalResult().wordCount);
    }
/*
    private void sendMessageToActivity(String msg) {
        Intent intent = new Intent("TwitterAnalizationUpdate");
        // You can also include some extra data.
     //   intent.putExtra(AnalizationResult.class.toString(),this.result);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }*/

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction().equals(ForegroundService.STARTFOREGROUND_ACTION)) {
            Log.d("AppD", "Received Start Foreground Intent ");

            Notification notification = this.createNotification("Analized words: 25345");
            startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE,
                    notification);
        } else if (intent.getAction().equals(ForegroundService.STOP_ANALYSIS_ACTION)) {
            Log.i(LOG_TAG, "Clicked stop analysis");

            this.stopService();

            Toast.makeText(this,"Analization finished",Toast.LENGTH_SHORT).show();

            //go tho the diagramm screen
            Intent notificationIntent = new Intent(this, BarChartActivity.class);
            notificationIntent.putExtra(Constants.ANALIZATION.DIAGRAM_MODE,Constants.ANALIZATION.MODE_ANALIZATION_RUNNING);
            notificationIntent.setAction(ForegroundService.GO_TO_GRAPH_ACTION);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            notificationIntent.putExtra(Constants.ANALIZATION.DIAGRAM_MODE,Constants.ANALIZATION.MODE_ANALIZATION_STOPPED);
            startActivity(notificationIntent);

        } else if (intent.getAction().equals(ForegroundService.STOPFOREGROUND_ACTION)) {
            Log.i(LOG_TAG, "Received Stop Foreground Intent");
            this.stopService();
        }


        return START_STICKY;
    }

    /**
     * Stops the analization and closes the notification
     */
    private void stopService(){
        this.updateDataTimer.cancel();
        AnalizationHelper.INSTANCE().stopAnalization();

        stopForeground(true);
        stopSelf();
    }

    private void updateNotification(String text){
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(NOTIF_ID, this.createNotification(text));
    }

    private Notification createNotification(String content){

        Intent notificationIntent = new Intent(this, BarChartActivity.class);
        notificationIntent.setAction(ForegroundService.GO_TO_GRAPH_ACTION);
        notificationIntent.putExtra(Constants.ANALIZATION.DIAGRAM_MODE,Constants.ANALIZATION.MODE_ANALIZATION_STOPPED);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        Intent stopIntent = new Intent(this, ForegroundService.class);
        stopIntent.setAction(ForegroundService.STOP_ANALYSIS_ACTION);
        PendingIntent pstopIntent = PendingIntent.getService(this, 0,
                stopIntent, 0);

      /*  Intent playIntent = new Intent(this, ForegroundService.class);
        playIntent.setAction(Constants.ACTION.PLAY_ACTION);
        PendingIntent pplayIntent = PendingIntent.getService(this, 0,
                playIntent, 0);

        Intent nextIntent = new Intent(this, ForegroundService.class);
        nextIntent.setAction(Constants.ACTION.NEXT_ACTION);
        PendingIntent pnextIntent = PendingIntent.getService(this, 0,
                nextIntent, 0);*/

        Bitmap icon = BitmapFactory.decodeResource(getResources(),
                R.drawable.movie);  // TODO: @paul do you have a nice icon?

        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle("Twitter Analysis running")
                .setTicker("Twitter Analysis running")
                .setContentText(content)
                .setSmallIcon(R.drawable.movie) // TODO: @paul do you have a nice icon?
                .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .addAction(android.R.drawable.ic_media_pause,
                        "Stop", pstopIntent)
                   /* .addAction(android.R.drawable.ic_media_play, "Play",
                            pplayIntent)
                    .addAction(android.R.drawable.ic_media_next, "Next",
                            pnextIntent)*/
                .build();

        return notification;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(LOG_TAG, "In onDestroy");
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Used only in case of bound services.
        return null;
    }
}