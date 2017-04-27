package com.example.mick.emotionanalizer;

import android.content.Context;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Mick on 27.04.2017.
 */
public class AnalizationHelper {

    private static AnalizationHelper instance;

    static{
        instance = new AnalizationHelper();
    }

    private AnalizationHelper(){}

    public static AnalizationHelper INSTANCE(){
        return AnalizationHelper.instance;
    }

    public void init(Context context){
        Log.d("analizer","init analizer");
        EmotionAnalizer.INSTANCE.init(context);
        Log.d("analizer","init analizer successfull");
    }

    private boolean isRunning = false;

    //TODO: very importatn!! remove these keys
    private String consumerKey = "WTSdBrmGi9X3GlSW1OTMb0Xhj";
    private String consumerSecret = "2xPN57eBDYeqWPKVpmG95XrwjX6fq79fUS2ilC7sYNWEc25xIL";
    private String accessToken = "791421180129476609-Ld84Ity8cdq9i0a7GawzS1OxKzGYWtz";
    private String AccessTokenSecret = "lOlxp3j603JJ4fZPJTl08PhEPnAZ30uJ6TmpYVwWCct1m";

    private TwitterCrawler twitterCrawler = null;



    public boolean isRunning() {
        return isRunning;
    }

   /* public void setRunning(boolean running) {
        isRunning = running;
    }*/

    public String getConsumerKey() {
        return consumerKey;
    }

    public void setConsumerKey(String consumerKey) {
        this.consumerKey = consumerKey;
    }

    public String getConsumerSecret() {
        return consumerSecret;
    }

    public void setConsumerSecret(String consumerSecret) {
        this.consumerSecret = consumerSecret;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getAccessTokenSecret() {
        return AccessTokenSecret;
    }

    public void setAccessTokenSecret(String accessTokenSecret) {
        AccessTokenSecret = accessTokenSecret;
    }

    public void stopAnalization() {
       this.isRunning = false;
        this.twitterCrawler = null;
    }

    private AnalizationResult currentResult = null;

    public  void startAnalization(String keywords){
        Log.d("analizer","start analizer!");
        this.twitterCrawler = new TwitterCrawler();

        this.isRunning = true;
        try {
            this.twitterCrawler.start(keywords);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //TODO: remove following -> just test
        Timer timer = new Timer();
        timer.schedule(new SayHello(this.twitterCrawler), 0, 5000);
       // timer.cancel();
    }
}

class SayHello extends TimerTask {

    private TwitterCrawler crawler;
    public SayHello(TwitterCrawler c){
        crawler = c;
    }

    public void run() {
        Log.d("analizer","Current WORDCOUNT: "+crawler.getCurrentResult().wordCount);
        System.out.println(crawler.getCurrentResult().wordCount);
    }
}
