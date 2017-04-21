/**
 *
 */
package com.example.mick.emotionanalizer;

import java.io.FileReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
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

	/**
	 * test
	 * @param args
	 */
	public static void main(String[] args) {
		String text = "walking walk added doing You don't loseing much, as asList() returns an ArrayList which has an array at its heart. The constructor will just change a reference so that's not much work to be done there. And contains()/indexOf() will iterate and use equals(). For primitives you should be better off coding it yourself, though. For Strings or other classes, the difference will not be noticeable.";
		AnalizationResult r = new EmotionAnalizer().process(text);
		r = new EmotionAnalizer().process("Compile it, import the Porter class into you program and create an instance. Then use the stripAffixes method of this method which takes a String as  input and returns the stem of this String again as a String.", r);
		System.out.println(r);
		System.out.println("fin");
	}

	public EmotionAnalizer(){
		this.init();
		this.wordProcessor = new WordProcessor();
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

			// create wordcount
          /*  if(!result.wordStatistic_all.containsKey(curWord)){
            	result.wordStatistic_all.put(curWord, 0);
            }

            //update wordcount
            result.wordStatistic_all.put(curWord, result.wordStatistic_all.get(curWord)+1);*/
			this.addWordToCounter(result.wordStatistic_all,curWord);

			if(!this.emotionalLexicon.containsKey(curWord)) continue;// skip if word is not in dictionary

			EmotionWeighting emotionData = this.emotionalLexicon.get(curWord); // get emotiondata from dict
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
			if(resultSet[0] >0){
				this.addWordToCounter(result.wordStatistic_anticipation,curWord);
			}
			if(resultSet[0] >0){
				this.addWordToCounter(result.wordStatistic_disgust,curWord);
			}
			if(resultSet[0] >0){
				this.addWordToCounter(result.wordStatistic_fear,curWord);
			}
			if(resultSet[0] >0){
				this.addWordToCounter(result.wordStatistic_joy,curWord);
			}
			if(resultSet[0] >0){
				this.addWordToCounter(result.wordStatistic_sadness,curWord);
			}
			if(resultSet[0] >0){
				this.addWordToCounter(result.wordStatistic_surprise,curWord);
			}
			if(resultSet[0] >0){
				this.addWordToCounter(result.wordStatistic_trust,curWord);
			}
			if(resultSet[0] >0){
				this.addWordToCounter(result.wordStatistic_sentiment_negative,curWord);
			}
			if(resultSet[0] >0){
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
		String[] sentences = text.split("[\\.!\\?:]+"); // split text to sentences
		for(String sentence : sentences){
			result = this.analyzeEmotions(this.cleanTokens(this.tokenize(this.prepareText(this.exapndContractions(sentence)))),result);
		}

		return result;
	}

	public AnalizationResult process(String text){
		return this.process(text,new AnalizationResult(/*new EmotionWeighting(new int[]{0,0,0,0,0,0,0,0,0,0}) ,new HashMap<String,Integer>(), 0, 0*/));
	}


	/**
	 * load dictionaries
	 */
	private void init(){
		// load contraction data/json
		try {
			Object obj = new JSONParser().parse(new FileReader("./bin/hhn/contractions.json"));
			JSONObject jsonObject = (JSONObject) obj;
			Iterator it = jsonObject.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry pair = (Map.Entry)it.next();
				// System.out.println(pair.getKey() + " = " + ((JSONArray)pair.getValue()).get(0));
				this.contractions.put(pair.getKey().toString() , ((JSONArray)pair.getValue()).get(0).toString());
			}

		} catch (Exception e) {
			e.printStackTrace();
		}


		// load stoppwords
		try {
			Object obj = new JSONParser().parse(new FileReader("./bin/hhn/stopwords_en.json"));
			JSONObject jsonObject = (JSONObject) obj;
			Iterator it = jsonObject.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry pair = (Map.Entry)it.next();
				this.stopwords.add(pair.getKey().toString());
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			Object obj = new JSONParser().parse(new FileReader("./bin/hhn/emotions.json"));

			JSONObject jsonObject = (JSONObject) obj;
			Iterator it = jsonObject.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry pair = (Map.Entry)it.next();
				this.emotionalLexicon.put(pair.getKey().toString(),
						new EmotionWeighting(
								Integer.parseInt(((JSONObject)pair.getValue()).get("anger").toString()),
								Integer.parseInt(((JSONObject)pair.getValue()).get("anticipation").toString()),
								Integer.parseInt(((JSONObject)pair.getValue()).get("disgust").toString()),
								Integer.parseInt(((JSONObject)pair.getValue()).get("fear").toString()),
								Integer.parseInt(((JSONObject)pair.getValue()).get("joy").toString()),
								Integer.parseInt(((JSONObject)pair.getValue()).get("sadness").toString()),
								Integer.parseInt(((JSONObject)pair.getValue()).get("surprise").toString()),
								Integer.parseInt(((JSONObject)pair.getValue()).get("trust").toString()),
								Integer.parseInt(((JSONObject)pair.getValue()).get("negative").toString()),
								Integer.parseInt(((JSONObject)pair.getValue()).get("positive").toString())
						)
				);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}