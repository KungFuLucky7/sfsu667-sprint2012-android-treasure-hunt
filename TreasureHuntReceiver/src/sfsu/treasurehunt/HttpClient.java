package sfsu.treasurehunt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

public class HttpClient {

	/**
	 * Sends a Request that includes the given JSON object to the 
	 * given URL and reads the response. Creates a new JSON object from the
	 * JSON sent in the response and returns this new JSON Object to the
	 * caller.
	 * 
	 * @param URL the URL for the game server
	 * @param jsonToSend the JSONObject to be sent to the server
	 * @return a JSONObject received from the server
	 */
	public JSONObject httpPost(String URL, JSONObject jsonToSend) {
		
		JSONObject jsonReceived = null;
		
		try {
			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpPost httpPostRequest = new HttpPost(URL);
			
			// Debug print out body of client request to server
			System.out.println("json sent : "+jsonToSend.toString());
			
			StringEntity stringToSend = new StringEntity(jsonToSend.toString());			
			httpPostRequest.setEntity(stringToSend);
			
			HttpResponse response = httpClient.execute(httpPostRequest);
			HttpEntity responseEntity = response.getEntity();
			
			InputStream input = responseEntity.getContent();
			BufferedReader bfReader = new BufferedReader(new InputStreamReader(input));
			StringBuffer stringBuf = new StringBuffer();
			String currentLine;
			while( (currentLine = bfReader.readLine()) != null) {
				stringBuf.append(currentLine);
			}
			
			jsonReceived = new JSONObject(stringBuf.toString());
			
			// Debug print out body of response send from server
			System.out.println("json received : "+jsonReceived.toString());
			
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
		
		return jsonReceived;
	}
}