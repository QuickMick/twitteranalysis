package com.example.mick.emotionanalizer;

import android.content.Context;
import android.util.Log;

import java.util.LinkedList;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Mick on 27.04.2017.
 */
public class AnalizationHelper {

    private static AnalizationHelper instance = new AnalizationHelper();

    public static void recreate(){
        instance = new AnalizationHelper();
    }

    private AnalizationHelper(){}

    public static AnalizationHelper INSTANCE(){
        return AnalizationHelper.instance;
    }

    private boolean isInitialized = false;

    private Object lock = new Object();
    private Object lock2 = new Object();
    private volatile AnalizationResult finalResult = null;
    private volatile LinkedList<AnalizationResult> result_steps = null;


    public synchronized AnalizationResult getFinalResult() {
        synchronized (lock) {
            return finalResult;
        }
    }

    public synchronized AnalizationResult[] getSteps(){
        synchronized (lock2){
            return this.result_steps.toArray(new AnalizationResult[this.result_steps.size()]);
        }
    }

    public synchronized void addNextResultToFinalResult(AnalizationResult lastResult) {
        synchronized (lock) {
            this.finalResult.Add(lastResult);
        }
        synchronized (lock2){
            lastResult.finalize();
            this.result_steps.addLast(lastResult);
        }
    }

    public void init(Context context){
        if(this.isInitialized) return;
        this.isInitialized = true;
        Log.d("analizer","init analizer");

        Log.d("analizer","init analizer successfull");

        //TODO: @paul very importatn!! remove these keys and load them from the settings
        this.consumerKey = "WTSdBrmGi9X3GlSW1OTMb0Xhj";
        this.consumerSecret = "2xPN57eBDYeqWPKVpmG95XrwjX6fq79fUS2ilC7sYNWEc25xIL";
        this.accessToken = "791421180129476609-Ld84Ity8cdq9i0a7GawzS1OxKzGYWtz";
        this.AccessTokenSecret = "lOlxp3j603JJ4fZPJTl08PhEPnAZ30uJ6TmpYVwWCct1m";
    }

    private boolean isRunning = false;


    private String consumerKey = "";
    private String consumerSecret = "";
    private String accessToken = "";
    private String AccessTokenSecret = "";

    private TwitterCrawler twitterCrawler = null;

    public TwitterCrawler getTwitterCrawler(){
        return this.twitterCrawler;
    }



    public boolean isRunning() {
        return isRunning;
    }

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

        this.twitterCrawler.stop();
        this.finalResult.finalize();
        this.isRunning = false;
        this.twitterCrawler = null;
        EmotionAnalizer.Recreate();
    }

    private AnalizationResult currentResult = null;

    public  void startAnalization(String keywords, Context c){
        Log.d("analizer","start analizer!");

        EmotionAnalizer.INSTANCE.init(c);

        this.finalResult = new AnalizationResult();
        this.result_steps = new LinkedList<AnalizationResult>();
        this.twitterCrawler = new TwitterCrawler();

        this.isRunning = true;
        try {
            this.twitterCrawler.start(keywords);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //TODO: remove following -> just test
      //  Timer timer = new Timer();
    //    timer.schedule(new SayHello(this.twitterCrawler), 0, 5000);
       // timer.cancel();
    }

    public boolean isInitialized() {
        return isInitialized;
    }
}
/*
class SayHello extends TimerTask {

    private TwitterCrawler crawler;
    public SayHello(TwitterCrawler c){
        crawler = c;
    }

    public void run() {
        Log.d("analizer","Current WORDCOUNT: "+crawler.getCurrentResult().wordCount);
        System.out.println(crawler.getCurrentResult().wordCount);
    }
}*/


/**
 * protected static void startTimer() {
 isTimerRunning = true;
 timer.scheduleAtFixedRate(new TimerTask() {
 public void run() {
 elapsedTime += 1; //increase every sec
 mHandler.obtainMessage(1).sendToTarget();
 }
 }, 0, 1000);
 }

 public Handler mHandler = new Handler() {
 public void handleMessage(Message msg) {
 StopWatch.time.setText(formatIntoHHMMSS(elapsedTime)); //this is the textview
 }
 };
 */
