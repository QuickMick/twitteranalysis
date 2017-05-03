package com.example.mick.emotionanalizer;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.paulc.twittersentimentanalysis.Settings;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by Mick on 27.04.2017.
 */
public class AnalizationHelper {

    /**
     * this is true, while savin is in progress - so that we are able to block a new analization while saving
     */
    private boolean isBlocked = false;

    /**
     * steps saved for the live timeline
     */
    public static final int MAX_HISTORY_COUNT = 20;


    /**
     * represents the folder name, e.g. "twitter_results", which is the folder, where all files for
     * the history are saved to and loaded from.
     */
    private String analyzation_folder= "twitter_results";

    private static AnalizationHelper instance = new AnalizationHelper();


    public static void recreate(){
        instance = new AnalizationHelper();
    }

    private AnalizationHelper(){
        //TODO: @paul very importatn!! remove these keys and load them from the settings
     /*   this.consumerKey = "WTSdBrmGi9X3GlSW1OTMb0Xhj";
        this.consumerSecret = "2xPN57eBDYeqWPKVpmG95XrwjX6fq79fUS2ilC7sYNWEc25xIL";
        this.accessToken = "791421180129476609-Ld84Ity8cdq9i0a7GawzS1OxKzGYWtz";
        this.AccessTokenSecret = "lOlxp3j603JJ4fZPJTl08PhEPnAZ30uJ6TmpYVwWCct1m";*/






        //analyzation_folder= "twitter_results";
    }

    public void loadSettings(Activity a){
        SharedPreferences sharedPref = a.getSharedPreferences(Settings.SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);

        final String consumerkeytext = sharedPref.getString("consumerkey", "");
        final String consumerkeytextscrt = sharedPref.getString("consumerkeyscrt", "");
        final String accesstokentext = sharedPref.getString("accesstoken", "");
        final String accesstokentextscrt = sharedPref.getString("accesstokenscrt", "");
        final String folder = sharedPref.getString("folder", "twitter_results");

        AnalizationHelper.INSTANCE().setAccessToken(accesstokentext);
        AnalizationHelper.INSTANCE().setAccessTokenSecret(accesstokentextscrt);
        AnalizationHelper.INSTANCE().setConsumerKey(consumerkeytext);
        AnalizationHelper.INSTANCE().setConsumerSecret(consumerkeytextscrt);
        AnalizationHelper.INSTANCE().setAnalyzation_folder(folder);
    }

    public static AnalizationHelper INSTANCE(){
        return AnalizationHelper.instance;
    }

    /**
     * true, if date is saved aka history data.
     * also true at startup, because there is no data to save
     */
    private boolean isSaved= true;

    public boolean isSaved() {
        return isSaved;
    }

    public void setSaved(boolean saved) {
        isSaved = saved;
    }

    private volatile boolean isInitialized = false;

    private Object lock = new Object();
    private Object lock2 = new Object();

    private volatile AnalizationResult finalResult = null;
    private volatile LinkedList<AnalizationResult> result_steps = null;




    public String getAnalyzation_folder() {
        return this.analyzation_folder;
    }

    /**
     * TODO: @paul pls add one option in the settings view, to specify the folder name for the analysis
     * @param analyzation_folder
     */
    public void setAnalyzation_folder(String analyzation_folder) {
        this.analyzation_folder = analyzation_folder;
    }


    public synchronized AnalizationResult getFinalResult() {
        synchronized (lock) {
            return finalResult;
        }
    }

    /**
     * necessary for injecting history file
     * @param ar
     * @return
     */
    public synchronized AnalizationResult setFinalResult(AnalizationResult ar) {
        synchronized (lock) {
            return finalResult = ar;
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

            if(this.result_steps.size() > MAX_HISTORY_COUNT){
                this.result_steps.removeFirst();
            }
        }
    }

    public void init(Context context){
        if(this.isInitialized) return;
        this.isInitialized = true;
        Log.d("analizer","init analizer");

        Log.d("analizer","init analizer successfull");


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


    public boolean isBlocked() {
        return isRunning;
    }

    public void setBlocked(boolean isBlocked){
        this.isBlocked = isBlocked;
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

       // Log.d("analize",this.getFinalResult().toJSON());
        EmotionAnalizer.Recreate();
    }

    private AnalizationResult currentResult = null;

    public  void startAnalization(String keywords, Context c){
        Log.d("analizer","start analizer!");

        EmotionAnalizer.INSTANCE.init(c);

        //split keywords to tokens and clean
        String[] kw = null;

        if(keywords != null && keywords.length()>0) {
            ArrayList<String> processKewords = new ArrayList<String>();
            kw = keywords.toLowerCase().split(",");
            for (int i = 0; i < kw.length; i++) {
               // kw[i] = kw[i].trim();
                String cur = kw[i].trim();
                if(cur != null && cur.length() > 0) {
                    processKewords.add(cur);
                }
            }
            kw = processKewords.toArray(new String[processKewords.size()]);
        }

        synchronized (lock) {
            this.finalResult = new AnalizationResult(kw);
        }
        synchronized (lock2) {
            this.result_steps = new LinkedList<AnalizationResult>();
        }
        this.twitterCrawler = new TwitterCrawler();

        this.isRunning = true;
        try {
            this.twitterCrawler.start(kw);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


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
