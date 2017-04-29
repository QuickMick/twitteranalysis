package com.example.mick.emotionanalizer;

/**
 * Created by Mick on 27.04.2017.
 */
import android.util.Log;

import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterCrawler {

    private final Object lock = new Object();

    private volatile AnalizationResult currentResult;

    private TwitterStream stream=null;

    private boolean isRunning = false;

    /**
     * everytime this method gecalled, a new result will be generated in which the next results are merged.
     * this is done so that you can use the returned value in the UI because the analization thread does not get interfered
     *
     * this method should be called each 10 seconds and the result should be merged to the overall result.
     * additionally you can save this object to display a timeline graph
     * @return
     */
    public synchronized AnalizationResult getCurrentResult(){
        AnalizationResult old;
        if(this.isRunning) {
            synchronized (lock) {
                old = this.currentResult;
                this.currentResult = new AnalizationResult();
            }
        }else{
            synchronized (lock) {
                old = this.currentResult;
                this.currentResult = null;
            }
        }
        return old;
    }

    public synchronized void stop(){

        try {
            this.stream.shutdown();
        }catch(Exception e){
            Log.d("analizer", "i dont know why, but everytime you stop it crashes");
        }
        this.stream = null;
        this.isRunning = false;
    }

    public void start(String keywords) throws InterruptedException {

        synchronized (lock) {
            this.currentResult = new AnalizationResult();
        }

        StatusListener listener = new StatusListener(){
            public void onStatus(Status status) {
            //    System.out.println("tweet");
                //TODO: dont use retweets --> is this a good idea?
                if(status.isRetweet())return;

                String currentTweet = status.getText();
                // merge hashtags to tweet
                for(HashtagEntity s :status.getHashtagEntities()){
                    currentTweet = currentTweet+" #"+s.getText();
                }

                synchronized (lock) {
                    EmotionAnalizer.INSTANCE.process(currentTweet, TwitterCrawler.this.currentResult);
                }
            }

            public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {}
            public void onTrackLimitationNotice(int numberOfLimitedStatuses) {}

            @Override
            public void onScrubGeo(long l, long l1) {}
            @Override
            public void onStallWarning(StallWarning stallWarning) {}
            public void onException(Exception ex) {ex.printStackTrace();}
        };


        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey(AnalizationHelper.INSTANCE().getConsumerKey())
                .setOAuthConsumerSecret(AnalizationHelper.INSTANCE().getConsumerSecret())
                .setOAuthAccessToken(AnalizationHelper.INSTANCE().getAccessToken())
                .setOAuthAccessTokenSecret(AnalizationHelper.INSTANCE().getAccessTokenSecret());

        this.stream= new TwitterStreamFactory(cb.build()).getInstance();
        this.stream.addListener(listener);
        // sample() method internally creates a thread which manipulates TwitterStream and calls these adequate listener methods continuously.


        if(keywords != null && keywords.length() >0) {
            FilterQuery fq = new FilterQuery();
            fq.track(keywords);
            fq.language("en");
            this.stream.filter(fq);
        }else {
            this.stream.sample("en");
        }

        this.isRunning = true;
    }

    public boolean isRunning() {
       // synchronized (lock) {
            return isRunning;
       // }
    }
}