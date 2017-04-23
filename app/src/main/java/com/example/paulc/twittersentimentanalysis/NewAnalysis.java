package com.example.paulc.twittersentimentanalysis;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;


/**
 * Created by paulc on 21.04.2017.
 */

public class NewAnalysis extends AppCompatActivity implements View.OnClickListener {

    // declaration of views
    FontManager FM;
    TextView backicon;
    Button go;
    EditText searchcriteria;

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
        //EditText
        searchcriteria = (EditText)findViewById(R.id.searchcriteria);

        FM.setAppRegular(backicon);
        FM.setAppMedium(go);
        FM.setAppMedium(searchcriteria);

    }

    @Override
    public void onClick (View view){
        // if he presses on Register , call the register user function
        if (view == go) {
            // TODO 5
            // 5. If the Data from the Settings is correct, redirect him to the display activity.

            //TODO 6
            // 6. Redirect him to the display activity.
        }

    }
}