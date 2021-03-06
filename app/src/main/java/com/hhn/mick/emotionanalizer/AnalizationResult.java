package com.hhn.mick.emotionanalizer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class AnalizationResult{


	/**
	 * Count of the tweets which are saved
	 */
	public static final int MAX_TWEET_HISTORY_COUNT = 50;

	public static String DATE_FORMAT= "dd.MM.yyyy HH:mm:ss";

	public EmotionWeighting weigthing = new EmotionWeighting(0,0,0,0,0,0,0,0,0,0);

	private String[] keywords = new String[0];

	private String[] keywordsProhibited = new String[0];

	/*private boolean isSaved = false;

	public boolean isSaved() {
		return isSaved;
	}

	public void setSaved(boolean saved) {
		isSaved = saved;
	}*/

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
	public void finalize(){
		this.endDate = new Date();
	}

	/**
	 * count of all words
	 */
	public int wordCount=0;

	public int tweetCount = 0;

	public int sentenceCount = 0;

	/**
	 * count of all words which actually got analized
	 */
	public int wordCountAnalized=0;

	public AnalizationResult(){

	}

	public AnalizationResult(String[] keywords,String[] keywordsProhibited){

		this.keywords = keywords;
		this.keywordsProhibited = keywordsProhibited;

		if(this.keywords == null) this.keywords = new String[0];

		if(this.keywordsProhibited == null) this.keywordsProhibited= new String[0];

		/*this.keywords = keywords.toLowerCase().split(",");

		for(int i =0; i<this.keywords.length;i++){
			this.keywords[i] = this.keywords[i].trim();
		}*/
	}

	/*
	String string = "January 2, 2010";
DateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm");
Date date = format.parse(string);
System.out.println(date); // Sat Jan 02 00:00:00 GMT 2010
	 */

	public String keywordsToJSON(){

		if(this.keywords==null || this.keywords.length <=0) return "[]";

		String result = "[";
		for(String e:this.keywords){
			result = result.concat("\""+e+"\", ");
		}

		if(result.length() >1) {
			result = result.substring(0, result.length() - 2);
		}

		result = result.concat("]");

		return result;
	}


	public String keywordsProhibitedToJSON(){

		if(this.keywordsProhibited==null || this.keywordsProhibited.length <=0) return "[]";

		String result = "[";
		for(String e:this.keywordsProhibited){
			result = result.concat("\""+e+"\", ");
		}

		if(result.length() >1) {
			result = result.substring(0, result.length() - 2);
		}

		result = result.concat("]");

		return result;
	}


	public String toJSON(){

		DateFormat format = new SimpleDateFormat(AnalizationResult.DATE_FORMAT);

		return "{\"startDate\":\""+format.format(this.startDate)+"\", "
				+"\"endDate\":\""+format.format(this.endDate)+"\", "
				+"\"keywords\":"+ this.keywordsToJSON()+", "
				+"\"prohibitedKeywords\":"+ this.keywordsProhibitedToJSON()+", "
				+"\"wordCount\":"+this.wordCount+", "
				+"\"tweetCount\":"+this.tweetCount+", "
				+"\"sentenceCount\":"+this.sentenceCount+", "
				+"\"wordCountAnalized\":"+this.wordCountAnalized+", "
				+"\"emotionWeighting\":"+this.weigthing.toJSON()+", "

				+"\"wordStatistic_all\":"+this.hashMapToJSON(this.wordStatistic_all)+", "
				+"\"wordStatistic_anger\":"+this.hashMapToJSON(this.wordStatistic_anger)+", "
				+"\"wordStatistic_anticipation\":"+this.hashMapToJSON(this.wordStatistic_anticipation)+", "
				+"\"wordStatistic_disgust\":"+this.hashMapToJSON(this.wordStatistic_disgust)+", "
				+"\"wordStatistic_fear\":"+this.hashMapToJSON(this.wordStatistic_fear)+", "
				+"\"wordStatistic_joy\":"+this.hashMapToJSON(this.wordStatistic_joy)+", "
				+"\"wordStatistic_sadness\":"+this.hashMapToJSON(this.wordStatistic_sadness)+", "
				+"\"wordStatistic_surprise\":"+this.hashMapToJSON(this.wordStatistic_surprise)+", "
				+"\"wordStatistic_trust\":"+this.hashMapToJSON(this.wordStatistic_trust)+", "
				+"\"wordStatistic_sentiment_negative\":"+this.hashMapToJSON(this.wordStatistic_sentiment_negative)+", "
				+"\"wordStatistic_sentiment_positive\":"+this.hashMapToJSON(this.wordStatistic_sentiment_positive)+"}";
	}

	public static AnalizationResult createFromJSON(String json) throws JSONException {

		JSONObject jsonObject = new JSONObject(json);
		DateFormat format = new SimpleDateFormat(AnalizationResult.DATE_FORMAT);

		String[] kWords = new String[0];
		String[] kWordsProhibited = new String[0];

		try {
			kWords = AnalizationResult.jsonArrayToStringArray2(jsonObject.getJSONArray("keywords"));
		}catch(Exception e){}
		try {
			kWordsProhibited = AnalizationResult.jsonArrayToStringArray2(jsonObject.getJSONArray("prohibitedKeywords"));
		}catch(Exception e){}
		AnalizationResult result = new AnalizationResult(
				kWords,
				kWordsProhibited
		);
		result.weigthing = EmotionWeighting.fromJSON(jsonObject.getJSONObject("emotionWeighting").toString());
		result.wordCount = jsonObject.getInt("wordCount");
		result.tweetCount = jsonObject.getInt("tweetCount");
		result.sentenceCount = jsonObject.getInt("sentenceCount");
		result.wordCountAnalized = jsonObject.getInt("wordCountAnalized");
		try {
			result.startDate = format.parse(jsonObject.getString("startDate"));
		} catch (ParseException e) {
			result.startDate = null;
		}
		try {
			result.endDate = format.parse(jsonObject.getString("endDate"));
		} catch (ParseException e) {
			result.endDate = null;
		}

		result.wordStatistic_all = AnalizationResult.jsonToHashMap(jsonObject.getJSONObject("wordStatistic_all"));
		result.wordStatistic_anger = AnalizationResult.jsonToHashMap(jsonObject.getJSONObject("wordStatistic_anger"));
		result.wordStatistic_anticipation = AnalizationResult.jsonToHashMap(jsonObject.getJSONObject("wordStatistic_anticipation"));
		result.wordStatistic_disgust = AnalizationResult.jsonToHashMap(jsonObject.getJSONObject("wordStatistic_disgust"));
		result.wordStatistic_fear = AnalizationResult.jsonToHashMap(jsonObject.getJSONObject("wordStatistic_fear"));
		result.wordStatistic_joy = AnalizationResult.jsonToHashMap(jsonObject.getJSONObject("wordStatistic_joy"));
		result.wordStatistic_sadness = AnalizationResult.jsonToHashMap(jsonObject.getJSONObject("wordStatistic_sadness"));
		result.wordStatistic_surprise = AnalizationResult.jsonToHashMap(jsonObject.getJSONObject("wordStatistic_surprise"));
		result.wordStatistic_trust = AnalizationResult.jsonToHashMap(jsonObject.getJSONObject("wordStatistic_trust"));
		result.wordStatistic_sentiment_negative = AnalizationResult.jsonToHashMap(jsonObject.getJSONObject("wordStatistic_sentiment_negative"));
		result.wordStatistic_sentiment_positive = AnalizationResult.jsonToHashMap(jsonObject.getJSONObject("wordStatistic_sentiment_positive"));

/*
		*/

			return result;

	}

	private static HashMap<String,Integer> jsonToHashMap(JSONObject jsonObject) throws JSONException {
		HashMap<String,Integer> result = new HashMap<String,Integer>();
		Iterator it = jsonObject.keys();
		while (it.hasNext()) {
			String key = it.next().toString();
			result.put(key,jsonObject.getInt(key));
		}

		return result;
	}

	private static String[] jsonArrayToStringArray(JSONArray jsonArray){
		ArrayList<String> stringArray = new ArrayList<String>();
		for(int i = 0, count = jsonArray.length(); i< count; i++)
		{
			try {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				stringArray.add(jsonObject.toString());
			}
			catch (JSONException e) {
				e.printStackTrace();
			}
		}

		return stringArray.toArray(new String[stringArray.size()]);
	}

	private static String[] jsonArrayToStringArray2(JSONArray jsonArray){
		ArrayList<String> stringArray = new ArrayList<String>();
		for(int i = 0, count = jsonArray.length(); i< count; i++)
		{
			try {
				stringArray.add(jsonArray.getString(i));
			}
			catch (JSONException e) {
				e.printStackTrace();
			}
		}

		return stringArray.toArray(new String[stringArray.size()]);
	}

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


	public static List<Map.Entry<String,Integer>> getTopList(HashMap<String,Integer> in){


		List<Map.Entry<String,Integer>> entries = new ArrayList<Map.Entry<String,Integer>>( in.entrySet());

		Collections.sort(
				entries
				,   new Comparator<Map.Entry<String,Integer>>() {
					public int compare(Map.Entry<String,Integer> a, Map.Entry<String,Integer> b) {
						if(b.getValue() > a.getValue()) return 1;
						else if(b.getValue() < a.getValue()) return -1;

						return 0;
					}
				}
		);

		return entries;
	}



	public String toString(){
		return "tweetCount: "+tweetCount+ " | wordCount: "+wordCount+" | wordcountAnalized: "+wordCountAnalized+" ||| Emotions: "+weigthing;
	}

	private String hashMapToJSON(HashMap<String, Integer> b){
		String result="{";
		for (Map.Entry<String, Integer> entry : b.entrySet()) {
			String key = entry.getKey();
			Integer value = entry.getValue();

			result=result.concat("\""+key+"\":"+value+", ");
		}
		if(b.entrySet().size() >1) {
			result = result.substring(0, result.length() - 2);
		}
		result=result.concat("}");

		return result;
	}

	/**
	 * Adds another instance of analization Result to this one
	 * @param ar
     */
	public void Add(AnalizationResult ar){
		this.weigthing.add(ar.weigthing);
		this.wordCount += ar.wordCount;
		this.wordCountAnalized +=ar.wordCountAnalized;
		this.tweetCount +=ar.tweetCount;
		this.sentenceCount += ar.sentenceCount;

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

		this.tweetSteps.addAll(ar.getTweetStepsRaw());

		// merge the tweetcache
		int size = this.tweetSteps.size();
		if(size > MAX_TWEET_HISTORY_COUNT){
			//this.tweetSteps = (LinkedList<TweetCache>)this.tweetSteps.subList(size-1-MAX_TWEET_HISTORY_COUNT,size-1);
			this.tweetSteps.subList(0,size-MAX_TWEET_HISTORY_COUNT).clear();	//remove first/old elements
		}


	}

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

	public String[] getKeywords() {
		return keywords;
	}

	public String[] getKeywordsProhibited() {
		return this.keywordsProhibited;
	}

	private LinkedList<TweetCache> tweetSteps = new LinkedList<>();

	public TweetCache[] getTweetSteps(){
		return this.tweetSteps.toArray(new TweetCache[this.tweetSteps.size()]);
	}

	private LinkedList<TweetCache> getTweetStepsRaw(){
		return this.tweetSteps;
	}

	public void addToTweetCache(TweetCache next) {
		this.tweetSteps.addLast(next);
		if(this.tweetSteps.size() > MAX_TWEET_HISTORY_COUNT){
			this.tweetSteps.removeFirst();
		}
	}

}