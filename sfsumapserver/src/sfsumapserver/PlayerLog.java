package sfsumapserver;

import java.io.*;
import java.util.StringTokenizer;

/**
 * A class for logging of a player's stats.
 * 
 * @author Terry Wong
 */
public class PlayerLog {

	File file;
	private String userInfoString, line, item, playerID = "", IDPassPoints[],
			fileholder = "";

	public PlayerLog(String ID) {
		file = new File("htpasswd.log");
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
		try {
			if (!file.exists()) {
				file.createNewFile();
			}
			BufferedReader reader = new BufferedReader(new FileReader(file));
			line = reader.readLine();
			while (line != null) {
				if (line.length() == 0) {
					fileholder += "\n";
					line = reader.readLine();
					continue;
				}
				StringTokenizer stringTokenizer = new StringTokenizer(line);
				while (stringTokenizer.hasMoreTokens()) {
					item = stringTokenizer.nextToken();
					IDPassPoints = item.split(":");
					if (playerID.equals(IDPassPoints[0])) {
						item = IDPassPoints[0] + ":" + IDPassPoints[1] + ":"
								+ ps;
					}
					fileholder += item + " ";
				}
				fileholder += "\n";
				line = reader.readLine();
			}
			if (!fileholder.equals("")) {
				BufferedWriter bw = new BufferedWriter(new FileWriter(file));
				bw.write(fileholder);
				bw.flush();
				bw.close();
			}
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}
}
