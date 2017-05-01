package com.example.paulc.twittersentimentanalysis;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;

import com.example.mick.emotionanalizer.AnalizationHelper;
import com.example.mick.service.ForegroundService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;

import org.w3c.dom.Text;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.conf.ConfigurationBuilder;


/**
 * Created by paulc on 21.04.2017.
 *
 * TODO: @paul add questionmark, which, when clicked, provides a short explanation (maybe toast) for how to get the twitter tokens? or open twitter age?
 * TODO: @paul this view looks weird in landscape mode
 * TODO: @paul pls add one option in the settings view, to specify the folder name for the analysis (see todo in AnalizationHelper)
 */
public class Settings extends AppCompatActivity implements View.OnClickListener {

    // declaration of views
    FontManager FM;
    TextView backicon;
    Button consumerkeybtn,consumerkeybtnscrt,accesstokenbtn,accesstokenbtnscrt,savebtn,validatebtn;
    EditText consumerkeytxt,consumerkeytxtscrt,accesstokentxt,accesstokentxtscrt;

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
        validatebtn.setOnClickListener(this);
        progressDialog = new ProgressDialog(this);

    }

    @Override
    protected void onResume(){
        super.onResume();

        //TODO: @paul load tokens from local encrypted storage

    }


    public void InitUI(){

        backicon = (TextView)findViewById(R.id.backicon);
        //Buttons
        consumerkeybtn = (Button) findViewById(R.id.consumerkeybtn);
        consumerkeybtnscrt = (Button) findViewById(R.id.consumerkeybtnscrt);
        accesstokenbtn = (Button) findViewById(R.id.accesstokenbtn);
        accesstokenbtnscrt = (Button) findViewById(R.id.accesstokenbtnscrt);
        savebtn = (Button) findViewById(R.id.savebtn) ;
        validatebtn = (Button) findViewById(R.id.validatebtn) ;
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


                //Getting values to be stored
                SettingsModel model = new SettingsModel(consumerkeytext, consumerkeytextscrt, accesstokentext, accesstokentextscrt);



                //TODO: @paul save in local encrypted storage an set following values
                AnalizationHelper.INSTANCE().setAccessToken("token");
                AnalizationHelper.INSTANCE().setAccessTokenSecret("token");
                AnalizationHelper.INSTANCE().setConsumerKey("token");
                AnalizationHelper.INSTANCE().setConsumerSecret("token");

                //checking if the data was inserted into the DB or not

            }
        });
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


        }else if( view == validatebtn){
            backicon.setEnabled(false);
            consumerkeybtn.setEnabled(false);
            consumerkeybtnscrt.setEnabled(false);
            accesstokenbtn.setEnabled(false);
            accesstokenbtnscrt.setEnabled(false);
            savebtn.setEnabled(false);
            validatebtn.setEnabled(false);
            //EditText
            consumerkeytxt.setEnabled(false);
            consumerkeytxtscrt.setEnabled(false);
            accesstokentxt.setEnabled(false);
            accesstokentxtscrt.setEnabled(false);
            // you have to check the credentials in a thread, otherwise android will drop an exepction,
            // because no network connections are allowed in the main-thread

            final ProgressDialog dialog = ProgressDialog.show(Settings.this, "","Checking Token-validity. Please wait...", true);
            new AsyncTask<Void,Void,Boolean>() {    //checking if twitter credentials are valid
                @Override
                protected Boolean doInBackground(Void... params) {
                    return Settings.this.vertifyTwitterCredentials();
                }
                protected void onPostExecute(Boolean result) {
                    backicon.setEnabled(true);
                    consumerkeybtn.setEnabled(true);
                    consumerkeybtnscrt.setEnabled(true);
                    accesstokenbtn.setEnabled(true);
                    accesstokenbtnscrt.setEnabled(true);
                    savebtn.setEnabled(true);
                    validatebtn.setEnabled(true);
                    //EditText
                    consumerkeytxt.setEnabled(true);
                    consumerkeytxtscrt.setEnabled(true);
                    accesstokentxt.setEnabled(true);
                    accesstokentxtscrt.setEnabled(true);

                    dialog.dismiss();
                    if(result) {
                        Toast.makeText(Settings.this,"Validation successfull. Tokens are correct",Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(Settings.this,"Tokens are incorrect",Toast.LENGTH_SHORT).show();
                    }
                }
            }.execute();
        }

    }

    private boolean vertifyTwitterCredentials(){
        Log.d("Settings","check credentials");
        AnalizationHelper.INSTANCE().init(this);

        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey(consumerkeytxt.getText().toString().trim())
                .setOAuthConsumerSecret(consumerkeytxtscrt.getText().toString().trim())
                .setOAuthAccessToken(accesstokentxt.getText().toString().trim())
                .setOAuthAccessTokenSecret(accesstokentxtscrt.getText().toString().trim());
        TwitterFactory tf = new TwitterFactory(cb.build());
        Twitter twitter = tf.getInstance();
        try {
            // following is really "best practice" :D
            User user = twitter.verifyCredentials();
            Log.d("Settings","credentials are ok");
            return true;
        } catch (Exception e) {
            Log.d("Settings","credentials are incorrect");
            e.printStackTrace();
            return false;
        }
    }
}