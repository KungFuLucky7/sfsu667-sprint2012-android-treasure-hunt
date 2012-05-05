package sfsumapserver;

import java.io.*;
import java.util.StringTokenizer;

/**
 * Add new username/password pair to the user.txt file.
 * 
 * @author Terry Wong
 */
public class PlayerLog {

	File file;
	private String userInfoString, line, playerID = "", IDPassPoints[],
			fileholder = "";

	public PlayerLog(String ID) {
		playerID = ID;
	}

	public PlayerLog(String ID, String pw, String ps) {
		file = new File("htpasswd.log");
		playerID = ID;
		userInfoString = ID + ":" + pw + ":" + ps;
	}

	public void add() {
		try {

			if (!file.exists()) {
				file.createNewFile();
			}
			BufferedReader reader = new BufferedReader(new FileReader(file));
			line = reader.readLine();
			if (line == null || !line.contains(userInfoString)) {
				BufferedWriter bw = new BufferedWriter(new FileWriter(file,
						true));
				bw.write(userInfoString + " ");
				bw.flush();
				bw.close();
			}
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}

	public void update(String ps) throws Exception {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		line = reader.readLine();
		while (line != null) {
			if (line.length() == 0) {
				line = reader.readLine();
				continue;
			}
			StringTokenizer stringTokenizer = new StringTokenizer(line);
			while (stringTokenizer.hasMoreTokens()) {
				line = stringTokenizer.nextToken();
				IDPassPoints = line.split(":");
				if (playerID.equals(IDPassPoints[0])) {
					line = IDPassPoints[0] + IDPassPoints[1] + ps;
				}
				fileholder += line + " ";
			}
			line = reader.readLine();
		}
		BufferedWriter bw = new BufferedWriter(new FileWriter(file));
		bw.write(fileholder);
		bw.flush();
		bw.close();
	}
}
