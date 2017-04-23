package com.example.paulc.twittersentimentanalysis;

import android.app.Activity;
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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import com.example.paulc.twittersentimentanalysis.FontManager;


public class LoginScreen5 extends Activity implements View.OnClickListener {

    // initialization of views
    FontManager FM;
    EditText passwordedt,usernameedt;
    Button signIn,registertv;
    TextView logo,forgotPassword,register;
    TextView registerhint;
    private ProgressDialog progressDialog;
    FirebaseAuth firebaseAuth ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen5);

        FM=new FontManager(getApplicationContext());
        InitUI();
        TextView backicon = (TextView)findViewById(R.id.backicon);
        backicon.setTextColor(Color.parseColor("#1cb189"));
        FM.setBackIcon(backicon);
        backicon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                finish();
            }
        });

        //intialization of Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);

        signIn.setOnClickListener(this);
        registertv.setOnClickListener(this);
    }


    public void InitUI(){

        logo = (TextView)findViewById(R.id.logo);
        usernameedt = (EditText)findViewById(R.id.usernameedt);
        passwordedt = (EditText)findViewById(R.id.passwordedt);
        signIn = (Button)findViewById(R.id.signinbtn);
        registertv = (Button)findViewById(R.id.registertv);
        forgotPassword = (TextView)findViewById(R.id.forgotpasswordtv);
        registerhint = (TextView)findViewById(R.id.registertvhint);

        FM.setAppRegular(logo);
        FM.setAppMedium(usernameedt);
        FM.setAppMedium(passwordedt);
        FM.setAppMedium(signIn);
        FM.setAppMedium(registertv);
        FM.setAppMedium(forgotPassword);
        FM.setAppMedium(registerhint);

    }

    //user login methode
    private void userLogin() {
        String email = usernameedt.getText().toString().trim();
        String password = passwordedt.getText().toString().trim();

        //checking if the Email is empty or not.
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please enter your Email", Toast.LENGTH_SHORT).show();
            // here we will stop the function from execution further
            return;
        }

        //checking if the Password empty or not.
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter your Password", Toast.LENGTH_SHORT).show();
            // here we will stop the function from execution further
            return;
        }

        progressDialog.setMessage("Login...");
        progressDialog.show();

        firebaseAuth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();
                        if (task.isSuccessful())
                        {
                            if (firebaseAuth.getCurrentUser() != null){
                                finish();
                                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                            } else {
                                Toast.makeText(LoginScreen5.this, "Wrong username or password", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(LoginScreen5.this, "Wrong username or password", Toast.LENGTH_SHORT).show();
                        }
                    }
                });


    }

    public void onClick (View view){
        // if he presses on SignIn , call the user Login function
        if (view == signIn) {
            userLogin();
        }

        // if he presses on Register, redirect him to the Register Page
        else if (view == registertv){
            finish();
            startActivity(new Intent(LoginScreen5.this, RegisterActivity.class));
        }

    }



}
