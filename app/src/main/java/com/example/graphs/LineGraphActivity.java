package com.example.graphs;

import android.app.Activity;
import android.graphics.Color;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.mick.emotionanalizer.AnalizationHelper;
import com.example.mick.emotionanalizer.AnalizationResult;
import com.example.mick.service.Constants;
import com.example.paulc.twittersentimentanalysis.R;
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Timer;
import java.util.TimerTask;

public class LineGraphActivity extends AppCompatActivity implements View.OnClickListener {

    private GraphView graph;

    public static final int REFRESH_TIME = 1000;

    private boolean showSentiment = false;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_graph);

        this.initGraph();
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


        graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(this, new SimpleDateFormat("HH:mm:ss")));


        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(1);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getGridLabelRenderer().setNumHorizontalLabels(4);


        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(0);
        graph.getViewport().setMaxY(100);

        graph.getViewport().setScalable(false);
        graph.getViewport().setScrollable(false);

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


    private void updateGraph(){
        if(!AnalizationHelper.INSTANCE().isRunning()){
            mHandler.removeCallbacks(this.refreshView);
            return;
        }

      //  this.graph.removeAllSeries();

        AnalizationResult[] steps = AnalizationHelper.INSTANCE().getSteps();
       // Log.d("analize_result",steps ==null?"UPDATE NO DATA":"UPDATE "+steps.length);
        if(steps == null || steps.length <3) return; //no data available

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

      //  Log.d("hv",anger_series.getLowestValueX()+"-"+anger_series.getHighestValueX()+" --> "+( anger_series.getHighestValueX()-anger_series.getLowestValueX()));


     //   graph.getGridLabelRenderer().setNumHorizontalLabels(5);
        graph.getViewport().setMinX(anger_series.getLowestValueX());
        graph.getViewport().setMaxX(anger_series.getHighestValueX());
     //   graph.getViewport().setXAxisBoundsManual(true);

      /*  graph.scrollTo(steps.length-1,100);
        graph.computeScroll();*/

    }



    Handler mHandler;
    Runnable refreshView = new Runnable() {
        @Override
        public void run() {
            LineGraphActivity.this.updateGraph();
            mHandler.postDelayed(this, REFRESH_TIME);
        }
    };

    @Override
    public void onResume() {
        super.onResume();

        mHandler = new Handler(this.getMainLooper());


        if(AnalizationHelper.INSTANCE().isRunning()) {
            mHandler.postDelayed(this.refreshView, REFRESH_TIME);
        }
    }

    @Override
    public void onPause() {
        mHandler.removeCallbacks(this.refreshView);
        super.onPause();
    }

    @Override
    public void onClick(View v) {
        if(v == this.graph){
            this.showSentiment = !this.showSentiment;

            this.addSeries();
        }
    }
}
