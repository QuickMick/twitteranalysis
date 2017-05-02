package com.example.graphs;

import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mick.emotionanalizer.AnalizationHelper;
import com.example.mick.emotionanalizer.AnalizationResult;
import com.example.mick.emotionanalizer.EmotionWeighting;
import com.example.mick.service.Constants;
import com.example.paulc.twittersentimentanalysis.R;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.json.JSONException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

public class HistoryTimelineActivity extends AppCompatActivity  implements View.OnClickListener{

    LineGraphSeries<DataPoint> anger_series = new LineGraphSeries<DataPoint>();
    LineGraphSeries<DataPoint> anticipation_series = new LineGraphSeries<DataPoint>();
    LineGraphSeries<DataPoint> disgust_series = new LineGraphSeries<DataPoint>();
    LineGraphSeries<DataPoint> fear_series = new LineGraphSeries<DataPoint>();
    LineGraphSeries<DataPoint> joy_series = new LineGraphSeries<DataPoint>();
    LineGraphSeries<DataPoint> sadness_series = new LineGraphSeries<DataPoint>();
    LineGraphSeries<DataPoint> surprise_series = new LineGraphSeries<DataPoint>();
    LineGraphSeries<DataPoint> trust_series = new LineGraphSeries<DataPoint>();

    LineGraphSeries<DataPoint> positive_series = new LineGraphSeries<DataPoint>();
    LineGraphSeries<DataPoint> negative_series = new LineGraphSeries<DataPoint>();

    private GraphView graph;
    private boolean showSentiment=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_timeline);
        this.graph = (GraphView) findViewById(R.id.graph);

        this.initGraph();


        final ProgressDialog dialog = ProgressDialog.show(HistoryTimelineActivity.this, "","Loading History Data. Please wait...", true);
        new AsyncTask<Void, Void, DisplayValue[]>() {
            @Override
            protected DisplayValue[] doInBackground(Void... params) {
                return HistoryTimelineActivity.this.init();
            }

            @Override
            protected void onPostExecute(DisplayValue[] r){
                if(r == null){
                    Toast.makeText(HistoryTimelineActivity.this, "Error while loading files",Toast.LENGTH_SHORT).show();
                    finish();
                }
                HistoryTimelineActivity.this.updateGraph(r);
                dialog.dismiss();
            }
        }.execute();

    }

    private DisplayValue[] init(){
        File[] files = this.listFiles();

        if(files.length <=0) return null;

        ArrayList<DisplayValue> result = new ArrayList<DisplayValue>();
        try {
            for(File f :files){

                AnalizationResult ar = AnalizationResult.createFromJSON(this.loadJSONFromFolder(f));
                result.add(new DisplayValue(ar.weigthing,ar.startDate));
            }

        } catch (JSONException e) {
            e.printStackTrace();

            //finish();
            return null;
        }

        Collections.sort(result, new Comparator<DisplayValue>() {
            public int compare(DisplayValue o1, DisplayValue o2) {
                return o1.startDate.compareTo(o2.startDate);
            }
        });

        return result.toArray(new DisplayValue[result.size()]);

    }



    private void initGraph(){
        this.graph = (GraphView) findViewById(R.id.graph);
        this.graph.setOnClickListener(this);
        //  this.updateGraph();

        anger_series.setTitle(Constants.DETAIL_GRAPH.EMOTION_NAME_ANGER);
        anticipation_series.setTitle(Constants.DETAIL_GRAPH.EMOTION_NAME_ANTICIPATION);
        disgust_series.setTitle(Constants.DETAIL_GRAPH.EMOTION_NAME_DISGUST);
        fear_series.setTitle(Constants.DETAIL_GRAPH.EMOTION_NAME_FEAR);
        joy_series.setTitle(Constants.DETAIL_GRAPH.EMOTION_NAME_JOY);
        sadness_series.setTitle(Constants.DETAIL_GRAPH.EMOTION_NAME_SADNESS);
        surprise_series.setTitle(Constants.DETAIL_GRAPH.EMOTION_NAME_SURPRISE);
        trust_series.setTitle(Constants.DETAIL_GRAPH.EMOTION_NAME_TRUST);

        positive_series.setTitle(Constants.DETAIL_GRAPH.EMOTION_NAME_POSITIVE);
        negative_series.setTitle(Constants.DETAIL_GRAPH.EMOTION_NAME_NEGATIVE);


        anger_series.setColor(Color.parseColor("#d62032"));
        anticipation_series.setColor(Color.parseColor("#f46314"));
        disgust_series.setColor(Color.parseColor("#bf1899"));
        fear_series.setColor(Color.parseColor("#009661"));
        joy_series.setColor(Color.parseColor("#f8b313"));
        sadness_series.setColor(Color.parseColor("#3e7fc7"));
        surprise_series.setColor(Color.parseColor("#25c0ce"));
        trust_series.setColor(Color.parseColor("#99bc43"));

        positive_series.setColor(Color.parseColor("#f8b313"));
        negative_series.setColor(Color.parseColor("#999999"));


        this.addSeries();

        graph.getLegendRenderer().setVisible(true);
        graph.getLegendRenderer().setBackgroundColor(Color.argb(30,0,0,0));
        graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);


        graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(this, new SimpleDateFormat("dd.MM.yyyy\nHH:mm:ss")));

/*
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(1);
        graph.getViewport().setXAxisBoundsManual(true);*/
        graph.getGridLabelRenderer().setNumHorizontalLabels(4);

        graph.getViewport().setScalable(false);
        graph.getViewport().setScrollable(false);
        graph.getViewport().setScalableY(false);

/*
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(0);
        graph.getViewport().setMaxY(100);

        graph.getViewport().setScalable(true);
        graph.getViewport().setScrollable(true);*/

        //  graph.getViewport().setScalableY(false);
    }


    private void addSeries(){
        graph.removeAllSeries();

        if(!this.showSentiment) {
            graph.addSeries(anger_series);
            graph.addSeries(anticipation_series);
            graph.addSeries(disgust_series);
            graph.addSeries(fear_series);
            graph.addSeries(joy_series);
            graph.addSeries(sadness_series);
            graph.addSeries(surprise_series);
            graph.addSeries(trust_series);
        }else{
            graph.addSeries(positive_series);
            graph.addSeries(negative_series);
        }
    }

    private void updateGraph(DisplayValue[] steps){

        //  this.graph.removeAllSeries();

        // Log.d("analize_result",steps ==null?"UPDATE NO DATA":"UPDATE "+steps.length);
        if(steps == null || steps.length <2) return; //no data available

        DataPoint[] anger = new DataPoint[steps.length];
        DataPoint[] anticipation = new DataPoint[steps.length];
        DataPoint[] disgust = new DataPoint[steps.length];
        DataPoint[] fear = new DataPoint[steps.length];
        DataPoint[] joy = new DataPoint[steps.length];
        DataPoint[] sadness = new DataPoint[steps.length];
        DataPoint[] surprise = new DataPoint[steps.length];
        DataPoint[] trust = new DataPoint[steps.length];

        DataPoint[] positive = new DataPoint[steps.length];
        DataPoint[] negative = new DataPoint[steps.length];

        for(int i=0; i<steps.length;i++){
            //  for(int i=steps.length-1; i>=0;i--){
            float total = steps[i].weigthing.getTotalEmotion();
            if(total ==0)total=1;
            anger[i] = new DataPoint(steps[i].startDate,(((float)steps[i].weigthing.anger)/total)*100f);
            anticipation[i] = new DataPoint(steps[i].startDate,(((float)steps[i].weigthing.anticipation)/total)*100f);
            disgust[i] = new DataPoint(steps[i].startDate,(((float)steps[i].weigthing.disgust)/total)*100f);
            fear[i] = new DataPoint(steps[i].startDate,(((float)steps[i].weigthing.fear)/total)*100f);
            joy[i] = new DataPoint(steps[i].startDate,(((float)steps[i].weigthing.joy)/total)*100f);
            sadness[i] = new DataPoint(steps[i].startDate,(((float)steps[i].weigthing.sadness)/total)*100f);
            surprise[i] = new DataPoint(steps[i].startDate,(((float)steps[i].weigthing.surprise)/total)*100f);
            trust[i] = new DataPoint(steps[i].startDate,(((float)steps[i].weigthing.trust)/total)*100f);

            float total_s = steps[i].weigthing.getTotalSentiment();
            positive[i] = new DataPoint(steps[i].startDate,(((float)steps[i].weigthing.sentiment_positive)/total_s)*100f);
            negative[i] = new DataPoint(steps[i].startDate,(((float)steps[i].weigthing.sentiment_negative)/total_s)*100f);
        }

        anger_series.resetData(anger);
        anticipation_series.resetData(anticipation);
        disgust_series.resetData(disgust);
        fear_series.resetData(fear);
        joy_series.resetData(joy);
        sadness_series.resetData(sadness);
        surprise_series.resetData(surprise);
        trust_series.resetData(trust);

        positive_series.resetData(positive);
        negative_series.resetData(negative);

        graph.getViewport().setMinX(anger_series.getLowestValueX());
        graph.getViewport().setMaxX(anger_series.getHighestValueX());

    }


    private File[] listFiles(){
        File mydir = new File(Environment.getExternalStorageDirectory(), AnalizationHelper.getAnalyzation_folder());
        if(!mydir.exists()) {
            return new File[0];
        }

        File[] tmpFiles = mydir.listFiles();
        ArrayList<File> cur = new ArrayList<File>();
        for(File f :tmpFiles){
            if(f.getAbsolutePath().endsWith(".json")) {
                cur.add(f);
            }
        }

        Collections.sort(cur);
        Collections.reverse(cur);

        return cur.toArray(new File[cur.size()]);
    }

    public String loadJSONFromFolder(File file) {
        String json = null;
        try {
            InputStream is = new FileInputStream(file);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    public static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    private void requistPermission(){
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
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "permission:WRITE_EXTERNAL_STORAGE: NOT granted!",Toast.LENGTH_SHORT).show();
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }

    @Override
    public void onClick(View v) {
        if(v == this.graph){
            this.showSentiment = !this.showSentiment;

            this.addSeries();
        }
    }

    class DisplayValue{
        EmotionWeighting weigthing;
        Date startDate;
        public DisplayValue(EmotionWeighting w, Date d){
            this.weigthing = w;
            this.startDate = d;
        }
    }
}
