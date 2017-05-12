package com.hhn.paulc.twittersentimentanalysis;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.hhn.mick.emotionanalizer.AnalizationHelper;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.conf.ConfigurationBuilder;


/**
 * Created by paulc on 21.04.2017.
 *
 * TODO: @paul add questionmark, which, when clicked, provides a short explanation (maybe toast) for how to get the twitter tokens? or open twitter age?
 * TODO: @paul this view looks weird in landscape mode
 */
public class Settings extends AppCompatActivity implements View.OnClickListener {

    public static final String SHARED_PREFERENCES_KEY = "TWITTER_ANALYSIS_PREFERENCES";

    // declaration of views

    Button savebtn,validatebtn;
    EditText consumerkeytxt,consumerkeytxtscrt,accesstokentxt,accesstokentxtscrt,foldertext;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        InitUI();


        savebtn.setOnClickListener(this);
        validatebtn.setOnClickListener(this);
        progressDialog = new ProgressDialog(this);


        SharedPreferences sharedPref = this.getSharedPreferences(Settings.SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);

        final String consumerkeytext = sharedPref.getString("consumerkey", "");
        final String consumerkeytextscrt = sharedPref.getString("consumerkeyscrt", "");
        final String accesstokentext = sharedPref.getString("accesstoken", "");
        final String accesstokentextscrt = sharedPref.getString("accesstokenscrt", "");
        final String folder = sharedPref.getString("folder", "twitter_results");

        AnalizationHelper.INSTANCE().setAccessToken(accesstokentext);
        AnalizationHelper.INSTANCE().setAccessTokenSecret(accesstokentextscrt);
        AnalizationHelper.INSTANCE().setConsumerKey(consumerkeytext);
        AnalizationHelper.INSTANCE().setConsumerSecret(consumerkeytextscrt);
        AnalizationHelper.INSTANCE().setAnalyzation_folder(folder);

        consumerkeytxt.setText(AnalizationHelper.INSTANCE().getConsumerKey());
        consumerkeytxtscrt.setText(AnalizationHelper.INSTANCE().getConsumerSecret());
        accesstokentxt.setText(AnalizationHelper.INSTANCE().getAccessToken());
        accesstokentxtscrt.setText(AnalizationHelper.INSTANCE().getAccessTokenSecret());
        foldertext.setText(AnalizationHelper.INSTANCE().getAnalyzation_folder());

    }

    @Override
    protected void onResume(){
        super.onResume();


    }


    public void InitUI(){

        //Buttons
     /*   consumerkeybtn = (Button) findViewById(R.id.consumerkeybtn);
        consumerkeybtnscrt = (Button) findViewById(R.id.consumerkeybtnscrt);
        accesstokenbtn = (Button) findViewById(R.id.accesstokenbtn);
        accesstokenbtnscrt = (Button) findViewById(R.id.accesstokenbtnscrt);*/
        savebtn = (Button) findViewById(R.id.savebtn) ;
        validatebtn = (Button) findViewById(R.id.validatebtn) ;

        foldertext = (EditText)findViewById(R.id.foldertext);

        //EditText
        consumerkeytxt = (EditText)findViewById(R.id.consumerkeytxt);
        consumerkeytxtscrt = (EditText)findViewById(R.id.consumerkeytxtscrt);
        accesstokentxt = (EditText)findViewById(R.id.accesstokentxt);
        accesstokentxtscrt = (EditText)findViewById(R.id.accesstokentxtscrt);



    }

    public void SaveData(){

        final String consumerkeytext = consumerkeytxt.getText().toString().trim();
        final String consumerkeytextscrt = consumerkeytxtscrt.getText().toString().trim();
        final String accesstokentext = accesstokentxt.getText().toString().trim();
        final String accesstokentextscrt = accesstokentxtscrt.getText().toString().trim();
        final String folder = foldertext.getText().toString().trim();
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

        if (folder != null && !folder.matches("[a-zA-Z0-9-_]+")){
            Toast.makeText(this, "Folder name contains prohibited characters", Toast.LENGTH_SHORT).show();
            // here we will stop the function from execution further
            return;
        }
/*
        savebtn.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View v) {
*/
                //Getting values to be stored
              //  SettingsModel model = new SettingsModel(consumerkeytext, consumerkeytextscrt, accesstokentext, accesstokentextscrt); // @paul i think this is not needed anymore because we have no firebase anymore right?

                AnalizationHelper.INSTANCE().setAccessToken(accesstokentext);
                AnalizationHelper.INSTANCE().setAccessTokenSecret(accesstokentextscrt);
                AnalizationHelper.INSTANCE().setConsumerKey(consumerkeytext);
                AnalizationHelper.INSTANCE().setConsumerSecret(consumerkeytextscrt);
                AnalizationHelper.INSTANCE().setAnalyzation_folder(folder);

                SharedPreferences sharedPref = Settings.this.getSharedPreferences(Settings.SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();

                editor.putString("consumerkey", consumerkeytext);
                editor.putString("consumerkeyscrt", consumerkeytextscrt);
                editor.putString("accesstoken", accesstokentext);
                editor.putString("accesstokenscrt", accesstokentextscrt);
                editor.putString("folder", folder);
                editor.commit();

                Toast.makeText(Settings.this,"Settings saved",Toast.LENGTH_SHORT).show();

/*
            }
        });*/
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
           /* new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    final Intent mainIntent = new Intent(Settings.this, MainActivity.class);
                    Settings.this.startActivity(mainIntent);
                    Settings.this.finish();
                }
            }, 5000);*/


        }else if( view == validatebtn){
          /*  consumerkeybtn.setEnabled(false);
            consumerkeybtnscrt.setEnabled(false);
            accesstokenbtn.setEnabled(false);
            accesstokenbtnscrt.setEnabled(false);*/
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
                 /*   consumerkeybtn.setEnabled(true);
                    consumerkeybtnscrt.setEnabled(true);
                    accesstokenbtn.setEnabled(true);
                    accesstokenbtnscrt.setEnabled(true);*/
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