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

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    // declaration of views
    FontManager FM;
    TextView userdisplay,logo,logout,informativeText,backicon;
    Button logouttv;
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
        FirebaseAuth f_auth = FirebaseAuth.getInstance();
        FirebaseUser f_user = f_auth.getCurrentUser();
        String email = f_user.getEmail();
        userdisplay.setText(email);

        backicon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                finish();
            }
        });

        progressDialog = new ProgressDialog(this);

        logouttv.setOnClickListener(this);
    }


    public void InitUI(){

        backicon = (TextView)findViewById(R.id.backicon);
        userdisplay = (TextView)findViewById(R.id.userdisplay);
        logout = (TextView)findViewById(R.id.logout);
        logouttv = (Button)findViewById(R.id.logouttv);
        informativeText = (TextView) findViewById(R.id.informativeText);

        FM.setAppRegular(backicon);
        FM.setAppRegular(userdisplay);
        FM.setAppRegular(logout);
        FM.setAppMedium(logouttv);
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
    public void onClick(View view) {

        // if he presses on Logout, call the user Logout function and redirects the user to the Login Page.
        if (view == logouttv) {
            userLogout();
//            startActivity(new Intent(MainActivity.this, LoginScreen5.class));
        }

    }
}