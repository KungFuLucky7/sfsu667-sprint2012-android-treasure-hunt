package sfsumapserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;

public class ServerTable extends Object {

	/**
	 * The ServerTable class also contains all the program related information
	 * that needs to be kept track of.
	 * 
	 * 
	 * @author Terry Wong
	 * @edited Benjamin Britten
	 */
	private static String buildingName[] = { "Parking Garage",
			"Student Services Building", "Village C",
			"The Village at Centennial Square", "Dining Center", "Burk Hall",
			"Student Health Center", "Ethnic Studies & Psychology",
			"Gymnasium", "Thornton Hall", "Hensill Hall", "Trailers",
			"Science Building", "Cesar Chavez Student Center",
			"SFSU Bookstore", "Fine Arts Building", "Creative Arts Building",
			"Humanities Building", "J. Paul Leonard Library",
			"Administration Building", "Business Building", "HSS Building",
			"CS Lab" };

	private static String buildingLocation[] = { "37.724213,-122.4812",
			"37.723413,-122.480602", "37.723008,-122.480838",
			"37.72315,-122.482935", "37.723674,-122.482898",
			"37.723018,-122.479582", "37.723317,-122.479784",
			"37.723375,-122.479274", "37.723343,-122.478239",
			"37.723684,-122.476873", "37.723457,-122.475862",
			"37.72392,-122.47631", "37.723167,-122.475594",
			"37.722397,-122.478496", "37.722628,-122.478365",
			"37.72246,-122.479808", "37.721716,-122.479475",
			"37.722356,-122.480953", "37.721391,-122.477954",
			"37.721198,-122.476739", "37.722083,-122.476737",
			"37.721889,-122.476074", "37.722486025,-122.4751094625" };

	private static String buildingMessage[] = {
			"There are lots of cars here.",
			"Where school tours start.",
			"Clue: Village C.",
			"They have cheap pizza 24/7.",
			"Where freshman in dorms eat.",
			"They have a three course restuarant here.",
			"Where to go when you're feeling sick.",
			"SFSU is the only CSU with a dedicated college to this area of study.",
			"You can swim here.",
			"This is where CS students go to classes most oftenly.",
			"The biology departement is here.", "Where CSC 667 has class.",
			"CS Lab is in this building.", "Where the student council meets.",
			"Get your textbooks here.", "They make art here.",
			"They make complex art here.", "Where the English majors go.",
			"There's a Peets coffee here.",
			"The people in charge of the school are here.",
			"Marketing people go here.", "History majors go here.",
			"This is where CS students study." };

	private static String toolName[] = { "taunt", "dizzyMonkey", "smokeBomb",
			"clearSky", "steal", "lock-out", "compass" };

	private static String toolMessage[] = { "You've been taunted!",
			"You are a dizzyMonkey!", "You've been smoke bombed!",
			"You sky is now clear!", "Someone is trying to steal your win!",
			"You have been locked out for winning!", "You have a radar!" };

	private static String toolWithDuration[] = { "dizzyMonkey", "smokeBomb",
			"steal", "lock-out", "compass" };

	private static Integer toolPrice[] = { 10, 100, 500, 700, 900, 950, 700 };
	// private static Integer toolDamage[] = { -5, -20, -50, 200, -300, -200, 0
	// };

	private static HashMap<String, String> buildingInfo = new HashMap<String, String>();
	private static HashMap<String, String> buildingClues = new HashMap<String, String>();
	private static HashMap<String, Integer> toolsCosts = new HashMap<String, Integer>();
	// private static HashMap<String, Integer> toolsEffects = new
	// HashMap<String, Integer>();
	private static HashMap<String, String> toolsClues = new HashMap<String, String>();
	private static HashMap<String, PlayerStats> playerInfo = new HashMap<String, PlayerStats>();
	private static ArrayList<String> durationalTools = new ArrayList<String>();

	private static String goal = "";
	private static String debugGoal = "";

	public static String setNewGoal() {
		if (!debugGoal.isEmpty() && buildingInfo.containsKey(debugGoal))
			// Debug
			goal = debugGoal;
		else {
			int randomIndex = (int) (Math.random() * buildingName.length);
			goal = buildingName[randomIndex];
		}
		return goal;
	}

	public static void removeGoal() {
		goal = "";
	}

	public static String getGoal() {
		if (goal.equals("")) {
			goal = ServerTable.setNewGoal();
		}
		return goal;
	}

	public static String getGoalLocation() {
		return buildingInfo.get(goal);
	}

	public static void setDebugGoal(String dg) {
		debugGoal = dg;
	}

	public static String getDebugGoal() {
		return debugGoal;
	}

	public static String getBuildingLocation(String buildingName) {
		return buildingInfo.get(buildingName);
	}

	public static String getBuildingClue(String buildingName) {
		return buildingClues.get(buildingName);
	}

	public static void setPlayerInfo(String playerID) {
		playerInfo.put(playerID, new PlayerStats(playerID));
	}

	public static PlayerStats getPlayerInfo(String playerID) {
		return playerInfo.get(playerID);
	}

	public static Set<String> getAllPlayerNames() {
		return playerInfo.keySet();
	}

	public static boolean playerInfoContains(String playerID) {
		return playerInfo.containsKey(playerID);
	}

	public static Integer getToolPrice(String toolName) {
		return toolsCosts.get(toolName);
	}

	public static String getToolMessage(String tool) {
		return toolsClues.get(tool);
	}

	// public static Integer getToolDamage(String toolName) {
	// return toolsEffects.get(toolName);
	// }

	public static ArrayList<String> getDurationalTools() {
		return durationalTools;
	}

	public static void resetGame() {
		for (Map.Entry<String, PlayerStats> entry : playerInfo.entrySet()) {
			entry.getValue().setGoal("");
			entry.getValue().resetCurrentEffect();
		}
	}

	private static void loadUserFile() {
		try {
			int index, points, score;
			long time;
			String line, item, ID, IDPassPoints[];
			File file = new File("htpasswd.log");
			if (!file.exists()) {
				file.createNewFile();
			}
			BufferedReader fileReader = new BufferedReader(new FileReader(file));
			line = fileReader.readLine();
			while (line != null) {
				if (line.length() == 0) {
					line = fileReader.readLine();
					continue;
				}
				StringTokenizer stringTokenizer = new StringTokenizer(line);
				while (stringTokenizer.hasMoreTokens()) {
					item = stringTokenizer.nextToken();
					IDPassPoints = item.split(":");
					ID = IDPassPoints[0];
					points = Integer.valueOf(IDPassPoints[2]);
					setPlayerInfo(ID);
					getPlayerInfo(ID).setPlayerPoints(points);
				}
				line = fileReader.readLine();
			}
			fileReader.close();
			file = new File("score rankings.log");
			if (!file.exists()) {
				file.createNewFile();
			}
			fileReader = new BufferedReader(new FileReader(file));
			line = fileReader.readLine();
			while (line != null) {
				if (line.length() == 0) {
					line = fileReader.readLine();
					continue;
				}
				StringTokenizer stringTokenizer = new StringTokenizer(line);
				while (stringTokenizer.hasMoreTokens()) {
					item = stringTokenizer.nextToken();
					index = item.indexOf(":");
					ID = item.substring(0, index);
					score = Integer.valueOf(item.substring(index + 1));
					Process.topScoreTeams.put(ID, score);
				}
				line = fileReader.readLine();
			}
			fileReader.close();
			file = new File("time rankings.log");
			if (!file.exists()) {
				file.createNewFile();
			}
			fileReader = new BufferedReader(new FileReader(file));
			line = fileReader.readLine();
			while (line != null) {
				if (line.length() == 0) {
					line = fileReader.readLine();
					continue;
				}
				StringTokenizer stringTokenizer = new StringTokenizer(line);
				while (stringTokenizer.hasMoreTokens()) {
					item = stringTokenizer.nextToken();
					index = item.indexOf(":");
					ID = item.substring(0, index);
					time = Long.valueOf(item.substring(index + 1));
					Process.topTimeTeams.put(ID, time);
				}
				line = fileReader.readLine();
			}
			fileReader.close();
		} catch (Exception e) {
			System.err.println("Error opening user files: " + e);
		}
	}

	public static void init() {
		for (int i = 0; i < buildingName.length; i++) {
			buildingInfo.put(buildingName[i], buildingLocation[i]);
			buildingClues.put(buildingName[i], buildingMessage[i]);
		}
		for (int i = 0; i < toolName.length; i++) {
			toolsCosts.put(toolName[i], toolPrice[i]);
		}
		// Future Use. Not dealing damage to other players in this version of
		// game
		// for (int i = 0; i < toolName.length; i++) {
		// toolsEffects.put(toolName[i], toolDamage[i]);
		// }
		for (int i = 0; i < toolName.length; i++) {
			toolsClues.put(toolName[i], toolMessage[i]);
		}
		durationalTools.addAll(Arrays.asList(toolWithDuration));
		loadUserFile();
	}
}
