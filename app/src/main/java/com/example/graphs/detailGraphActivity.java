package com.example.graphs;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.mick.emotionanalizer.AnalizationHelper;
import com.example.mick.emotionanalizer.AnalizationResult;
import com.example.mick.emotionanalizer.EmotionWeighting;
import com.example.mick.service.Constants;
import com.example.paulc.twittersentimentanalysis.CircleView;
import com.example.paulc.twittersentimentanalysis.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * TODO: should this reload while analization is running?
 */
public class DetailGraphActivity extends AppCompatActivity {

    public static final int MAX_VISIBLE_WORDCOUNT = 30;

    private String currentEmotion="";
    private HashMap<String,Integer> currentWordlist;
    private CircleView circleView;

    private TextView emotionTitleLbl;

    private ListView wordListLv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_graph);

        this.circleView = (CircleView) findViewById(R.id.emotionpercent);
        this.emotionTitleLbl = (TextView)findViewById(R.id.emotiontitlelbl);
        this.wordListLv = (ListView)findViewById(R.id.wordlistlv);

        //circleView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);


        this.currentEmotion = this.getIntent().getStringExtra(Constants.DETAIL_GRAPH.EMOTION_NAME);

        Log.d("AppD","selected detail emotion: "+this.currentEmotion);


        //this.currentEmotion = Constants.DETAIL_GRAPH.EMOTION_NAME_ANTICIPATION;

        this.init(AnalizationHelper.INSTANCE().getFinalResult());

    }

    private void init(AnalizationResult ar) {

        EmotionWeighting ew = ar.weigthing;

        String captionText = this.currentEmotion;

        float em=0;
        switch (this.currentEmotion){
            case Constants.DETAIL_GRAPH.EMOTION_NAME_ANGER:
                em=ew.anger;
                currentWordlist= ar.wordStatistic_anger;
                break;
            case Constants.DETAIL_GRAPH.EMOTION_NAME_ANTICIPATION:
                em=ew.anticipation;
                currentWordlist= ar.wordStatistic_anticipation;
                break;
            case Constants.DETAIL_GRAPH.EMOTION_NAME_DISGUST:
                currentWordlist= ar.wordStatistic_disgust;
                em=ew.disgust;
                break;
            case Constants.DETAIL_GRAPH.EMOTION_NAME_FEAR:
                currentWordlist= ar.wordStatistic_fear;
                em=ew.fear;
                break;
            case Constants.DETAIL_GRAPH.EMOTION_NAME_JOY:
                currentWordlist= ar.wordStatistic_joy;
                em=ew.joy;
                break;
            case Constants.DETAIL_GRAPH.EMOTION_NAME_SADNESS:
                currentWordlist= ar.wordStatistic_sadness;
                em=ew.sadness;
                break;
            case Constants.DETAIL_GRAPH.EMOTION_NAME_SURPRISE:
                currentWordlist= ar.wordStatistic_surprise;
                em=ew.surprise;
                break;
            case Constants.DETAIL_GRAPH.EMOTION_NAME_TRUST:
                currentWordlist= ar.wordStatistic_trust;
                em=ew.trust;
                break;
            case Constants.DETAIL_GRAPH.EMOTION_NAME_POSITIVE:
                currentWordlist= ar.wordStatistic_sentiment_positive;
                em=ew.sentiment_positive;
                break;
            case Constants.DETAIL_GRAPH.EMOTION_NAME_NEGATIVE:
                currentWordlist= ar.wordStatistic_sentiment_negative;
                em=ew.sentiment_negative;
            case Constants.DETAIL_GRAPH.ALL_WORDS:
                currentWordlist= ar.wordStatistic_all;
                em=ar.wordCountAnalized;
                captionText+=" | Total words: "+ar.wordCount+" - Analized: "+ar.wordCountAnalized;

                break;
        }

        this.emotionTitleLbl.setText(captionText);

        float total =0;

        if(this.currentEmotion.equals(Constants.DETAIL_GRAPH.EMOTION_NAME_NEGATIVE) || this.currentEmotion.equals(Constants.DETAIL_GRAPH.EMOTION_NAME_POSITIVE)){
            total = ew.getTotalSentiment();
        }else if(this.currentEmotion.equals(Constants.DETAIL_GRAPH.ALL_WORDS)) {
            total = ar.wordCount;
            Log.d("AppD","words: "+total+" analized: "+em);
        }else{
            total = ew.getTotalEmotion();
        }
        circleView.setPercent((em/total)*100f);

        // init the wordlist with following section

        List<Map.Entry<String,Integer>> entries = new ArrayList<Map.Entry<String,Integer>>( this.currentWordlist.entrySet());

        Collections.sort(
                entries
                ,   new Comparator<Map.Entry<String,Integer>>() {
                    public int compare(Map.Entry<String,Integer> a, Map.Entry<String,Integer> b) {
                        if(b.getValue() > a.getValue()) return 1;
                        else if(b.getValue() < a.getValue()) return -1;

                        return 0;
                    }
                }
        );

        ArrayList<String> visibleItems = new ArrayList<String>();
        ArrayList<String> keyWords = new ArrayList<String>(Arrays.asList(ar.getKewords()));

        int i=0;
        for (Map.Entry<String,Integer> e : entries) {
            if(keyWords.contains(e.getKey())) continue; //filter the searched keywords --> becuase they are in every tweet

            visibleItems.add(e.getKey()+" : "+e.getValue());    // TODO: @paul: i think it would look better if we would split the content to two text fields and align them
            i++;
            // showing all data would propably kill the list
            if(i > DetailGraphActivity.MAX_VISIBLE_WORDCOUNT){
                break;
            }
        }

        ArrayAdapter adapter = new ArrayAdapter<String>(this,R.layout.list_view_emotions,visibleItems.toArray(new String[visibleItems.size()]));
        this.wordListLv.setAdapter(adapter);
    }
}
/*
final Handler mainHandler = new Handler(this.getMainLooper());
        new Timer().schedule(new TimerTask(){

            @Override
            public void run() {
        DetailGraphActivity.this.x+=5;
                if(x>=100) x=0;
                Log.d("AppD","TESTPERCENT: "+x+"");


                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("AppD","TESTPERCENT: redraw");
                        circleView.setPercent(x);
                        //getWindow().getDecorView().findViewById(android.R.id.content).invalidate();
                        DetailGraphActivity.this.circleView.invalidate();
                    } // This is your code
                });

            }
        }, 0, 500);
 */