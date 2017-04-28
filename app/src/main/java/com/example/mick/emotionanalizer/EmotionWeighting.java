package com.example.mick.emotionanalizer;


/**
 * Represents the result of the emotional analisys
 * @author Mick
 *
 */
public class EmotionWeighting{

	public int anger;
	public int anticipation;
	public int disgust;
	public int fear;
	public int joy;
	public int sadness;
	public int surprise;
	public int trust;

	public int sentiment_negative;
	public int sentiment_positive;

	@Override
	public String toString(){
		return "anger: "+anger+" | anticipation: "+anticipation+" | disgust: "+disgust+" | fear: "+fear+" | joy: "+joy+" | sadness: "+sadness+" | surprise: "+surprise+" | trust: "+trust +"||| Sentiment: positive: "+sentiment_positive+" | negative: "+sentiment_negative;
	}

	public String toJSON(){
		return "{\"anger\":"+anger+", \"anticipation\":"+anticipation+", \"disgust\": "+disgust+", \"fear\":"+fear+", \"joy\":"+joy+", \"sadness\":"+sadness+", \"surprise\":"+surprise+", \"trust\":"+trust+", \"positive\":"+sentiment_positive+", \"negative\":"+sentiment_negative+"}";
	}

	/**
	 * create a new instance
	 * @param anger
	 * @param anticipation
	 * @param disgust
	 * @param fear
	 * @param joy
	 * @param sadness
	 * @param surprise
	 * @param trust
	 * @param sentiment_negative
	 * @param sentiment_positive
	 */
	public EmotionWeighting(int anger, int anticipation, int disgust, int fear, int joy, int sadness, int surprise, int trust, int sentiment_negative, int sentiment_positive){
		this.anger =anger;
		this.anticipation = anticipation;
		this.disgust = disgust;
		this.fear = fear;
		this.joy = joy;
		this.sadness = sadness;
		this.surprise = surprise;
		this.trust = trust;
		this.sentiment_negative = sentiment_negative;
		this.sentiment_positive = sentiment_positive;
	}


	public EmotionWeighting(){
		this.anger =0;
		this.anticipation = 0;
		this.disgust = 0;
		this.fear = 0;
		this.joy = 0;
		this.sadness = 0;
		this.surprise = 0;
		this.trust = 0;
		this.sentiment_negative = 0;
		this.sentiment_positive = 0;
	}

	/**
	 * create a new instance
	 * @param vals the order of the values are important
	 * this.anger =vals[0];
	this.anticipation = vals[1];
	this.disgust = vals[2];
	this.fear = vals[3];
	this.joy = vals[4];
	this.sadness = vals[5];
	this.surprise = vals[6];
	this.trust = vals[7];
	this.sentiment_negative = vals[8];
	this.sentiment_positive = vals[9];
	 */
	public EmotionWeighting(int[] vals){
		this.anger =vals[0];
		this.anticipation = vals[1];
		this.disgust = vals[2];
		this.fear = vals[3];
		this.joy = vals[4];
		this.sadness = vals[5];
		this.surprise = vals[6];
		this.trust = vals[7];
		this.sentiment_negative = vals[8];
		this.sentiment_positive = vals[9];
	}

	/**
	 * add values to this weighting
	 * @param vals order is important
	 */
	public void add(int[] vals){
		this.anger += vals[0];
		this.anticipation += vals[1];
		this.disgust += vals[2];
		this.fear += vals[3];
		this.joy += vals[4];
		this.sadness += vals[5];
		this.surprise += vals[6];
		this.trust += vals[7];
		this.sentiment_negative += vals[8];
		this.sentiment_positive += vals[9];
	}

	public void add(EmotionWeighting ew){
		this.add(ew.asArray());
	}

	public int getTotalEmotion(){
		return this.anger+this.anticipation+this.disgust+this.fear+this.joy+this.sadness+this.surprise+this.trust;
	}

	public int getTotalSentiment(){
		return this.sentiment_negative+this.sentiment_positive;
	}

	/**
	 * converts a emotionalweighting class to an array
	 * @return
	 */
	public int[] asArray(){
		return new int[]{this.anger,this.anticipation,this.disgust,this.fear,this.joy,this.sadness,this.surprise,this.trust,this.sentiment_negative,this.sentiment_positive};
	}

}