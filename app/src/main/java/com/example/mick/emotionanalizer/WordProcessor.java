package com.example.mick.emotionanalizer;

import android.content.Context;


import java.io.IOException;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
/*
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;*/
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * The MIT License (MIT)

 Copyright (c) 2016 Alex Corvi

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.

 Translated from JavaScript to Java by Matthias Figura - also the functionality gots reduced

 * @author Mick
 *
 */
public class WordProcessor {

	private HashSet<String> infinitives = new HashSet<String>();
	private List<String[]> verbsTable = new LinkedList<String[]>();

	public WordProcessor(){
		//this.init();
	}


	public String conjugate(String vb) {
		vb = vb.toLowerCase();

		String tableResult = solveFromTable(vb);
		if(tableResult != null) return tableResult;

		if(isListedInfinitive(vb)) return vb;

		// from this point on .. try your best to convert the verb
		// it it's infinitive form
		String stripped = this.stripVerb(vb,null);

		String strippedFromTable = solveFromTable(stripped);

		if(stripped != "" && stripped != null && stripped != vb)
			return mergeVerb(vb,stripped,strippedFromTable != null ? strippedFromTable:vb);

		String stemmed = this.stemmer(vb);
		vb = this.isListedInfinitive(stemmed) ? stemmed: vb;

		String sft = solveFromTable(vb);

		return  sft!= null? sft :vb;
	}

	/**
	 * Used to strip complex verbs to simpler ones
	 * For example: underwent => went
	 * @param  {String} vb the verb
	 * @param  {String} o  the original verb (for recursion purposes)
	 * @return {String}    Stripped verb || Original verb (if it can't be stripped)
	 **/
	public String stripVerb(String vb,String o){
		if(o==null) o = vb;
		if(vb.length() < 2) return o;
		vb = vb.substring(0,1);
		if(isListedInfinitive(vb) || solveFromTable(vb) != null) return vb;
		else return stripVerb(vb,o);
	}

	/**
	 * Returns true if the passed verb is in the
	 * infintive verbs list
	 * @param  {String}  v Verb
	 * @return {Boolean}  true if it exists, false if it doesn't
	 **/
	public boolean isListedInfinitive(String v){
		return infinitives.contains(v);
	}

	/**
	 * Merge verb with the original verb so it
	 * can be returned to the user
	 * @param  {String} o original version of the verb
	 * @param  {String} s stripped version of the verb
	 * @param  {String} m modified version of the verb
	 * @return {String}  the merger result
	 **/
	public String mergeVerb(String o, String s, String m) {
		o = o.substring(0,o.indexOf(s)+s.length());

		String[] x =o.split(s);

		StringBuilder wordList = new StringBuilder();
		for (String word : x) {
			wordList.append(word + m);
		}

		return new String(wordList.deleteCharAt(wordList.length() - 1));
	}


	/**
	 *
	 * Solve verbs using the dictionary
	 * @param  {String} vb verb
	 * @param  {String} to transformation direction
	 * @return {String}    result
	 *
	 **/
	public String solveFromTable(String vb){
		for(String[] vt : this.verbsTable){
			for(String s: vt){
				if(s.equals(vb)){
					return vt[0];
				}
			}
		}
		return null;
	}



	/**
	 * Applies few rules to remove ing / s / ed
	 * is possible, if not then apply porter stemmer
	 * @param  {String} vb verb
	 * @return {String}    result
	 */
	public String stemmer(String vb){
		String[] possibilities;
		if(vb.endsWith("ing")) {
			possibilities = new String[]{
					vb.replace("ing$",""),
					vb.replace("ing$","e"),
					vb.replace(".ing$",""),
					vb.replace("ying$","ie"),
			};

			for(String s : possibilities){
				if(isListedInfinitive(s))
					return s;
			}
		}
		else if(vb.endsWith("s")){
			possibilities = new String[]{
					vb.replace("s$",""),
					vb.replace("es$",""),
					vb.replace("ies$","y"),
			};

			for(String s : possibilities){
				if(isListedInfinitive(s))
					return s;
			}
		}
		else if(vb.endsWith("ed")) {
			possibilities = new String[]{
					vb.replace("ed$",""),
					vb.replace("d$",""),
					vb.replace("ied$","y"),
					vb.replace("ed$","y"),
					vb.replace("ed$","e"),
					vb.replace(".ed$",""),
			};

			for(String s : possibilities){
				if(isListedInfinitive(s))
					return s;
			}

		}
		//return porterStemmer(vb);

		return new PorterStemmer().stripAffixes(vb);
	}


	public String loadJSONFromAsset(Context context, String name) {
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


	public void init(Context context){
		// load contraction data/json
		try {
			JSONObject jsonObject = new JSONObject(this.loadJSONFromAsset(context,"verbs.json"));
			JSONArray verbForms = (JSONArray) jsonObject.get("verbforms");
			JSONArray base = (JSONArray) jsonObject.get("base");

			for(int i=0; i< verbForms.length();i++){
				Object o = verbForms.get(i);
				JSONArray x = (JSONArray)o;

				LinkedList<String> cur = new LinkedList<String>();

				for(int j=0; j<x.length();j++){
					cur.add(x.get(j).toString());
				}


				this.verbsTable.add(cur.toArray(new String[]{}));
				this.infinitives.add(x.get(0).toString());

			}

			for(int j=0; j<base.length();j++){
				this.infinitives.add(base.get(j).toString());
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
