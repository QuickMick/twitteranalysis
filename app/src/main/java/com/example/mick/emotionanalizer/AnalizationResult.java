package com.example.mick.emotionanalizer;

import java.util.HashMap;

public class AnalizationResult{

	public EmotionWeighting weigthing = new EmotionWeighting(0,0,0,0,0,0,0,0,0,0);

	/**
	 * count of all words
	 */
	public int wordCount=0;

	/**
	 * count of all words which actually got analized
	 */
	public int wordCountAnalized=0;

	public HashMap<String,Integer> wordStatistic_all = new HashMap<String,Integer>();

	public HashMap<String,Integer> wordStatistic_anger = new HashMap<String,Integer>();
	public HashMap<String,Integer> wordStatistic_anticipation = new HashMap<String,Integer>();
	public HashMap<String,Integer> wordStatistic_disgust = new HashMap<String,Integer>();
	public HashMap<String,Integer> wordStatistic_fear = new HashMap<String,Integer>();
	public HashMap<String,Integer> wordStatistic_joy = new HashMap<String,Integer>();
	public HashMap<String,Integer> wordStatistic_sadness = new HashMap<String,Integer>();
	public HashMap<String,Integer> wordStatistic_surprise = new HashMap<String,Integer>();
	public HashMap<String,Integer> wordStatistic_trust = new HashMap<String,Integer>();

	public HashMap<String,Integer> wordStatistic_sentiment_negative = new HashMap<String,Integer>();
	public HashMap<String,Integer> wordStatistic_sentiment_positive = new HashMap<String,Integer>();

	/*
	public AnalizationResult(){
	//public AnalizationResult(EmotionWeighting weigthing, HashMap<String,Integer> wordStatistic_all, int wordCount, int wordCountAnalized){
		this.weigthing = weigthing;
		this.wordStatistic_all = wordStatistic_all;
		this.wordCount = wordCount;
		this.wordCountAnalized = wordCountAnalized;
	}*/
}