package com.example.graphs;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.mick.emotionanalizer.AnalizationHelper;
import com.example.mick.emotionanalizer.EmotionWeighting;
import com.example.mick.service.Constants;
import com.example.paulc.twittersentimentanalysis.R;
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.ValueDependentColor;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;

import java.util.Timer;
import java.util.TimerTask;

public class BarChartActivity extends AppCompatActivity implements View.OnClickListener{

    public static final int REFRESH_INTERVALL_MS = 1000; // refresh graph every 5 secons

    private GraphView graph;

    private boolean showSentiment = false;

    private Button changeViewBtn;

    private EmotionWeighting currentData = new EmotionWeighting();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bar_chart);

        changeViewBtn = (Button) findViewById(R.id.changeviewbtn);
        changeViewBtn.setOnClickListener(this);

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







        // start the view
        Intent i =getIntent();
        switch(i.getStringExtra(Constants.ANALIZATION.DIAGRAM_MODE)){
            case Constants.ANALIZATION.MODE_ANALIZATION_RUNNING:
                this.startRunningMode();
                break;
            case Constants.ANALIZATION.MODE_ANALIZATION_STOPPED:
                break;
            case Constants.ANALIZATION.MODE_HISTORY:
                String date = i.getStringExtra(Constants.ANALIZATION.MODE_HISTORY_DATE);
                break;
        }
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
    }

    private Timer refreshTimer;
    private void startRunningMode(){

        if(this.refreshTimer != null) refreshTimer.cancel();
        this.currentData = AnalizationHelper.INSTANCE().getFinalResult().weigthing;
        this.updateGraphData();

        this.refreshTimer = new Timer();
        this.refreshTimer.schedule(new TimerTask(){

            @Override
            public void run() {
                BarChartActivity.this.currentData = AnalizationHelper.INSTANCE().getFinalResult().weigthing;
                BarChartActivity.this.updateGraphData();

                // stop refresehing, if analization has stopped.
                if(!AnalizationHelper.INSTANCE().isRunning()){
                    BarChartActivity.this.refreshTimer.cancel();
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
        }
    }
}
