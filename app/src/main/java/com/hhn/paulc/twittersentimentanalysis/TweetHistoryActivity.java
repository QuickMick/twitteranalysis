package com.hhn.paulc.twittersentimentanalysis;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.hhn.mick.emotionanalizer.AnalizationHelper;
import com.hhn.mick.emotionanalizer.EmotionWeighting;
import com.hhn.mick.emotionanalizer.TweetCache;

import java.util.ArrayList;

public class TweetHistoryActivity extends AppCompatActivity implements View.OnClickListener{

    private ListView tweetListLv;
    private Button refreshBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweet_history);

        this.refreshBtn = (Button)findViewById(R.id.reloadtweetsbtn);
        this.refreshBtn.setOnClickListener(this);

        this.tweetListLv = (ListView)findViewById(R.id.tweetlistlv);
        this.updateList();
    }

    @Override
    public void onClick(View v) {
        if(v == this.refreshBtn){
          //  if(AnalizationHelper.INSTANCE().isRunning()) {
                this.updateList();
          //  }
        }
    }

/*
    private void updateList(){
        if(!AnalizationHelper.INSTANCE().isRunning()){
            finish();
        }

        TweetCache[] tcList = AnalizationHelper.INSTANCE().getFinalResult().getTweetSteps();

        final ArrayList<TwoLinedListItem> visibleItems = new ArrayList<TwoLinedListItem>();


        for(TweetCache tc:tcList){

            TwoLinedListItem cur= new TwoLinedListItem();
            visibleItems.add(cur);
            cur.caption = tc.tweetText;

            if(tc.tweetTokens==null || tc.tweetTokens.length <=0){
                cur.subtext="Anlysis of this tweet not possible";
                continue;
            }

            StringBuilder buffer = new StringBuilder();
            for (String each : tc.tweetTokens)
                buffer.append(",").append(each);

            if(buffer.length() <=0){
                cur.subtext="Anlysis of this tweet not possible";
                continue;
            }
            cur.subtext = "Analized Tokens: \n[ "+buffer.deleteCharAt(0).toString()+" ]\n";

            EmotionWeighting sd = tc.weighting;
            cur.subtext = cur.subtext+"Sentiment Score: \nPositive("+sd.sentiment_positive+"), Negative("+sd.sentiment_negative+")\n";
            cur.subtext = cur.subtext+"Emotion Score: \nAnger("+sd.anger+"), Anticipation("+sd.anticipation+"), Disgust("+sd.disgust+"), Fear("+sd.fear+"), Joy("+sd.joy+"), Sadness("+sd.sadness+"), Surprise("+sd.surprise+"), Trust("+sd.trust+")";
        }

        final TwoLinedListItem[] items = visibleItems.toArray(new TwoLinedListItem[visibleItems.size()]);
        ArrayAdapter adapter = new ArrayAdapter<TwoLinedListItem>(this, android.R.layout.simple_list_item_2, android.R.id.text1,items ) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                TextView text2 = (TextView) view.findViewById(android.R.id.text2);

                text1.setText(items[position].caption);
                text2.setText(items[position].subtext);
                return view;
            }
        };

        this.tweetListLv.setAdapter(adapter);
    }*/

    private class TweetItem{
        public String text;
        public String tokens;
        public String sentiment;
        public String emotion;
    }

    private void updateList(){
        if(!AnalizationHelper.INSTANCE().isRunning()){
            finish();
        }

        TweetCache[] tcList = AnalizationHelper.INSTANCE().getFinalResult().getTweetSteps();

        final ArrayList<TweetItem> visibleItems = new ArrayList<TweetItem>();


        for(TweetCache tc:tcList){

            TweetItem cur= new TweetItem();
            visibleItems.add(cur);
            cur.text = tc.tweetText;

            if(tc.tweetTokens==null || tc.tweetTokens.length <=0){
                cur.tokens="no usefull tokens found";
                cur.emotion="Anlysis of this tweet not possible";
                cur.sentiment="Anlysis of this tweet not possible";
                continue;
            }

            StringBuilder buffer = new StringBuilder();
            for (String each : tc.tweetTokens)
                buffer.append(",").append(each);

            cur.tokens = "[ "+buffer.deleteCharAt(0).toString()+" ]";

            EmotionWeighting sd = tc.weighting;
           // cur.sentiment = "Positive("+sd.sentiment_positive+"), Negative("+sd.sentiment_negative+")";
           // cur.emotion = "Anger("+sd.anger+"), Anticipation("+sd.anticipation+"), Disgust("+sd.disgust+"), Fear("+sd.fear+"), Joy("+sd.joy+"), Sadness("+sd.sadness+"), Surprise("+sd.surprise+"), Trust("+sd.trust+")";
            cur.sentiment = "Positive ("+sd.sentiment_positive+") \nNegative ("+sd.sentiment_negative+")";
            cur.emotion = "Anger ("+sd.anger+") \nAnticipation ("+sd.anticipation+") \nDisgust ("+sd.disgust+") \nFear ("+sd.fear+") \nJoy ("+sd.joy+") \nSadness ("+sd.sadness+") \nSurprise ("+sd.surprise+") \nTrust ("+sd.trust+")";


        }

        final TweetItem[] items = visibleItems.toArray(new TweetItem[visibleItems.size()]);
        ArrayAdapter adapter = new ArrayAdapter<TweetItem>(this, R.layout.list_view_tweets,R.id.tweettextlbl, items ) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text = (TextView) view.findViewById(R.id.tweettextlbl);
                TextView token = (TextView) view.findViewById(R.id.tokenslbl);
                TextView sentiment = (TextView) view.findViewById(R.id.sentimentlbl);
                TextView emotion = (TextView) view.findViewById(R.id.emotionlbl);

                text.setText(items[position].text);
                token.setText(items[position].tokens);
                sentiment.setText(items[position].sentiment);
                emotion.setText(items[position].emotion);
                return view;
            }
        };

        this.tweetListLv.setAdapter(adapter);
    }
}
