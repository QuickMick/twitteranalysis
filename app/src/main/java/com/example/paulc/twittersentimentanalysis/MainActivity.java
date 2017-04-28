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

import com.example.mick.emotionanalizer.AnalizationHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

/**
 * TODO: @paul - look if old analization results are saved on the SD-Card, if no, hide the history button ?
 * TODO: @paul - remove logout button - i think our app really does not need a log in feature - that would just be one thing more we would need to maintain
 * TODO: @paul - if the analization is running at the moment ( AnalitaionHelper.INSTANCE().isRunning() ) then hide the "new analysis" button and replace it with a "go to live diagramm" button
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    // declaration of views
    FontManager FM;
    TextView userdisplay,logout,informativeText,backicon;
    Button logouttv,newAnalysis,history,settings;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FM=new FontManager(getApplicationContext());
        InitUI();
        TextView backicon = (TextView)findViewById(R.id.backicon);
        backicon.setTextColor(Color.parseColor("#1cb189"));
        FM.setBackIcon(backicon);

        //Database Connection to display the username in the GUI
      ///  FirebaseAuth f_auth = FirebaseAuth.getInstance();
       /// FirebaseUser f_user = f_auth.getCurrentUser();
       // String email = f_user.getEmail();
        String email = "test@test.com";


        userdisplay.setText(email);


        backicon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                finish();
            }
        });

        progressDialog = new ProgressDialog(this);

        logouttv.setOnClickListener(this);
        settings.setOnClickListener(this);
        history.setOnClickListener(this);
        newAnalysis.setOnClickListener(this);

      //  AnalizationHelper.INSTANCE().init(this);

    }


    public void InitUI(){

        backicon = (TextView)findViewById(R.id.backicon);
        userdisplay = (TextView)findViewById(R.id.userdisplay);
        logout = (TextView)findViewById(R.id.logout);
        logouttv = (Button)findViewById(R.id.logouttv);
        newAnalysis = (Button)findViewById(R.id.newAnalysis);
        history = (Button)findViewById(R.id.history);
        settings = (Button)findViewById(R.id.settings);
        informativeText = (TextView) findViewById(R.id.informativeText);

        FM.setAppRegular(backicon);
        FM.setAppRegular(userdisplay);
        FM.setAppRegular(logout);
        FM.setAppMedium(logouttv);
        FM.setAppMedium(newAnalysis);
        FM.setAppMedium(history);
        FM.setAppMedium(settings);
        FM.setAppRegular(informativeText);

    }



    //user Logout methode
    private void userLogout() {
        FirebaseAuth f_auth = FirebaseAuth.getInstance();
        f_auth.signOut();
        finish();
        startActivity(new Intent(MainActivity.this, LoginScreen5.class));
    }

    @Override
    public void onClick (View view){
        // if he presses on Register , call the register user function
        if (view == logouttv) {
            userLogout();
        }

        if (view == settings) {
            startActivity(new Intent(MainActivity.this, Settings.class));
        }

        if (view == history) {
            startActivity(new Intent(MainActivity.this, History.class));
        }

        if (view == newAnalysis) {

            if(AnalizationHelper.INSTANCE().isRunning()){
                Toast.makeText(this,"Analization already running. Pleas stop current analization first..",Toast.LENGTH_SHORT).show();
                return;
            }
            startActivity(new Intent(MainActivity.this, NewAnalysis.class));
        }


    }
}