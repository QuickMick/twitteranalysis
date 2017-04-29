package com.example.graphs;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mick.emotionanalizer.AnalizationHelper;
import com.example.mick.emotionanalizer.AnalizationResult;
import com.example.mick.emotionanalizer.EmotionWeighting;
import com.example.mick.service.Constants;
import com.example.mick.service.ForegroundService;
import com.example.paulc.twittersentimentanalysis.R;
import com.example.paulc.twittersentimentanalysis.Settings;
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.ValueDependentColor;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class BarChartActivity extends AppCompatActivity implements View.OnClickListener{

    public static final int REFRESH_INTERVALL_MS = 1000; // refresh graph every 5 secons

    private GraphView graph;

    private boolean showSentiment = false;

    private Button changeViewBtn;

    private Button stopAnalysisBtn;

    private Button saveAnalysisBtn;

    private Button showDetailsBtn;

    private EmotionWeighting currentData = new EmotionWeighting();

    private AnalizationResult ar = new AnalizationResult();

    private TextView tweetCountLbl, wordCountLbl,sentenceCountLbl, usedKeywordsLbl;

    /**
     * needed for the foregroudnservice, to check if he should start this activity or not
     */
    private static boolean active = false;

    public static boolean isActive(){
        return active;
    }

    @Override
    public void onStart() {
        super.onStart();
        active = true;
    }

    @Override
    public void onStop() {
        super.onStop();
        active = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bar_chart);

        changeViewBtn = (Button) findViewById(R.id.changeviewbtn);
        changeViewBtn.setOnClickListener(this);

        this.stopAnalysisBtn = (Button) findViewById(R.id.stopanalysisbtn);
        this.stopAnalysisBtn.setOnClickListener(this);

        this.tweetCountLbl = (TextView)findViewById(R.id.tweetcountlbl);
        this.wordCountLbl = (TextView)findViewById(R.id.wordcountlbl);
        this.sentenceCountLbl = (TextView)findViewById(R.id.sentencecountlbl);
        this.usedKeywordsLbl = (TextView)findViewById(R.id.tweetkeywordslbl);


        this.saveAnalysisBtn = (Button) findViewById(R.id.saveanalysisbtn);
        this.saveAnalysisBtn.setOnClickListener(this);

        this.showDetailsBtn = (Button) findViewById(R.id.showdetailsbtn);
        this.showDetailsBtn.setOnClickListener(this);


        this.graph = (GraphView) findViewById(R.id.graph);



        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(0);
        graph.getViewport().setMaxY(100);

      /*  graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(4);
        graph.getViewport().setMaxX(80);*/

        // enable scaling and scrolling
        graph.getViewport().setScalable(false);
        graph.getViewport().setScalableY(false);


      /*  graph.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
            @Override
            public String formatLabel(double value, boolean isValueX) {
                if (isValueX) {
                    // show normal x values
                    return super.formatLabel(value, isValueX);
                } else {
                    // show currency for y values
                    return super.formatLabel(value, isValueX) + " €";
                }
            }
        });*/





        this.stopAnalysisBtn.setVisibility(Button.INVISIBLE);
        this.saveAnalysisBtn.setVisibility(Button.INVISIBLE);
        // start the view
        Intent i =getIntent();
        switch(i.getStringExtra(Constants.ANALIZATION.DIAGRAM_MODE)){
            case Constants.ANALIZATION.MODE_ANALIZATION_RUNNING:
                this.usedKeywordsLbl.setText(Arrays.toString(AnalizationHelper.INSTANCE().getFinalResult().getKewords()));
                this.startRunningMode();
                this.stopAnalysisBtn.setVisibility(Button.VISIBLE);
                break;
            case Constants.ANALIZATION.MODE_ANALIZATION_STOPPED:
                this.usedKeywordsLbl.setText(Arrays.toString(AnalizationHelper.INSTANCE().getFinalResult().getKewords()));
                this.currentData = AnalizationHelper.INSTANCE().getFinalResult().weigthing;
                this.ar = AnalizationHelper.INSTANCE().getFinalResult();
                this.saveAnalysisBtn.setVisibility(Button.VISIBLE);
                this.updateGraphData();
                break;
            case Constants.ANALIZATION.MODE_HISTORY:
                String date = i.getStringExtra(Constants.ANALIZATION.MODE_HISTORY_DATE);

                //TODO: load old data
                break;
        }



        // following code is required for receiving messages from the foregroudn service
        // currently it is just used to remove the "stop analysis button"
        LocalBroadcastManager bManager = LocalBroadcastManager.getInstance(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.ACTION.ANALIZATION);
        bManager.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getStringExtra("MSG").equals(Constants.ANALIZATION.BROADCAST_ANALIZATION_STOPPED)){
                    BarChartActivity.this.stopAnalysisBtn.setVisibility(Button.INVISIBLE);
                    BarChartActivity.this.saveAnalysisBtn.setVisibility(Button.VISIBLE);
                }
            }
        }, intentFilter);


    }

    private void updateGraphData(){
        EmotionWeighting ew = this.currentData;
        Log.d("appd","update graph - mode:"+this.showSentiment);

        DataPoint[] dataPoints = null;
        StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(graph);
     //   staticLabelsFormatter.setVerticalLabels(new String[] {"0","10","20","30","40","50","60","70","80","90","100"});
        // create the sentiments graph
        if(this.showSentiment){
            staticLabelsFormatter.setHorizontalLabels(new String[] {"positive","negative"});
            double count_total = ew.sentiment_negative+ew.sentiment_positive;
            if(count_total==0)count_total=1;
            dataPoints=new DataPoint[] {
                    new DataPoint(0, (((double)ew.sentiment_positive)/count_total) *100d),
                    new DataPoint(1, (((double)ew.sentiment_negative)/count_total) *100d)
            };

        }else{ // create the emotion graph
            staticLabelsFormatter.setHorizontalLabels(new String[] {"anger", "anticipation", "disgust","fear", "joy","sadness","surprise","trust"});

            double count_total = ew.anger+ew.anticipation+ew.disgust+ew.fear+ew.joy+ew.sadness+ew.surprise+ew.trust;
            if(count_total==0)count_total=1;//cannot divide by zero
            dataPoints=new DataPoint[] {
                    new DataPoint(0, (((double)ew.anger)/count_total) *100d),
                    new DataPoint(1, (((double)ew.anticipation)/count_total) *100d),
                    new DataPoint(2, (((double)ew.disgust)/count_total) *100d),
                    new DataPoint(3, (((double)ew.fear)/count_total) *100d),
                    new DataPoint(4, (((double)ew.joy)/count_total) *100d),
                    new DataPoint(5, (((double)ew.sadness)/count_total) *100d),
                    new DataPoint(6, (((double)ew.surprise)/count_total) *100d),
                    new DataPoint(7, (((double)ew.trust)/count_total) *100d)
            };
        }

        graph.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);

        BarGraphSeries<DataPoint> series = new BarGraphSeries<>(dataPoints);

        series.setValueDependentColor(new ValueDependentColor<DataPoint>() {
            @Override
            public int get(DataPoint data) {
                return Color.rgb((int) data.getX()*255/4, 255, 100);
            }
        });

        graph.removeAllSeries();
        graph.addSeries(series);

// styling


        series.setSpacing(10);

// draw values on top
        series.setDrawValuesOnTop(true);
        series.setValuesOnTopColor(Color.RED);


        this.tweetCountLbl.setText(this.ar.tweetCount+"");
        this.sentenceCountLbl.setText(this.ar.sentenceCount+"");
        this.wordCountLbl.setText(this.ar.wordCount+"");
    }

    private Timer refreshTimer;


    @Override
    protected void onPause() {
        super.onPause();
        this.refreshTimer.cancel();
    }

    protected void onResume() {
        super.onResume();

        if(AnalizationHelper.INSTANCE().isRunning()) {
            this.startRunningMode();
        }
    }



    private void startRunningMode(){

        if(this.refreshTimer != null) refreshTimer.cancel();
        this.currentData = AnalizationHelper.INSTANCE().getFinalResult().weigthing;
        this.ar = AnalizationHelper.INSTANCE().getFinalResult();
        this.updateGraphData();

        final Handler mainHandler = new Handler(this.getMainLooper());
        this.refreshTimer = new Timer();
        this.refreshTimer.schedule(new TimerTask(){

            @Override
            public void run() {
                BarChartActivity.this.currentData = AnalizationHelper.INSTANCE().getFinalResult().weigthing;
                BarChartActivity.this.ar = AnalizationHelper.INSTANCE().getFinalResult();

                //neeeded to update labels
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        BarChartActivity.this.updateGraphData();
                    }
                });


                // stop refresehing, if analization has stopped.
                if(!AnalizationHelper.INSTANCE().isRunning()){
                    BarChartActivity.this.refreshTimer.cancel();
//                    BarChartActivity.this.stopAnalysisBtn.setVisibility(Button.INVISIBLE);
                }

            }
        }, 0, BarChartActivity.REFRESH_INTERVALL_MS);
    }

    @Override
    public void onClick (View view) {
        // if he presses on Register , call the register user function
        if (view == this.changeViewBtn) {
            this.showSentiment = !this.showSentiment;   //change stat
            this.changeViewBtn.setText(this.showSentiment?"show emotions":"show sentiment");    //change button text

            this.updateGraphData(); //update graph based on the current mode
        }else if( view == this.stopAnalysisBtn){
            this.stopAnalysisBtn.setVisibility(Button.INVISIBLE);

            Intent stopIntent = new Intent(BarChartActivity.this, ForegroundService.class);
            stopIntent.setAction(ForegroundService.STOPFOREGROUND_ACTION);
            startService(stopIntent);
        }else if(view == this.saveAnalysisBtn){
            this.saveCurrentAnalysis();
        }else if(view == this.showDetailsBtn){
            this.showSelectEmotionDetail();
        }
    }

    private void saveCurrentAnalysis() {

        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)){
            Toast.makeText(this, "Error: external storage is unavailable",Toast.LENGTH_SHORT).show();
            return;
        }
        if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            Toast.makeText(this, "Error: external storage is read only.",Toast.LENGTH_SHORT).show();
            return ;
        }
        Log.d("myAppName", "External storage is not read only or unavailable");

        if (ContextCompat.checkSelfPermission(this, // request permission when it is not granted.
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "permission:WRITE_EXTERNAL_STORAGE: NOT granted!",Toast.LENGTH_SHORT).show();
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }


        //TODO: do the writing (following code) in an async task and show a "waiting" symbol - block everything else (also going back)
        AnalizationResult ar = AnalizationHelper.INSTANCE().getFinalResult();
        String json = ar.toJSON();
        //DateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm");

        //return "{\"startDate\":\""+format.format(this.startDate)+"\", "	/
        DateFormat format = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss");
        String fileName = "twitter_"+format.format(ar.startDate)+"_-_"+format.format(ar.endDate)+".json";
        try {
            File mydir = new File(Environment.getExternalStorageDirectory(),"twitter_results");
            if(!mydir.exists()) {
                mydir.mkdirs();
            }

            File myFile = new File(mydir, fileName);
            Log.d("AppD","save file: "+myFile.getAbsolutePath());

            myFile.createNewFile();
            FileOutputStream fOut = new FileOutputStream(myFile);
            OutputStreamWriter myOutWriter =new OutputStreamWriter(fOut);
            myOutWriter.append(json);
            myOutWriter.close();
            fOut.close();
            Toast.makeText(this,"Done writing data to SD Card", Toast.LENGTH_SHORT).show();
            this.saveAnalysisBtn.setVisibility(Button.INVISIBLE);
        }
        catch (Exception e)
        {
            Toast.makeText(this, "Error while saving: "+e.getMessage(),Toast.LENGTH_SHORT).show();
        }


    }








    public static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    /*  public int mkFolder(String folderName){ // make a folder under Environment.DIRECTORY_DCIM
          String state = Environment.getExternalStorageState();
          if (!Environment.MEDIA_MOUNTED.equals(state)){
              Toast.makeText(this, "Error: external storage is unavailable",Toast.LENGTH_SHORT).show();
              return 0;
          }
          if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
              Log.d("myAppName", "Error: external storage is read only.");
              return 0;
          }
          Log.d("myAppName", "External storage is not read only or unavailable");

          if (ContextCompat.checkSelfPermission(this, // request permission when it is not granted.
                  Manifest.permission.WRITE_EXTERNAL_STORAGE)
                  != PackageManager.PERMISSION_GRANTED) {
              Log.d("myAppName", "permission:WRITE_EXTERNAL_STORAGE: NOT granted!");
              // Should we show an explanation?
              if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                      Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                  // Show an expanation to the user *asynchronously* -- don't block
                  // this thread waiting for the user's response! After the user
                  // sees the explanation, try again to request the permission.

              } else {
                  // No explanation needed, we can request the permission.
                  ActivityCompat.requestPermissions(this,
                          new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                          MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);

                  // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                  // app-defined int constant. The callback method gets the
                  // result of the request.
              }
          }



          File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),folderName);
          int result = 0;
          if (folder.exists()) {
              Log.d("myAppName","folder exist:"+folder.toString());
              result = 2; // folder exist
          }else{
              try {
                  if (folder.mkdir()) {
                      Log.d("myAppName", "folder created:" + folder.toString());
                      result = 1; // folder created
                  } else {
                      Log.d("myAppName", "creat folder fails:" + folder.toString());
                      result = 0; // creat folder fails
                  }
              }catch (Exception ecp){
                  ecp.printStackTrace();
              }
          }
          return result;
      }
  */
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
Log.d("test","perission granted");
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {
Log.d("test","no permission");
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void showSelectEmotionDetail(){
        DialogFragment d= new DialogFragment() {
            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState) {
                AlertDialog.Builder builder = new AlertDialog.Builder(BarChartActivity.this);
                builder.setTitle("Select")
                        .setItems(Constants.emotionCommandsAsArray(), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // The 'which' argument contains the index position
                                // of the selected item

                                Intent i = new Intent(BarChartActivity.this, DetailGraphActivity.class);
                                i.putExtra(Constants.DETAIL_GRAPH.EMOTION_NAME, Constants.emotionCommandsAsArray()[which]); //TODO: add a selection popup here
                                startActivity(i);
                            }
                        });
                return builder.create();
            }
        };

        d.show(this.getFragmentManager(),"SELECT_EMOTION_DETAIL");
    }
}
