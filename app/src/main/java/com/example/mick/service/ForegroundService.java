package com.example.mick.service;

/**
 * Created by Mick on 27.04.2017.
 */
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
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
    public static final String SEARCH_CRITERIA = "SEARCH_CRITERIA";
    public static String STARTFOREGROUND_ACTION = "FS_STARTFOREGROUND_ACTION";
    public static String STOP_ANALYSIS_ACTION = "FS_STOP_ANALYSIS_ACTION";
    public static String STOPFOREGROUND_ACTION = "FS_STOPFOREGROUND_ACTION";

    public static String GO_TO_GRAPH_ACTION = "FS_GO_TO_GRAPH_ACTION";

    private Timer updateDataTimer;


    @Override
    public void onCreate() {
        super.onCreate();

     //   this.result = new AnalizationResult();
       // this.result_steps = new LinkedList<AnalizationResult>();


    }

    private void updateResult(){
        AnalizationResult current = AnalizationHelper.INSTANCE().getTwitterCrawler().getCurrentResult();
        AnalizationHelper.INSTANCE().addNextResultToFinalResult(current);

      //  Log.d("analize_result",AnalizationHelper.INSTANCE().getFinalResult().toString());

        this.updateNotification("Analized tweets: "+AnalizationHelper.INSTANCE().getFinalResult().tweetCount,AnalizationHelper.INSTANCE().isRunning());
    }

    private void sendMessageToActivity(String msg) {
        Intent intent = new Intent("TwitterAnalizationUpdate");
        // You can also include some extra data.
        intent.setAction(Constants.ACTION.ANALIZATION);
        intent.putExtra("MSG",msg);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        if(intent == null || intent.getAction() == null){
            Toast.makeText(this,"Action unknown try again",Toast.LENGTH_SHORT).show();
        }

        if (intent.getAction().equals(ForegroundService.STARTFOREGROUND_ACTION)) {
            Log.d("AppD", "Received Start Foreground Intent ");
            AnalizationHelper.INSTANCE().recreate();
            AnalizationHelper.INSTANCE().init(this);

            AnalizationHelper.INSTANCE().loadSettings(this);

            String keywords = intent.getStringExtra(ForegroundService.SEARCH_CRITERIA);
            AnalizationHelper.INSTANCE().setSaved(false);
            AnalizationHelper.INSTANCE().startAnalization(keywords,this);


            this.updateDataTimer = new Timer();
            this.updateDataTimer .schedule(
                    new TimerTask() {
                        public void run() {
                            ForegroundService.this.updateResult();

               /* Log.d("analizer","Current WORDCOUNT: "+crawler.getCurrentResult().wordCount);
                System.out.println(crawler..wordCount);*/
                        }
                    } , 0, 1000);

            Toast.makeText(this,"Analization succesfully started.",Toast.LENGTH_SHORT).show();




            Notification notification = this.createNotification("Analized tweets: 0",true);
            startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE,
                    notification);
        } else if (intent.getAction().equals(ForegroundService.STOP_ANALYSIS_ACTION)) {
            Log.i(LOG_TAG, "Clicked stop analysis");



            Toast.makeText(this,"Analization stopped",Toast.LENGTH_SHORT).show();

            this.stopService();
            this.sendMessageToActivity(Constants.ANALIZATION.BROADCAST_ANALIZATION_STOPPED);



            //go tho the diagramm screen if it is not opend already
            String mode= intent.getStringExtra(Constants.ANALIZATION.DIAGRAM_MODE);
            if(mode==null)mode = "";
            if(!BarChartActivity.isActive() && !mode.equals(Constants.ANALIZATION.DIAGRAM_MODE_DONT_SHOW)) {
                Intent notificationIntent = new Intent(this, BarChartActivity.class);
             //   notificationIntent.putExtra(Constants.ANALIZATION.DIAGRAM_MODE, Constants.ANALIZATION.MODE_ANALIZATION_RUNNING);
                notificationIntent.setAction(ForegroundService.GO_TO_GRAPH_ACTION);
                notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
               // notificationIntent.putExtra(Constants.ANALIZATION.DIAGRAM_MODE, Constants.ANALIZATION.DIAGRAM_MODE_FIND);
             /*  if(AnalizationHelper.INSTANCE().isRunning()) {
                    notificationIntent.putExtra(Constants.ANALIZATION.DIAGRAM_MODE, Constants.ANALIZATION.MODE_ANALIZATION_RUNNING);
                }else{
                    notificationIntent.putExtra(Constants.ANALIZATION.DIAGRAM_MODE, Constants.ANALIZATION.MODE_ANALIZATION_STOPPED);
                }*/

                notificationIntent.putExtra(Constants.ANALIZATION.DIAGRAM_MODE, Constants.ANALIZATION.MODE_ANALIZATION_STOPPED);

                startActivity(notificationIntent);
            }

        } else if (intent.getAction().equals(ForegroundService.STOPFOREGROUND_ACTION)) {    //this comes from app
            Log.i(LOG_TAG, "Received Stop Foreground Intent");
            this.updateNotification("Analized tweets: "+AnalizationHelper.INSTANCE().getFinalResult().tweetCount,false);
            Toast.makeText(this,"Stopping analysis, please be patient",Toast.LENGTH_SHORT).show();
            this.stopService();

            this.sendMessageToActivity(Constants.ANALIZATION.BROADCAST_ANALIZATION_STOPPED);

            Toast.makeText(this,"Analization stopped",Toast.LENGTH_SHORT).show();

            //go tho the diagramm screen
           /* Intent notificationIntent = new Intent(this, BarChartActivity.class);
            notificationIntent.putExtra(Constants.ANALIZATION.DIAGRAM_MODE,Constants.ANALIZATION.MODE_ANALIZATION_RUNNING);
            notificationIntent.setAction(ForegroundService.GO_TO_GRAPH_ACTION);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            notificationIntent.putExtra(Constants.ANALIZATION.DIAGRAM_MODE,Constants.ANALIZATION.MODE_ANALIZATION_STOPPED);
            startActivity(notificationIntent);*/
        }


        return START_STICKY;
    }

    /**
     * Stops the analization and closes the notification
     */
    private void stopService(){
    //    final ProgressDialog dialog = ProgressDialog.show(ForegroundService.this, "","Saving. Please wait...", true);

        new Handler(this.getMainLooper()).post(new Runnable() {
            public void run() {
                if(!AnalizationHelper.INSTANCE().isRunning()){  //check is needed, becuase maybe a scheduled call can come after the user has stoped it in beforehand
                    return;
                }

                AnalizationHelper.INSTANCE().setBlocked(true);

                ForegroundService.this.updateDataTimer.cancel();
                AnalizationHelper.INSTANCE().stopAnalization();

                stopForeground(true);
                stopSelf();

                AnalizationHelper.INSTANCE().setBlocked(false);

              //  dialog.dismiss();
            }
        });

    }

    private void updateNotification(String text,boolean isRunning){
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(NOTIF_ID, this.createNotification(text,isRunning));
    }

    private Notification createNotification(String content,boolean isRunning){

        Intent notificationIntent = new Intent(this, BarChartActivity.class);
        notificationIntent.setAction(ForegroundService.GO_TO_GRAPH_ACTION);
        notificationIntent.putExtra(Constants.ANALIZATION.DIAGRAM_MODE,Constants.ANALIZATION.MODE_ANALIZATION_RUNNING);
        //notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,notificationIntent, 0);

        Intent stopIntent = new Intent(this, ForegroundService.class);
        stopIntent.setAction(ForegroundService.STOP_ANALYSIS_ACTION);
        PendingIntent pstopIntent = PendingIntent.getService(this, 0, stopIntent, 0);

        Bitmap icon = BitmapFactory.decodeResource(getResources(),
                R.drawable.movie);  // TODO: @paul do you have a nice icon?

        Notification notification;
        if(isRunning) {
            notification = new NotificationCompat.Builder(this)
                    .setContentTitle("Twitter Analysis running")
                    .setTicker("Twitter Analysis running")
                    .setContentText(content)
                    .setSmallIcon(R.drawable.movie) // TODO: @paul do you have a nice icon?
                    .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                    .setContentIntent(pendingIntent)
                    .setOngoing(true)
                    .addAction(android.R.drawable.ic_lock_power_off,
                            "Stop", pstopIntent)
                    .build();
        }

        else {
            notification = new NotificationCompat.Builder(this)
                    .setContentTitle("Twitter Analysis stopped")
                    .setTicker("Twitter Analysis stopped")
                    .setContentText(content)
                    .setSmallIcon(R.drawable.movie) // TODO: @paul do you have a nice icon?
                    .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                    .setProgress(0, 0, true)
                    .setContentIntent(pendingIntent)
                    .setOngoing(true)
                    .build();
        }


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