package com.example.paulc.twittersentimentanalysis;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
