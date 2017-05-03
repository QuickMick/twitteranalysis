package com.example.paulc.twittersentimentanalysis;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.mick.service.AnalysisSchedulTask;

public class TaskScheduledActivity extends AppCompatActivity implements View.OnClickListener{

    private TextView duration,interval;

    private Button dismiss;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_scheduled);

        this.duration = (TextView)findViewById(R.id.durationlbl);
        this.interval = (TextView)findViewById(R.id.intervallbl);

        this.dismiss = (Button)findViewById(R.id.dismissbtn);
        this.dismiss.setOnClickListener(this);


    }

    @Override
    protected void onResume(){
        super.onResume();

        SharedPreferences sharedPref = this.getSharedPreferences(Settings.SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);

        int hour_interval = sharedPref.getInt("hour_interval", 0);
        int min_interval = sharedPref.getInt("min_interval", 0);
        int hour_duration=sharedPref.getInt("hour_duration", 0);
        int min_duration=sharedPref.getInt("min_duration", 0);


        this.duration.setText(String.format("%02d", hour_duration)+":"+String.format("%02d", min_duration));
        this.interval.setText(String.format("%02d", hour_interval)+":"+String.format("%02d", min_interval));
    }

    @Override
    public void onClick(View v) {
        if(v == this.dismiss){

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Do you really want to dissmiss all scheduled tasks?");
            builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    AnalysisSchedulTask.stopAlarm(TaskScheduledActivity.this);
                    TaskScheduledActivity.this.finish();
                    TaskScheduledActivity.this.startActivity(new Intent(TaskScheduledActivity.this, MainActivity.class));
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




        }
    }
}
