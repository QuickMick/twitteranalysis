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
import android.os.Handler;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseError;

import org.w3c.dom.Text;


/**
 * Created by paulc on 21.04.2017.
 */

public class Settings extends AppCompatActivity implements View.OnClickListener {

    // declaration of views
    FontManager FM;
    TextView backicon;
    Button consumerkeybtn,consumerkeybtnscrt,accesstokenbtn,accesstokenbtnscrt,savebtn;
    EditText consumerkeytxt,consumerkeytxtscrt,accesstokentxt,accesstokentxtscrt;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        FM=new FontManager(getApplicationContext());
        InitUI();
        TextView backicon = (TextView)findViewById(R.id.backicon);
        backicon.setTextColor(Color.parseColor("#1cb189"));
        FM.setBackIcon(backicon);

        savebtn.setOnClickListener(this);
        progressDialog = new ProgressDialog(this);

    }


    public void InitUI(){

        backicon = (TextView)findViewById(R.id.backicon);
        //Buttons
        consumerkeybtn = (Button) findViewById(R.id.consumerkeybtn);
        consumerkeybtnscrt = (Button) findViewById(R.id.consumerkeybtnscrt);
        accesstokenbtn = (Button) findViewById(R.id.accesstokenbtn);
        accesstokenbtnscrt = (Button) findViewById(R.id.accesstokenbtnscrt);
        savebtn = (Button) findViewById(R.id.savebtn) ;
        //EditText
        consumerkeytxt = (EditText)findViewById(R.id.consumerkeytxt);
        consumerkeytxtscrt = (EditText)findViewById(R.id.consumerkeytxtscrt);
        accesstokentxt = (EditText)findViewById(R.id.accesstokentxt);
        accesstokentxtscrt = (EditText)findViewById(R.id.accesstokentxtscrt);


        FM.setAppRegular(backicon);
        FM.setAppMedium(consumerkeybtn);
        FM.setAppMedium(consumerkeybtnscrt);
        FM.setAppMedium(accesstokenbtn);
        FM.setAppMedium(accesstokenbtnscrt);
        FM.setAppMedium(savebtn);
        FM.setAppMedium(consumerkeytxt);
        FM.setAppMedium(consumerkeytxtscrt);
        FM.setAppMedium(accesstokentxt);
        FM.setAppMedium(accesstokentxtscrt);

    }

    public void SaveData(){

        final String consumerkeytext = consumerkeytxt.getText().toString().trim();
        final String consumerkeytextscrt = consumerkeytxtscrt.getText().toString().trim();
        final String accesstokentext = accesstokentxt.getText().toString().trim();
        final String accesstokentextscrt = accesstokentxtscrt.getText().toString().trim();

        //checking if the Consumer Key is empty or not.
        if (TextUtils.isEmpty(consumerkeytext)){
            Toast.makeText(this, "Please enter your Consumer Key", Toast.LENGTH_SHORT).show();
            // here we will stop the function from execution further
            return;
        }

        //checking if the Secret Consumer Key empty or not.
        if (TextUtils.isEmpty(consumerkeytextscrt)){
            Toast.makeText(this, "Please enter your Secret Consumer Key", Toast.LENGTH_SHORT).show();
            // here we will stop the function from execution further
            return;
        }

        //checking if the Access Token empty or not.
        if (TextUtils.isEmpty(accesstokentext)){
            Toast.makeText(this, "Please enter your Access Token", Toast.LENGTH_SHORT).show();
            // here we will stop the function from execution further
            return;
        }

        //checking if the Secret Access Token empty or not.
        if (TextUtils.isEmpty(accesstokentextscrt)){
            Toast.makeText(this, "Please enter your Secret Access Token", Toast.LENGTH_SHORT).show();
            // here we will stop the function from execution further
            return;
        }

        savebtn.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View v) {
                //Creating firebase object
                databaseReference = firebaseDatabase.getInstance().getReference();

                //Getting values to be stored
                SettingsModel model = new SettingsModel(consumerkeytext, consumerkeytextscrt, accesstokentext, accesstokentextscrt);
                insertSaveData(model);

                //checking if the data was inserted into the DB or not

            }
        });
    }

    //methode for inserting the Settings in the Database.
    private void insertSaveData(SettingsModel model) {
        //calls the Database
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        //pushes the Data into the Database
        reference.child("Settings").push().setValue(model);
    }

    //methode for displaying the Saved Text
    private void showMessage(String message) {
        Toast msg = Toast.makeText(Settings.this, message, Toast.LENGTH_SHORT);
        msg.show();
    }

    //methode for displaying the Error Text
    private void showError(String message) {
        Toast msg = Toast.makeText(Settings.this, message, Toast.LENGTH_SHORT);
        msg.show();
    }

   // TODO 1
    // 1. Check why the Data is saved into the Database only after the second click on the Button. If i press two times on the button, the first time nothing happens. The second time, all the Data
    // is inserted into the DB. If a press a third time, even though i changed something in the text, the data from the second attempt is inserted.

    //TODO 2
    // 2. The Redirect should be made only if the Data was inserted successfully into the DB.
    // the redirect should be made after 5 secounds after the insert Job was successful. -- Partially Solved. Only need to work on the success part.

   // TODO 3
    // 3. The Data which was inserted into the fiels and saved, should be displayed into those fiels after the save and when the user enters the Setting again.

    @Override
    public void onClick (View view){
        // if he presses on Register , call the register user function
        if (view == savebtn) {
            // call the save button function
            SaveData();

            // redirect to the Main Activity, once the Data has been stored, after 5 seconds.
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    final Intent mainIntent = new Intent(Settings.this, MainActivity.class);
                    Settings.this.startActivity(mainIntent);
                    Settings.this.finish();
                }
            }, 5000);


        }

    }
}