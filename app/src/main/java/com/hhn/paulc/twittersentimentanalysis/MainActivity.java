package com.hhn.paulc.twittersentimentanalysis;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.net.wifi.WifiManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.hhn.graphs.BarChartActivity;
import com.hhn.mick.emotionanalizer.AnalizationHelper;
import com.hhn.mick.service.AnalysisSchedulTask;
import com.hhn.mick.service.Constants;

/**
 * TODO: @paul Change app name to something cool :D
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    /**
     * Ask once on startup, if the user wants to enable wifi
     */
    private static boolean ASKED_FOR_WLAN = false;

    // declaration of views

  //  TextView userdisplay,informativeText,backicon;
    Button newAnalysis,history,settings,imprintBtn,helpBtn,unsavedResultBtn;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("App",HelpActivity.THANKS_TO);

        // block main activity, if task is scheduled!

        if(AnalysisSchedulTask.IS_RUNNGING(this)){
            Log.d("analysis_schedule","scheduled task blocks main activity");
            finish();

            startActivity(new Intent(MainActivity.this, TaskScheduledActivity.class));
        }

        setContentView(R.layout.activity_main);

        InitUI();
     //   TextView backicon = (TextView)findViewById(R.id.backicon);
      //  backicon.setTextColor(Color.parseColor("#1cb189"));
     //   FM.setBackIcon(backicon);

        //Database Connection to display the username in the GUI

    //    String email = "test@test.com";


     //   userdisplay.setText(email);


      /*  backicon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                finish();
            }
        });*/

        progressDialog = new ProgressDialog(this);

     //   logouttv.setOnClickListener(this);
        settings.setOnClickListener(this);
        history.setOnClickListener(this);
        helpBtn.setOnClickListener(this);
        newAnalysis.setOnClickListener(this);
        this.imprintBtn.setOnClickListener(this);
        this.unsavedResultBtn.setOnClickListener(this);
    }


    @Override
    protected void onResume(){
        super.onResume();

        if(AnalysisSchedulTask.IS_RUNNGING(this)){
            Log.d("analysis_schedule","scheduled task blocks main activity");
            finish();

            startActivity(new Intent(MainActivity.this, TaskScheduledActivity.class));
        }

        this.unsavedResultBtn.setVisibility(Button.GONE);
        if(AnalizationHelper.INSTANCE().isRunning()){
            newAnalysis.setText("GO TO ANALYSIS");

            final int sdk = android.os.Build.VERSION.SDK_INT;
            if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                newAnalysis.setBackgroundDrawable( getResources().getDrawable(R.drawable.roundorange) );
            } else {
                newAnalysis.setBackground( getResources().getDrawable(R.drawable.roundorange));
            }

           // GradientDrawable bgShape = (GradientDrawable)newAnalysis.getBackground();
           // bgShape.setColor(ContextCompat.getColor(this, R.color.running));


        }else{
            newAnalysis.setText("New Analysis");
            GradientDrawable bgShape = (GradientDrawable)newAnalysis.getBackground();

            final int sdk = android.os.Build.VERSION.SDK_INT;
            if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                newAnalysis.setBackgroundDrawable( getResources().getDrawable(R.drawable.roundblue2) );
            } else {
                newAnalysis.setBackground( getResources().getDrawable(R.drawable.roundblue2));
            }

            if(!AnalizationHelper.INSTANCE().isSaved()){
                this.unsavedResultBtn.setVisibility(Button.VISIBLE);
            }

        }




       // ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
       // NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if(ASKED_FOR_WLAN) {
           // final WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            final WifiManager wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

            if (!wifi.isWifiEnabled()) {
                new AlertDialog.Builder(this).setMessage("We recomend to use WIFI for the Twitter Analysis. Do you want to activate your WIFI now?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case DialogInterface.BUTTON_POSITIVE:
                                        //Yes button clicked
                                        wifi.setWifiEnabled(true);
                                        break;

                                    case DialogInterface.BUTTON_NEGATIVE:
                                        //No button clicked
                                        break;
                                }
                            }
                        })
                        .setNegativeButton("No", null).show();
            }

            ASKED_FOR_WLAN=true;
        }


    }

    public void InitUI(){

    //    backicon = (TextView)findViewById(R.id.backicon);
    //    userdisplay = (TextView)findViewById(R.id.userdisplay);
       // logout = (TextView)findViewById(R.id.logout);
       // logouttv = (Button)findViewById(R.id.logouttv);
        newAnalysis = (Button)findViewById(R.id.newAnalysis);
        history = (Button)findViewById(R.id.history);
        settings = (Button)findViewById(R.id.settings);
        helpBtn = (Button) findViewById(R.id.help);
    //    informativeText = (TextView) findViewById(R.id.informativeText);
        unsavedResultBtn = (Button) findViewById(R.id.unsavedanalysis);
        this.imprintBtn = (Button)findViewById(R.id.imprintbnt);


     //   FM.setAppRegular(backicon);
    //    FM.setAppRegular(userdisplay);
       // FM.setAppRegular(logout);
     //   FM.setAppMedium(logouttv);

    //    FM.setAppRegular(informativeText);

    }

    @Override
    public void onClick (View view){
        // if he presses on Register , call the register user function
     /*   if (view == logouttv) {
            userLogout();
        }*/

        if (view == settings) {
            startActivity(new Intent(MainActivity.this, Settings.class));
        }

        if (view == history) {
            startActivity(new Intent(MainActivity.this, HistoryActivity.class));
        }

        if (view == newAnalysis) {

            if(AnalizationHelper.INSTANCE().isRunning()){
                Intent i = new Intent(MainActivity.this, BarChartActivity.class);
                i.putExtra( Constants.ANALIZATION.DIAGRAM_MODE,Constants.ANALIZATION.MODE_ANALIZATION_RUNNING);
                startActivity(i);


               // Toast.makeText(this,"Analization already running. Pleas stop current analization first..",Toast.LENGTH_SHORT).show();
               // return;
            }else if(AnalizationHelper.INSTANCE().isBlocked()) {
                Toast.makeText(this,"Software is currently saving - please be patient",Toast.LENGTH_SHORT).show();
            }else if(!AnalizationHelper.INSTANCE().isSaved()) {

                new android.support.v7.app.AlertDialog.Builder(this).setMessage("Unsaved analysis results found. Starting a new Analysis will drop recent progess. Do you want to proceede?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startActivity(new Intent(MainActivity.this, NewAnalysis.class));
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new android.support.v7.app.AlertDialog.Builder(MainActivity.this).setMessage("Do you want to see the previous unsaved result?")
                                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                startActivity(new Intent(MainActivity.this, BarChartActivity.class));
                                            }
                                        })
                                        .setNegativeButton("No", null).show();
                            }
                        }).show();


            }else{
                startActivity(new Intent(MainActivity.this, NewAnalysis.class));
            }
        }

        if(view == this.imprintBtn){
            //Intent ii=new Intent(MainActivity.this, ImprintActivity.class);
            Intent ii=new Intent(MainActivity.this, HelpActivity.class);
            ii.putExtra(HelpActivity.VIEW_MODE,HelpActivity.IMPRINT);
            startActivity(ii);
        }

        if(view == this.helpBtn){
            Intent hi=new Intent(MainActivity.this, HelpActivity.class);
            hi.putExtra(HelpActivity.VIEW_MODE,HelpActivity.HELP);
            startActivity(hi);
        }

        if(view == this.unsavedResultBtn){
            startActivity(new Intent(MainActivity.this, BarChartActivity.class));
        }


    }
}