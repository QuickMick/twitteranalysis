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
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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
    FontManager FM;
    TextView backicon;
    Button go;
    EditText searchcriteria;

    private Button scheduleBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newanalysis);

        FM=new FontManager(getApplicationContext());
        InitUI();
        TextView backicon = (TextView)findViewById(R.id.backicon);
        backicon.setTextColor(Color.parseColor("#1cb189"));
        FM.setBackIcon(backicon);

        go.setOnClickListener(this);
    }


    public void InitUI(){

        backicon = (TextView)findViewById(R.id.backicon);
        //Buttons
        go = (Button) findViewById(R.id.go);
        scheduleBtn = (Button) findViewById(R.id.schedulebtn);
        scheduleBtn.setOnClickListener(this);
        //EditText
        searchcriteria = (EditText)findViewById(R.id.searchcriteria);

        FM.setAppRegular(backicon);
        FM.setAppMedium(go);
        FM.setAppMedium(searchcriteria);

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

            Log.d("AppD","start analization clicked");

            backicon.setEnabled(false);
            go.setEnabled(false);
            searchcriteria.setEnabled(false);



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
                        backicon.setEnabled(true);
                        go.setEnabled(true);
                        searchcriteria.setEnabled(true);

                        if (result) {
                            if(view == NewAnalysis.this.go) {
                                Intent startIntent = new Intent(NewAnalysis.this, ForegroundService.class);
                                startIntent.setAction(ForegroundService.STARTFOREGROUND_ACTION);
                                String kw = searchcriteria.getText().toString();
                                Log.d("AppD", "Start analysis with kewords: " + kw);
                                startIntent.putExtra(ForegroundService.SEARCH_CRITERIA, kw);
                                finish();   // i thought it would be a good idea to close the newAnalisis activity so,
                                // if you hit back from the graph activity
                                // you are not able to start a new analisis, if the other one is still running
                                startService(startIntent);

                                Intent ac = new Intent(NewAnalysis.this, BarChartActivity.class);
                                ac.putExtra(Constants.ANALIZATION.DIAGRAM_MODE, Constants.ANALIZATION.MODE_ANALIZATION_RUNNING);
                                startActivity(ac);
                            }else if(view == NewAnalysis.this.scheduleBtn){
                                NewAnalysis.this.scheduleTask(searchcriteria.getText().toString());
                            }

                        } else {
                            Toast.makeText(NewAnalysis.this, "Twitter-tokens are incorrect or missing, please check your settings.", Toast.LENGTH_SHORT).show();
                        }

                    dialog.dismiss();

                }
            }.execute();
        }

    }



    private void scheduleTask(final String keywords){

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
                    NewAnalysis.this.scheduleTaskX(keywords);
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

        this.scheduleTaskX(keywords);
    }

    private void scheduleTaskX(final String keywords){
        new DialogFragment() {

            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState) {
                //Use the current time as the default values for the time picker
                //Create and return a new instance of TimePickerDialog //android.R.style#Theme_Material_Dialog_Alert
                return new TimePickerDialog(NewAnalysis.this, AlertDialog.THEME_TRADITIONAL ,new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, final int hourOfDay, final int minute) {

                        new DialogFragment() {

                            @Override
                            public Dialog onCreateDialog(Bundle savedInstanceState) {
                                //Use the current time as the default values for the time picker

                                //Create and return a new instance of TimePickerDialog //android.R.style#Theme_Material_Dialog_Alert
                                return new TimePickerDialog(NewAnalysis.this, AlertDialog.THEME_TRADITIONAL ,new TimePickerDialog.OnTimeSetListener() {
                                    @Override
                                    public void onTimeSet(TimePicker view, final int hourOfDay_duration, final int minute_duration) {
                                        //TODO:


                                        //Intent intent = new Intent(NewAnalysis.this, AnalysisSchedulTask.class);
                                       // intent.setAction(AnalysisSchedulTask.ACTION);

                                        AnalysisSchedulTask.startAlarm(NewAnalysis.this,keywords,hourOfDay,minute,hourOfDay_duration,minute_duration);

                                        Log.d("analysis_schedule","Analysis scheduled each "+hourOfDay+":"+minute+" with the duration of "+hourOfDay_duration+":"+minute_duration);
                                        Toast.makeText(NewAnalysis.this,"Analysis scheduled each "+hourOfDay+":"+minute+" with the duration of "+hourOfDay_duration+":"+minute_duration,Toast.LENGTH_SHORT).show();
                                        NewAnalysis.this.finish();
                                    }
                                }, 0, 0, true);

                            }
                        }.show(NewAnalysis.this.getFragmentManager(),"Duration-Picker");
                        Toast.makeText(NewAnalysis.this,"Select duration of each Analysis (cannot be greater than your interval of "+hourOfDay+":"+minute+")",Toast.LENGTH_SHORT).show();





                    }
                }, 0, 0, true);

            }
        }.show(this.getFragmentManager(),"Interval-Picker");
        Toast.makeText(NewAnalysis.this,"Select intervall in which the Analysis should be started",Toast.LENGTH_SHORT).show();

    }
}
