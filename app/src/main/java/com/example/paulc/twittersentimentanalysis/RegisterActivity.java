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
import com.firebase.client.Firebase;

public class RegisterActivity extends Activity implements View.OnClickListener  {

    // initialization of views
    FontManager FM;
    EditText passwordedt,usernameedt;
    Button registerbtn,logintv;
    TextView logo,forgotPassword,register;
    private ProgressDialog progressDialog;
    FirebaseAuth firebaseAuth ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        FM=new FontManager(getApplicationContext());
        InitUI();
        Firebase.setAndroidContext(this);
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

        registerbtn.setOnClickListener(this);
        logintv.setOnClickListener(this);

    }

    public void InitUI(){

        logo = (TextView)findViewById(R.id.logo);
        usernameedt = (EditText)findViewById(R.id.usernameedt);
        passwordedt = (EditText)findViewById(R.id.passwordedt);
        registerbtn = (Button)findViewById(R.id.registerbtn);
        forgotPassword = (TextView)findViewById(R.id.forgotpasswordtv);
        logintv = (Button)findViewById(R.id.logintv);

        FM.setAppRegular(logo);
        FM.setAppMedium(usernameedt);
        FM.setAppMedium(passwordedt);
        FM.setAppMedium(registerbtn);
        FM.setAppMedium(logintv);
        FM.setAppMedium(forgotPassword);

    }

    public void onClick (View view){
        // if he presses on Register , call the register user function
        if (view == registerbtn) {
            registerUser();
        }

        // if he presses on Login IN, redirect him to the Login Page
        if (view == logintv){
            startActivity(new Intent(this, LoginScreen5.class));
        }

    }

    // methode for the registration of users
    private void registerUser(){
        //Email and Password for the editText
        final String email = usernameedt.getText().toString().trim();
        String password = passwordedt.getText().toString().trim();

        //checking if the Email is empty or not.
        if (TextUtils.isEmpty(email)){
            Toast.makeText(this, "Please enter your Email", Toast.LENGTH_SHORT).show();
            // here we will stop the function from execution further
            return;
        }

        //checking if the Password empty or not.
        if (TextUtils.isEmpty(password)){
            Toast.makeText(this, "Please enter your Password", Toast.LENGTH_SHORT).show();
            // here we will stop the function from execution further
            return;
        }

        //if validation was successfull, we will proceed with the next step.
        // firstly we will display a progressdialog with a text

        progressDialog.setMessage("Registering User... Please wait");
        progressDialog.show();

        // this methode will create a user in the firebase console with the given Email and Password
        // this listener will execute this methode in completion.
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //user is successfully registered.
                            //we can redirect the user to the login page.
                            if ((firebaseAuth.getCurrentUser().getUid()) != null) {

                                // UserModel == how the Data should be inserted in the Database.
                                // Everything is set in the UserModel Class.
                                // We are only saving the Email
                                UserModel model = new UserModel(email);
                                insertUser(model);
                            }
                            progressDialog.dismiss();
                            finish();
                            startActivity(new Intent(RegisterActivity.this, LoginScreen5.class));
                        } else {
                            showMessage("The User already exists");
                            progressDialog.dismiss();
                        }
                    }
                });
    }

    private void showMessage(String message) {
        Toast msg = Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_SHORT);
        msg.show();
    }

    //methode for inserting a User in the Database.
    private void insertUser(UserModel model) {
        //calls the Database
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        //pushes the Data into the Database
        reference.child("Users").push().setValue(model);
    }

}


