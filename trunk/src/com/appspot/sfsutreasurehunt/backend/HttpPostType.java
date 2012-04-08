package com.appspot.sfsutreasurehunt.backend;

import org.json.JSONObject;


/**
 * Again just some initial code for back-end. We can flesh it out
 * more later.
 * 
 * 
 * 
 * Might needed to break this class up into more than one class.
 * Leaving as one class for now.
 * -BB 4/7/12
 * 
 * 
 * To-Do Once functional:
 * 1. Add multi-threading, probably using AsyncTask.
 * 2. Add expanded error handling.
 * -BB 4/7/12
 * 
 * @author bbritten
 *
 */
public class HttpPostType {

	
	/**
	 * 
	 * 
	 * 
	 * @return String with clue to current winning geographic area
	 */
	public String getClue() {
		
		String URL = "http://thecity.sfsu.edu:????/getclue";
		
		JSONObject jsonToSend = null; 
		
		/*
		 * Need Code to convert String[] to JSON object 
		 * If needed create conversion class
		 * with method to convert String[] to JSON object?
		*/
		
		HttpClient newHttpClient = new HttpClient();
		JSONObject jsonReceived = newHttpClient.httpPost(URL,jsonToSend);
		
		/*
		 * Need Code to convert JSON object to String[] 
		 * If needed create conversion class
		 * with method to convert JSON object to String[]?
		*/
		
		String temp = null;
		return temp;
	}
	
	
	/**
	 * 
	 * 
	 * @param currentLatLong String[] of players current latitude and longitude
	 * @return String[] with winning status, if not in the winning area
	 * 			will include indicator(hot, warm, cold)
	 * 
	 */
	public String[] isLocationWinner(String[] currentLatLong) {
		
		String URL = "http://thecity.sfsu.edu:????/iswinning";
		
		JSONObject jsonToSend = null; 
		
		/*
		 * Need Code to convert String[] to JSON object 
		 * If needed create conversion class
		 * with method to convert String[] to JSON object?
		*/
		
		HttpClient newHttpClient = new HttpClient();
		JSONObject jsonReceived = newHttpClient.httpPost(URL,jsonToSend);
		
		/*
		 * Need Code to convert JSON object to String[] 
		 * If needed create conversion class
		 * with method to convert JSON object to String[]?
		*/
		
		String[] temp = null;
		return temp;
	}
	
	
	// Helper method for converting JSON Objects to Strings
	public String jsonToString(JSONObject jsonToString) {
		String convert = null;
		return convert;
	}
	
	public JSONObject stringToJson(String[] stringToJson) {
		JSONObject convert = null;
		return convert;
	}
}