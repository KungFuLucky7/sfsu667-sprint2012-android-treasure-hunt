package com.appspot.sfsutreasurehunt.backend;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;

import org.json.JSONObject;


/**
 * This is just an intial idea.
 * I'm not sure if we'll need to implement with JSON or not.
 * We can flesh this out more when we start working on the server.
 * 
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
public class HttpClient {

	/**
	 * Sends a Request to the given URL that includes the given JSON
	 * object and reads a response. Creates a new JSON object from the
	 * response and returns this new JSON Object to the calling class
	 * to be parsed.
	 * 
	 * @param URL the URL for the current calling request
	 * @param jsonToSend the JSONObject to be sent to the server
	 * @return a JSONObject received from server
	 */
	public JSONObject httpPost(String URL, JSONObject jsonToSend) {

		HttpPost httpPostRequest = new HttpPost(URL);
		HttpResponse response = null;	
		JSONObject jsonReceived = null;
			
		return jsonReceived;			
	}
}