package com.hhn.mick.emotionanalizer;

/**
 * Created by Mick on 02.05.2017.
 *
 * Used for the short history of tweets during the analysis process
 */
public class TweetCache{
    public String tweetText;
    public String[] tweetTokens;
    public EmotionWeighting weighting;
    public TweetCache(String text, String[] tokens, EmotionWeighting weighting){
        this.tweetText = text;
        this.tweetTokens = tokens;
        this.weighting = weighting;
    }
    public TweetCache(){

    }
}