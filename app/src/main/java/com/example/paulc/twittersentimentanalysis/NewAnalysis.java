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

import com.example.graphs.BarChartActivity;
import com.example.graphs.LineGraphActivity;
import com.example.mick.emotionanalizer.AnalizationHelper;
import com.example.mick.service.Constants;
import com.example.mick.service.ForegroundService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.User;
import twitter4j.conf.ConfigurationBuilder;


/**
 * Created by paulc on 21.04.2017.
 *
 * TODO: @paul also this view looks weird in landscape mode
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

    /**
     * checks if the given credentials/tokens/keys are correct
     * @return true, if correct, false if incorrect
     */
    private boolean vertifyTwitterCredentials(){
        Log.d("AppD","check credentials");
        AnalizationHelper.INSTANCE().init(this);

        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey(AnalizationHelper.INSTANCE().getConsumerKey())
                .setOAuthConsumerSecret(AnalizationHelper.INSTANCE().getConsumerSecret())
                .setOAuthAccessToken(AnalizationHelper.INSTANCE().getAccessToken())
                .setOAuthAccessTokenSecret(AnalizationHelper.INSTANCE().getAccessTokenSecret());
        TwitterFactory tf = new TwitterFactory(cb.build());
        Twitter twitter = tf.getInstance();

      //  TwitterStream stream= new TwitterStreamFactory(cb.build()).getInstance();
        try {
            // following is really "best practice" :D
            User user = twitter.verifyCredentials();
            Log.d("AppD","credentials are ok");
            return true;
        } catch (Exception e) {
            Log.d("AppD","credentials are incorrect");
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void onClick (View view){
        // if he presses on Register , call the register user function
        if (view == go) {

            if(AnalizationHelper.INSTANCE().isRunning()){
                Toast.makeText(this,"Analization already running. Pleas stop current analization first..",Toast.LENGTH_SHORT).show();
                //TODO 7
                // 7. Redirect him to the display activity.
                return;
            }

            Log.d("AppD","start analization clicked");

            //TODO: @paul maybe show some "processing" or "waiting" bars or icons while vertifying, if the twitter credentials are correct?
            backicon.setEnabled(false);
            go.setEnabled(false);
            searchcriteria.setEnabled(false);

            // you have to check the credentials in a thread, otherwise android will drop an exepction,
            // because no network connections are allowed in the main-thread

            // check if the twitter credentials are correct in async task
            new AsyncTask<Void,Void,Boolean>() {
                @Override
                protected Boolean doInBackground(Void... params) {
                    return NewAnalysis.this.vertifyTwitterCredentials();
                }
                protected void onPostExecute(Boolean result) {
                    backicon.setEnabled(true);
                    go.setEnabled(true);
                    searchcriteria.setEnabled(true);

                    if(result) {
                        Intent startIntent = new Intent(NewAnalysis.this, ForegroundService.class);
                        startIntent.setAction(ForegroundService.STARTFOREGROUND_ACTION);
                        String kw = searchcriteria.getText().toString();
                        Log.d("AppD","Start analysis with kewords: "+kw);
                        startIntent.putExtra(ForegroundService.SEARCH_CRITERIA,kw);
                        finish();   // i thought it would be a good idea to close the newAnalisis activity so,
                        // if you hit back from the graph activity
                        // you are not able to start a new analisis, if the other one is still running
                        startService(startIntent);

                        Intent ac = new Intent(NewAnalysis.this, BarChartActivity.class);
                        ac.putExtra(Constants.ANALIZATION.DIAGRAM_MODE,Constants.ANALIZATION.MODE_ANALIZATION_RUNNING);
                        startActivity(ac);

                    }else{
                        Toast.makeText(NewAnalysis.this,"Twitter-tokens are incorrect, please check your settings.",Toast.LENGTH_SHORT).show();
                    }


                }
            }.execute();
        }

    }
}