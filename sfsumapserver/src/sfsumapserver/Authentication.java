package sfsumapserver;

import java.io.*;
import java.util.StringTokenizer;

/**
 * <p>
 * Title: Authentication.java
 * </p>
 * 
 * <p>
 * Description: Used when authentication of the player is needed before access
 * is given to certain files.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2012
 * </p>
 * 
 * @author Terry Wong
 */
public class Authentication {

	private String line, item, playerID = "", password = "", IDPassPoints[];

	public Authentication(String ID, String pw) {
		playerID = ID;
		password = pw;
	}

	/**
	 * Checks the incoming information from the client against the user file to
	 * see if the authentication information is correct. If it is correct, the
	 * user can proceed, otherwise he or she is blocked and not allowed to
	 * access files. This class uses the Base64Decoder class to check
	 * information.
	 * 
	 * @param input
	 *            String passed in through the header which is encoded. Use the
	 *            Base64Decoder to decode this information so it can be used to
	 *            check against in the user file.
	 * @return true if data passed in matches what is in the user file, false
	 *         otherwise
	 */
	public String checkAuth() throws IOException {
		Base64Decoder decoder = new Base64Decoder(password);
		try {
			password = decoder.processString();
		} catch (Base64FormatException e) {
			System.out.println("Incorrectly formatted authentication string: "
					+ e);
			return null;
		}
		try {
			File file = new File("htpasswd.log");
			BufferedReader authUserFileReader = new BufferedReader(
					new FileReader(file));
			line = authUserFileReader.readLine();
			while (line != null) {
				if (line.length() == 0) {
					line = authUserFileReader.readLine();
					continue;
				}
				StringTokenizer stringTokenizer = new StringTokenizer(line);
				while (stringTokenizer.hasMoreTokens()) {
					item = stringTokenizer.nextToken();
					IDPassPoints = item.split(":");
					if (playerID.equals(IDPassPoints[0])) {
						decoder = new Base64Decoder(IDPassPoints[1]);
						String tmp = decoder.processString();
						if (password.equals(tmp)) {
							return playerID;
						} else {
							return null;
						}
					}
				}
				line = authUserFileReader.readLine();
			}
			authUserFileReader.close();
			return null;
		} catch (FileNotFoundException e) {
			System.err.println("Error opening AuthUserFile: " + e);
			throw new IOException();
		} catch (IOException e) {
			System.err.println("Error reading AuthUserFile file: " + e);
			throw new IOException();
		} catch (Base64FormatException e) {
			System.err.println("Incorrectly formatted password string: " + e);
			throw new IOException();
		}
	}
}
