/**
 *
 */
package com.example.mick.emotionanalizer;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
/*
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;*/
import org.json.simple.parser.JSONParser;

/**
 * @author Mick
 *
 */
public class EmotionAnalizer {

	/**
	 * Contains a list of many stopwords from the english language
	 */
	private HashSet<String> stopwords = new HashSet<String>();

	/**
	 * contains a list of contractions and their coresponding expansions
	 */
	private HashMap<String,String> contractions = new HashMap<String,String>();

	/**
	 * contains the emotional weighting
	 */
	private HashMap<String,EmotionWeighting> emotionalLexicon = new HashMap<String,EmotionWeighting>();


	private WordProcessor wordProcessor;

	public static EmotionAnalizer INSTANCE = new EmotionAnalizer();


	public static void Recreate(){
		INSTANCE = new EmotionAnalizer();
	}


	/*public static void main(String[] args) {
		String text = "walking walk added doing You don't loseing much, as asList() returns an ArrayList which has an array at its heart. The constructor will just change a reference so that's not much work to be done there. And contains()/indexOf() will iterate and use equals(). For primitives you should be better off coding it yourself, though. For Strings or other classes, the difference will not be noticeable.";
		AnalizationResult r = new EmotionAnalizer().process(text);
		r = new EmotionAnalizer().process("Compile it, import the Porter class into you program and create an instance. Then use the stripAffixes method of this method which takes a String as  input and returns the stem of this String again as a String.", r);
	    System.out.println(r);
	    System.out.println("fin");
	}*/

	private EmotionAnalizer(){
		//  this.init();
		//this.wordProcessor = new WordProcessor();
	}

	// no need for this function anymore - the contractions will be removed anyway in the cleantokens section
	private String exapndContractions(String s){
		return s;
	}

	private String prepareText(String text){
		text = text.toLowerCase();										// to lower
		text = text.replaceAll("(\\r\\n|\\n|\\r)", " "); 				// remove new lines
		text = text.replaceAll("&([^;]*);", ""); 						// remove HTML-entities
		text = text.replaceAll("(?:https?|ftp):\\/\\/[\\n\\S]+", ""); 	// remove urls
		text = text.replaceAll("@\\w+",""); 							// remove mentions
		text = text.replaceAll("[^a-z' ]", " ");						// just let text pass
		text = text.replaceAll(" +", " ");								// remove multiple spaces
		text = text.trim();												// remove leading and trailing spaces

		/*if(text.contains("start") || text.contains("https"))
		{
			Log.d("text",text+" |||||||| "+orgi);
		}*/
		return text;
	}

	private String[] tokenize(String processedText){
		return processedText.split(" ");
	}

	/**
	 * remove stoppwords and contractions
	 * @param token
	 * @return
	 */
	private String[] cleanTokens(String[] token) {
		List<String> result = new LinkedList<String>();
		for(String cur : token){
			cur = cur.trim();
			if (cur.length() >2
					&& !this.stopwords.contains(cur)
					&& !this.contractions.containsKey(cur)
					) {
				result.add(this.wordProcessor.conjugate(cur));
				//System.out.println(cur+"		-> "+this.wordProcessor.conjugate(cur)+" 		--->"+new PorterStemmer().stripAffixes(cur));
			}
		}

		return result.toArray(new String[]{});

	}

	private AnalizationResult analyzeEmotions(String[] tokens, AnalizationResult result){
		int[] resultSet = new int[]{0,0,0,0,0,0,0,0,0,0};

		// Iterate over tokens
		for(int i = 0; i< tokens.length; i++){
			String curWord = tokens[i];

			this.addWordToCounter(result.wordStatistic_all,curWord);

			if(!this.emotionalLexicon.containsKey(curWord)) continue;// skip if word is not in dictionary

			EmotionWeighting emotionData = this.emotionalLexicon.get(curWord); // get emotiondata from dict

			// get emotional data from the dictionary and add its values to the result set.
			int[] arr = emotionData.asArray();
			for(int j=0;j<resultSet.length;j++){
				resultSet[j] = resultSet[j]+arr[j];
			}

			result.wordCountAnalized++;
		}

		// create own wordcount for the emotions
		for(int i = 0; i< tokens.length; i++){
			String curWord = tokens[i];
			if(resultSet[0] >0){
				this.addWordToCounter(result.wordStatistic_anger,curWord);
			}
			if(resultSet[1] >0){
				this.addWordToCounter(result.wordStatistic_anticipation,curWord);
			}
			if(resultSet[2] >0){
				this.addWordToCounter(result.wordStatistic_disgust,curWord);
			}
			if(resultSet[3] >0){
				this.addWordToCounter(result.wordStatistic_fear,curWord);
			}
			if(resultSet[4] >0){
				this.addWordToCounter(result.wordStatistic_joy,curWord);
			}
			if(resultSet[5] >0){
				this.addWordToCounter(result.wordStatistic_sadness,curWord);
			}
			if(resultSet[6] >0){
				this.addWordToCounter(result.wordStatistic_surprise,curWord);
			}
			if(resultSet[7] >0){
				this.addWordToCounter(result.wordStatistic_trust,curWord);
			}
			if(resultSet[8] >0){
				this.addWordToCounter(result.wordStatistic_sentiment_negative,curWord);
			}
			if(resultSet[9] >0){
				this.addWordToCounter(result.wordStatistic_sentiment_positive,curWord);
			}
		}

		result.wordCount+=tokens.length;
		result.weigthing.add(resultSet);

		return result;
	}

	private void addWordToCounter(HashMap<String,Integer> hm, String word){
		// create wordcount
		if(!hm.containsKey(word)){
			hm.put(word, 0);
		}
		//update wordcount
		hm.put(word, hm.get(word)+1);
	}

	public AnalizationResult process(String text,AnalizationResult result){
		text = text.replaceAll("&([^;]*);", ""); 						// remove HTML-entities
		text = text.replaceAll("(?:https?|ftp):\\/\\/[\\n\\S]+", ""); 	// remove urls

		//Log.d("text",text);

		String[] sentences = text.split("[\\.!\\?;]+"); // split text to sentences

		result.tweetCount +=1;
		result.sentenceCount +=sentences.length;

		for(String sentence : sentences){
			result = this.analyzeEmotions(this.cleanTokens(this.tokenize(this.prepareText(this.exapndContractions(sentence)))),result);
			//Log.d("text_sentence",this.prepareText(this.exapndContractions(sentence)));
		}

		return result;
	}

	public AnalizationResult process(String text){
		return this.process(text,new AnalizationResult());
	}

	public String loadJSONFromAsset(Context context,String name) {
		String json = null;
		try {
			InputStream is = context.getAssets().open(name);
			int size = is.available();
			byte[] buffer = new byte[size];
			is.read(buffer);
			is.close();
			json = new String(buffer, "UTF-8");
		} catch (IOException ex) {
			ex.printStackTrace();
			return null;
		}
		return json;
	}



	private boolean isInitialized = false;
	/**
	 * load dictionaries
	 */
	public void init(Context context){
		if(this.isInitialized) return;
		// load contraction data/json
		try {
			JSONObject jsonObject = new JSONObject(this.loadJSONFromAsset(context,"contractions.json")); //(JSONObject) new JSONParser().parse(this.loadJSONFromAsset(context,"contractions.json"));

			Iterator it = jsonObject.keys();
			while (it.hasNext()) {
				String key = it.next().toString();
				this.contractions.put(key , jsonObject.getJSONArray(key).get(0).toString());
			}

		} catch (Exception e) {
			e.printStackTrace();
		}


		// load stoppwords
		try {
			JSONObject jsonObject = new JSONObject(this.loadJSONFromAsset(context,"stopwords_en.json")); // (JSONObject) obj;
			Iterator it = jsonObject.keys();
			while (it.hasNext()) {
				this.stopwords.add(it.next().toString());
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			JSONObject jsonObject = new JSONObject(this.loadJSONFromAsset(context,"emotions.json"));
			Iterator it = jsonObject.keys();
			while (it.hasNext()) {
				String key = it.next().toString();
				this.emotionalLexicon.put(key,
						new EmotionWeighting(
								Integer.parseInt(jsonObject.getJSONObject(key).get("anger").toString()),
								Integer.parseInt(jsonObject.getJSONObject(key).get("anticipation").toString()),
								Integer.parseInt(jsonObject.getJSONObject(key).get("disgust").toString()),
								Integer.parseInt(jsonObject.getJSONObject(key).get("fear").toString()),
								Integer.parseInt(jsonObject.getJSONObject(key).get("joy").toString()),
								Integer.parseInt(jsonObject.getJSONObject(key).get("sadness").toString()),
								Integer.parseInt(jsonObject.getJSONObject(key).get("surprise").toString()),
								Integer.parseInt(jsonObject.getJSONObject(key).get("trust").toString()),
								Integer.parseInt(jsonObject.getJSONObject(key).get("negative").toString()),
								Integer.parseInt(jsonObject.getJSONObject(key).get("positive").toString())
						)
				);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		this.wordProcessor = new WordProcessor();
		this.wordProcessor.init(context);
		this.isInitialized = true;
	}

}