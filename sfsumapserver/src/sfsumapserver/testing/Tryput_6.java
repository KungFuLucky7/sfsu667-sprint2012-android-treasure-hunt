package sfsumapserver.testing;

/*
 * This is a class for testing the Put, Trace or other edited methods.
 */
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * 
 * @author Terry Wong
 */
public class Tryput_6 {

	public Tryput_6() {
	}

	public static void main(String[] args) {
		String s;
		boolean which = true;
		if (which) {
			try {
				// Socket server = new Socket("localhost", 9255);
				Socket server = new Socket("thecity.sfsu.edu", 8088);
				BufferedReader fromServer = new BufferedReader(
						new InputStreamReader(server.getInputStream()));
				PrintWriter toServer = new PrintWriter(
						server.getOutputStream(), true);

				String stringToJson = "Testing!\n"
						+ "\n"
						+ "{\"playerID\":\"DF\", \"currentLocation\":\"37.723598,-122.480807\","
						+ " \"option\":\"setTool\", \"tool\":\"smokeBomb\", \"targetPlayer\":\"tw\", \"message\":\"Screw You!\"}";

				toServer.println(stringToJson);
				toServer.println("");

				while ((s = fromServer.readLine()) != null) {
					System.out.println(s);
				}
				fromServer.close();
				toServer.close();
				server.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			try {
				Socket server = new Socket("localhost", 3388);
				BufferedReader fromServer = new BufferedReader(
						new InputStreamReader(server.getInputStream()));
				PrintWriter toServer = new PrintWriter(
						server.getOutputStream(), true);

				toServer.println("Trace /~terry/test.html HTTP/1.1");
				toServer.println("Host: localhost:3388");
				toServer.println("User-Agent: Internet Explorer/5.0 (Windows NT 6.1; rv:10.0.2) Gecko/20100101 Firefox/10.0.2");
				toServer.println("Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
				toServer.println("Accept-Language: en-us,en;q=0.5");
				toServer.println("Accept-Encoding: gzip, deflate");
				toServer.println("Connection: keep-alive");
				// toServer.println("If-Modified-Since: Fri, 15 Feb 2008 22:56:36 GMT");
				toServer.println();

				while ((s = fromServer.readLine()) != null) {
					System.out.println(s);
				}
				fromServer.close();
				toServer.close();
				server.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
