package com.example.paulc.twittersentimentanalysis;

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
            AnalysisSchedulTask.stopAlarm(this);
            finish();

            startActivity(new Intent(TaskScheduledActivity.this, MainActivity.class));

        }
    }
}
