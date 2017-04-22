package com.example.mick.emotionanalizer;

import java.util.HashMap;
import java.util.Map;

public class AnalizationResult{

	public EmotionWeighting weigthing = new EmotionWeighting(0,0,0,0,0,0,0,0,0,0);

	/**
	 * the analitzaitonResult instance should be generated, when the analization starts,
	 * so we initialize the start date with the current date
	 */
	public Date startDate = new Date();


	public Date endDate = null;

	/**
	 * Use this when the analisation ends,
	 * currently this will just set the endDate, but in futer development we may need
	 * to finalize other stuff
	 */
	public void Finalize(){
		this.endDate = new Date();
	}

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


	/**
	 * Adds another instance of analization Result to this one
	 * @param ar
     */
	public void Add(AnalizationResult ar){
		this.weigthing.add(ar.weigthing);
		this.wordCount += ar.wordCount;
		this.wordCountAnalized +=ar.wordCountAnalized;

		this.mergeHashTables(this.wordStatistic_all, ar.wordStatistic_all);
		this.mergeHashTables(this.wordStatistic_anger, ar.wordStatistic_anger);
		this.mergeHashTables(this.wordStatistic_anticipation, ar.wordStatistic_anticipation);
		this.mergeHashTables(this.wordStatistic_disgust, ar.wordStatistic_disgust);
		this.mergeHashTables(this.wordStatistic_fear, ar.wordStatistic_fear);
		this.mergeHashTables(this.wordStatistic_joy, ar.wordStatistic_joy);
		this.mergeHashTables(this.wordStatistic_sadness, ar.wordStatistic_sadness);
		this.mergeHashTables(this.wordStatistic_surprise, ar.wordStatistic_surprise);
		this.mergeHashTables(this.wordStatistic_trust, ar.wordStatistic_trust);
		this.mergeHashTables(this.wordStatistic_sentiment_negative, ar.wordStatistic_sentiment_negative);
		this.mergeHashTables(this.wordStatistic_sentiment_positive, ar.wordStatistic_sentiment_positive);
	}

	/*private void mergeHashTables(HashMap<String,Integer> a, HashMap<String,Integer> b){
		Iterator it = b.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry)it.next();
			if(a.contains(pair.getKey())){
				a.put(pair.getKey(),a.get(pair.getKey()) + pair.getValue());

				a.put(pair.getKey(),a.get(((int)pair.getKey())) + ((int)pair.getValue()));
			}else{
				a.put(pair.getKey(),pair.getValue());
			}
		}
	}*/

	private void  mergeHashTables(HashMap<String,Integer> a, HashMap<String,Integer> b){
		for (Map.Entry<String, Integer> entry : b.entrySet()) {
			String key = entry.getKey();
			Integer value = entry.getValue();
			if(a.containsKey(key)){
				a.put(key,a.get(key) + value);
			}else{
				a.put(key,value);
			}
		}
	}

	/*
	public AnalizationResult(){
	//public AnalizationResult(EmotionWeighting weigthing, HashMap<String,Integer> wordStatistic_all, int wordCount, int wordCountAnalized){
		this.weigthing = weigthing;
		this.wordStatistic_all = wordStatistic_all;
		this.wordCount = wordCount;
		this.wordCountAnalized = wordCountAnalized;
	}*/
}