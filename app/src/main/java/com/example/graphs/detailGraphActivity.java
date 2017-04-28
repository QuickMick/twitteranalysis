package com.example.graphs;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.example.mick.emotionanalizer.AnalizationHelper;
import com.example.mick.emotionanalizer.EmotionWeighting;
import com.example.mick.service.Constants;
import com.example.paulc.twittersentimentanalysis.CircleView;
import com.example.paulc.twittersentimentanalysis.R;
import com.example.paulc.twittersentimentanalysis.SemiCircleView;

public class DetailGraphActivity extends AppCompatActivity {

    private String currentEmotion="";

    private CircleView circleView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_graph);

        circleView = (CircleView) findViewById(R.id.emotionpercent);
        //circleView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);


        this.currentEmotion = this.getIntent().getStringExtra(Constants.DETAIL_GRAPH.EMOTION_NAME);


        EmotionWeighting ew = AnalizationHelper.INSTANCE().getFinalResult().weigthing;
this.currentEmotion = Constants.DETAIL_GRAPH.EMOTION_NAME_ANTICIPATION;
        float em=0;
        switch (this.currentEmotion){
            case Constants.DETAIL_GRAPH.EMOTION_NAME_ANGER:
                em=ew.anger;
                break;
            case Constants.DETAIL_GRAPH.EMOTION_NAME_ANTICIPATION:
                em=ew.anticipation;
                break;
            case Constants.DETAIL_GRAPH.EMOTION_NAME_DISGUST:
                em=ew.disgust;
                break;
            case Constants.DETAIL_GRAPH.EMOTION_NAME_FEAR:
                em=ew.fear;
                break;
            case Constants.DETAIL_GRAPH.EMOTION_NAME_JOY:
                em=ew.joy;
                break;
            case Constants.DETAIL_GRAPH.EMOTION_NAME_SADNESS:
                em=ew.sadness;
                break;
            case Constants.DETAIL_GRAPH.EMOTION_NAME_SURPRISE:
                em=ew.surprise;
                break;
            case Constants.DETAIL_GRAPH.EMOTION_NAME_TRUST:
                em=ew.trust;
                break;
            case Constants.DETAIL_GRAPH.EMOTION_NAME_POSITIVE:
                em=ew.sentiment_positive;
                break;
            case Constants.DETAIL_GRAPH.EMOTION_NAME_NEGATIVE:
                em=ew.sentiment_negative;
                break;

        }
//        ((float)ew.anticipation)

        circleView.setStart(0);
       circleView.setEnd( (int) (em/((float)ew.getTotalEmotion())*100f));
    }
}
