package com.example.paulc.twittersentimentanalysis;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.graphs.BarChartActivity;
import com.example.mick.service.AnalysisSchedulTask;
import com.example.mick.emotionanalizer.AnalizationHelper;
import com.example.mick.service.Constants;
import com.example.mick.service.ForegroundService;

import java.util.Date;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.conf.ConfigurationBuilder;


/**
 * Created by paulc on 21.04.2017.
 *
 * TODO: @paul also this view looks weird in landscape mode
 */
public class NewAnalysis extends AppCompatActivity implements View.OnClickListener {

    // declaration of views
    Button go;
    EditText searchcriteria, searchcriteriaProhibited;

    private Button scheduleBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newanalysis);


        InitUI();

        go.setOnClickListener(this);
    }


    public void InitUI(){

        //Buttons
        go = (Button) findViewById(R.id.go);
        scheduleBtn = (Button) findViewById(R.id.schedulebtn);
        scheduleBtn.setOnClickListener(this);
        //EditText
        searchcriteria = (EditText)findViewById(R.id.searchcriteria);
        this.searchcriteriaProhibited = (EditText)findViewById(R.id.searchcriteriaprohibited);

       // FM.setAppMedium(go);
       // FM.setAppMedium(searchcriteria);
      //  FM.setAppMedium(searchcriteriaProhibited);
    }

    /**
     * checks if the given credentials/tokens/keys are correct
     * @return true, if correct, false if incorrect
     */
    private boolean vertifyTwitterCredentials(){
        Log.d("AppD","check credentials");
        AnalizationHelper.INSTANCE().init(this);

        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey(AnalizationHelper.INSTANCE().getConsumerKey())
                .setOAuthConsumerSecret(AnalizationHelper.INSTANCE().getConsumerSecret())
                .setOAuthAccessToken(AnalizationHelper.INSTANCE().getAccessToken())
                .setOAuthAccessTokenSecret(AnalizationHelper.INSTANCE().getAccessTokenSecret());
        TwitterFactory tf = new TwitterFactory(cb.build());
        Twitter twitter = tf.getInstance();

      //  TwitterStream stream= new TwitterStreamFactory(cb.build()).getInstance();
        try {
            // following is really "best practice" :D
            User user = twitter.verifyCredentials();
            Log.d("AppD","credentials are ok");
            return true;
        } catch (Exception e) {
            Log.d("AppD","credentials are incorrect");
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void onClick (final View view){
        // if he presses on Register , call the register user function
        if (view == go || view == this.scheduleBtn) {

            if(AnalizationHelper.INSTANCE().isRunning()){
                Toast.makeText(this,"Analization already running. Pleas stop current analization first..",Toast.LENGTH_SHORT).show();
                return;
            }else if(AnalizationHelper.INSTANCE().isBlocked()) {
                Toast.makeText(this, "Software is currently saving - please be patient", Toast.LENGTH_SHORT).show();
                return;
            }

       //     AnalizationHelper.INSTANCE().loadSettings(this);

            Log.d("AppD","start analization clicked");

            go.setEnabled(false);
            searchcriteria.setEnabled(false);
            searchcriteriaProhibited.setEnabled(false);


            // you have to check the credentials in a thread, otherwise android will drop an exepction,
            // because no network connections are allowed in the main-thread

            final ProgressDialog dialog = ProgressDialog.show(NewAnalysis.this, "","Starting analysis. Please wait...", true);
            // check if the twitter credentials are correct in async task
            new AsyncTask<Void,Void,Boolean>() {
                @Override
                protected Boolean doInBackground(Void... params) {
                    AnalizationHelper.INSTANCE().loadSettings(NewAnalysis.this);
                    return NewAnalysis.this.vertifyTwitterCredentials();
                }
                protected void onPostExecute(Boolean result) {

                        go.setEnabled(true);
                        searchcriteria.setEnabled(true);
                    searchcriteriaProhibited.setEnabled(true);
                        if (result) {
                            String kw = searchcriteria.getText().toString().trim().toLowerCase();
                            String kwP = searchcriteriaProhibited.getText().toString().trim().toLowerCase();

                            if(view == NewAnalysis.this.go) {
                                Intent startIntent = new Intent(NewAnalysis.this, ForegroundService.class);
                                startIntent.setAction(ForegroundService.STARTFOREGROUND_ACTION);

                                Log.d("AppD", "Start analysis with kewords: " + kw+" prohibited: "+kwP);
                                startIntent.putExtra(ForegroundService.SEARCH_CRITERIA, kw);
                                startIntent.putExtra(ForegroundService.SEARCH_CRITERIA_PROHIBITED, kwP);
                                finish();   // i thought it would be a good idea to close the newAnalisis activity so,
                                // if you hit back from the graph activity
                                // you are not able to start a new analisis, if the other one is still running
                                startService(startIntent);

                                Intent ac = new Intent(NewAnalysis.this, BarChartActivity.class);
                                ac.putExtra(Constants.ANALIZATION.DIAGRAM_MODE, Constants.ANALIZATION.MODE_ANALIZATION_RUNNING);
                                startActivity(ac);
                            }else if(view == NewAnalysis.this.scheduleBtn){
                                NewAnalysis.this.scheduleTask(kw,kwP);
                            }

                        } else {
                            Toast.makeText(NewAnalysis.this, "Twitter-tokens are incorrect or missing, please check your settings.", Toast.LENGTH_SHORT).show();
                        }

                    dialog.dismiss();

                }
            }.execute();
        }

    }



    //TODO: add prohibited
    private void scheduleTask(final String keywords, final String prohibitedKeywords){

     /*   boolean alarmUp = (PendingIntent.getBroadcast(this.getApplicationContext(), AnalysisSchedulTask.ID,
                new Intent(AnalysisSchedulTask.ACTION),
                PendingIntent.FLAG_NO_CREATE) != null);*/

        boolean alarmUp = AnalysisSchedulTask.IS_RUNNGING(this);

        Log.d("analysis_schedule","Task state - is schedule running: "+alarmUp);
        if(alarmUp){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Analysis already scheduled. Do you want to remove it and start a new schedule?");
            builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    //delete
                    AnalysisSchedulTask.stopAlarm(NewAnalysis.this);
                    NewAnalysis.this.scheduleTaskX(keywords,prohibitedKeywords);
                    dialog.dismiss();
                }
            });

            builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    //cancel
                    dialog.dismiss();
                }
            });
            builder.create().show();


            return;
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            final String packageName = this.getPackageName();
            PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Android 6 new Doze feature could prevent the analyze-job from firering during the night. Do you want to put this app to the whitelist of your battery optimization?");
                builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        Intent intent = new Intent();
                     //   intent.setAction(android.provider.Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                      //  else{
                            intent.setAction(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                            intent.setData(Uri.parse("package:" + packageName));
                       // }
                        NewAnalysis.this.startActivity(intent);

                        NewAnalysis.this.scheduleTaskX(keywords,prohibitedKeywords);
                        dialog.dismiss();
                    }
                });

                builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        NewAnalysis.this.scheduleTaskX(keywords,prohibitedKeywords);

                        dialog.dismiss();
                    }
                });
                builder.create().show();
            }else{
                NewAnalysis.this.scheduleTaskX(keywords,prohibitedKeywords);
            }
        }else{
            NewAnalysis.this.scheduleTaskX(keywords,prohibitedKeywords);
        }




    }

    private void scheduleTaskX(final String keywords, final String prohibitedKeywords){
        new DialogFragment() {

            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState) {
                //Use the current time as the default values for the time picker
                //Create and return a new instance of TimePickerDialog //android.R.style#Theme_Material_Dialog_Alert
                TimePickerDialog tpd= new TimePickerDialog(NewAnalysis.this, AlertDialog.THEME_HOLO_LIGHT ,new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, final int hourOfDay, final int minute) {

                        if(hourOfDay == 0 && minute ==0){
                            Toast.makeText(NewAnalysis.this,"Invalid interval of zero length",Toast.LENGTH_SHORT).show();
                            return;
                        }

                        new DialogFragment() {
                            @Override
                            public Dialog onCreateDialog(Bundle savedInstanceState) {
                                //Use the current time as the default values for the time picker

                                //Create and return a new instance of TimePickerDialog //android.R.style#Theme_Material_Dialog_Alert
                                //AlertDialog.THEME_TRADITIONAL
                                TimePickerDialog tpd2 = new TimePickerDialog(NewAnalysis.this, AlertDialog.THEME_HOLO_LIGHT ,new TimePickerDialog.OnTimeSetListener() {
                                    @Override
                                    public void onTimeSet(TimePicker view, final int hourOfDay_duration, final int minute_duration) {

                                        if(hourOfDay_duration == 0 && minute_duration ==0){
                                            Toast.makeText(NewAnalysis.this,"Invalid duration of zero length",Toast.LENGTH_SHORT).show();
                                            return;
                                        }

                                      /*  if((hourOfDay <= hourOfDay_duration && minute <= minute_duration)
                                                ||(hourOfDay <= hourOfDay_duration)){*/

                                      /*  if((hourOfDay < hourOfDay_duration)
                                                || (hourOfDay == hourOfDay_duration && minute <= minute_duration)){

                                            Toast.makeText(NewAnalysis.this,"Duration has to be smaller then the interval",Toast.LENGTH_SHORT).show();
                                            return;
                                        }*/

                                        long interval = (hourOfDay*60*60*1000)+(minute*60*1000);
                                        long duration = (hourOfDay_duration*60*60*1000)+(minute_duration*60*1000);

                                        if(interval < duration){
                                            Toast.makeText(NewAnalysis.this,"Duration has to be smaller then the interval",Toast.LENGTH_SHORT).show();
                                            return;
                                        }

                                        if(interval < (30*60*1000)){
                                            Toast.makeText(NewAnalysis.this,"Interval should be at least half an hour",Toast.LENGTH_SHORT).show();
                                            return;
                                        }

                                        if(duration > interval/2){
                                            Toast.makeText(NewAnalysis.this,"Duration should not be longer than the half of the intervals length",Toast.LENGTH_SHORT).show();
                                            return;
                                        }

                                        AnalysisSchedulTask.startAlarm(NewAnalysis.this,keywords,prohibitedKeywords,hourOfDay,minute,hourOfDay_duration,minute_duration);

                                        Log.d("analysis_schedule","Analysis scheduled each "+String.format("%02d",hourOfDay)+":"+String.format("%02d",minute)+" with the duration of "+String.format("%02d",hourOfDay_duration)+":"+String.format("%02d",minute_duration));

                                        Toast.makeText(NewAnalysis.this,"Analysis scheduled each "+String.format("%02d",hourOfDay)+":"+String.format("%02d",minute)+" with the duration of "+String.format("%02d",hourOfDay_duration)+":"+String.format("%02d",minute_duration),Toast.LENGTH_SHORT).show();
                                        NewAnalysis.this.finish();
                                    }
                                }, 0, 0, true);

                                LayoutInflater inflater = NewAnalysis.this.getLayoutInflater();
                                View dialogView = inflater.inflate(R.layout.title, null);
                                TextView texts=(TextView) dialogView.findViewById(R.id.textss);
                                texts.setText("Select duration of each Analysis (cannot be greater than the half of your interval of "+String.format("%02d",hourOfDay)+":"+String.format("%02d",minute)+")"); //"Select Duration of each analysis");
                                tpd2.setCustomTitle(dialogView);

                                return tpd2;

                            }
                        }.show(NewAnalysis.this.getFragmentManager(),"Duration-Picker");


                      //  Toast.makeText(NewAnalysis.this,"Select duration of each Analysis (cannot be greater than your interval of "+String.format("%02d",hourOfDay)+":"+String.format("%02d",minute)+")",Toast.LENGTH_SHORT).show();

                    }
                }, 0, 0, true);

                LayoutInflater inflater = NewAnalysis.this.getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.title, null);
                TextView texts=(TextView) dialogView.findViewById(R.id.textss);
                texts.setText("Select interval in which the Analysis should start");//"Select Interval of the analysis starts");
                tpd.setCustomTitle(dialogView);

                return tpd;

            }
        }.show(this.getFragmentManager(),"Interval-Picker");
       // Toast.makeText(NewAnalysis.this,"Select interval in which the Analysis should start",Toast.LENGTH_SHORT).show();

    }
}
