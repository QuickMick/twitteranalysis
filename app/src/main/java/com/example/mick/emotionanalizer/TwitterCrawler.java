package com.example.mick.emotionanalizer;

/**
 * Created by Mick on 27.04.2017.
 */
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterCrawler {

    private final Object lock = new Object();

        private volatile AnalizationResult currentResult;

        public synchronized AnalizationResult getCurrentResult(){
            synchronized (lock) {
                return this.currentResult;
            }
        }

        public void start(String keywords) throws InterruptedException {

            this.currentResult = new AnalizationResult();

            StatusListener listener = new StatusListener(){
                public void onStatus(Status status) {
                //    System.out.println("tweet");

                    String currentTweet = status.getText();
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

            TwitterStream twitterStream = new TwitterStreamFactory(cb.build()).getInstance();
            twitterStream.addListener(listener);
            // sample() method internally creates a thread which manipulates TwitterStream and calls these adequate listener methods continuously.

            //TODO: keywords

            if(keywords != null && keywords.length() >0) {
                FilterQuery fq = new FilterQuery();
                fq.track(keywords);
                fq.language("en");
                twitterStream.filter(fq);
            }else {
                twitterStream.sample("en");
            }
        }
}