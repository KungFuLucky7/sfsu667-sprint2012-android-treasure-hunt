package sfsumapserver;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * This class is for processing the request input stream.
 * 
 * @author Terry Wong
 */
public class Process {

	private final Socket client;
	private int index, totalPoints = 0;
	private String requestline, playerID, goal, currentLocation, goalLocation,
			option, password = "", tool = "", targetPlayer = "", clue = "",
			currentEffect = "";
	private SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
	private float distance;
	private long startTime, currentTime, elapsedTime;
	private PlayerStats player;
	private boolean authenticationFailure = false, getTopThree = false,
			getTopScores = false, getTopTime = false;
	private static String[] topThreeTeams = { "*****", "*****", "*****" };
	private static String[] topThreeClues = { "****", "****", "****" };
	private static float[] topThreeDistances = { 1000, 1000, 1000 };
	public static HashMap<String, Integer> topScoreTeams = new HashMap<String, Integer>();
	public static HashMap<String, Long> topTimeTeams = new HashMap<String, Long>();
	private PlayerLog loggedPlayer;
	private Authentication authen;

	/**
	 * Default constructor used to reset your variables and data structures for
	 * each new incoming request.
	 */
	public Process(Socket client) {
		this.client = client;
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
	}

	/**
	 * private function used by the request object to parse the rest of the
	 * request message (e.g. other headers and the body of the message) from the
	 * client so it can be used later when actual processing of the request
	 * happens.
	 * 
	 * @param inFile
	 *            BufferedReader object that comes through the socket. Needs to
	 *            be processed properly before the data stored within it can be
	 *            used.
	 */
	public void readRequest() throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(
				client.getInputStream()));
		requestline = in.readLine();
		// Debug
		System.out.println("requestline : " + requestline);

		JsonObject jsonReceived = new JsonParser().parse(requestline)
				.getAsJsonObject();

		// Debug
		// System.out.println("json received : "+jsonReceived.toString());
		// System.out.println("playerid : "+jsonReceived.getAsJsonPrimitive("playerID").getAsString());

		if (jsonReceived.has("playerID")) {
			playerID = jsonReceived.getAsJsonPrimitive("playerID")
					.getAsString();
			// Debug
			// System.out.println("playerID : "+playerID);
		}

		if (jsonReceived.has("currentLocation")) {
			currentLocation = jsonReceived
					.getAsJsonPrimitive("currentLocation").getAsString();
			// Debug
			// System.out.println("currentLocation : "+currentLocation);
		}

		if (jsonReceived.has("option")) {
			option = jsonReceived.getAsJsonPrimitive("option").getAsString();
			// Debug
			// System.out.println("option : "+option);
		}

		if (jsonReceived.has("password")) {
			password = jsonReceived.getAsJsonPrimitive("password")
					.getAsString();
			// Debug
			// System.out.println("password : "+password);
		}

		if (jsonReceived.has("tool")) {
			tool = jsonReceived.getAsJsonPrimitive("tool").getAsString();
			// Debug
			// System.out.println("tool : "+tool);
		}

		if (jsonReceived.has("targetPlayer")) {
			targetPlayer = jsonReceived.getAsJsonPrimitive("targetPlayer")
					.getAsString();
			// Debug
			// System.out.println("targetPlayer : "+targetPlayer);
		} else {
			targetPlayer = playerID;
			// Debug
			// System.out.println("targetPlayer : "+targetPlayer);
		}
	}

	/**
	 * function to process each specific request
	 */
	public synchronized void processRequest() throws IOException {
		if (option.equalsIgnoreCase("signIn")) {
			if (!ServerTable.playerInfoContains(playerID)) {
				loggedPlayer = new PlayerLog(playerID, password, "500");
				loggedPlayer.add();
				ServerTable.setPlayerInfo(playerID);
				ServerTable.getPlayerInfo(playerID).setPlayerPoints(500);
			} else {
				authen = new Authentication(playerID, password);
				String ID = authen.checkAuth();
				System.out.println("signIn response: " + ID);
				if (ID == null || !ServerTable.playerInfoContains(playerID)) {
					authenticationFailure = true;
				}
			}
		} else if (option.equalsIgnoreCase("getClue")) {
			player = ServerTable.getPlayerInfo(playerID);
			goal = ServerTable.getGoal();
			if (goal.equals("")) {
				goal = ServerTable.setNewGoal();
			}
			System.out.println("goal: " + goal);
			if (player.getGoal().equals("")) {
				player.setGoal(goal);
				startTime = System.currentTimeMillis();
				System.out.println("player startTime: " + startTime);
				player.setStartTime(startTime);
			}
			goalLocation = ServerTable.getGoalLocation();
			computeDistance();
			computeElapsedTime();
			player.setDistance(distance);
			setClue();
			updateTopThree();
		} else if (option.equalsIgnoreCase("setTool")) {
			player = ServerTable.getPlayerInfo(playerID);
			goal = ServerTable.getGoal();
			if (goal.equals("")) {
				goal = ServerTable.setNewGoal();
			}
			System.out.println("goal: " + goal);
			if (player.getGoal().equals("")) {
				player.setGoal(goal);
				startTime = System.currentTimeMillis();
				System.out.println("player startTime: " + startTime);
				player.setStartTime(startTime);
			}
			totalPoints -= ServerTable.getToolPrice(tool);
			ServerTable.getPlayerInfo(targetPlayer).activateTool(tool);
			if (tool.equals("steal")) {
				ServerTable.getPlayerInfo(targetPlayer).setStealer(playerID);
			}
			ServerTable.getPlayerInfo(targetPlayer).setPlayerPoints(
					ServerTable.getToolDamage(tool));
			goalLocation = ServerTable.getGoalLocation();
			computeDistance();
			computeElapsedTime();
			player.setDistance(distance);
			setClue();
			updateTopThree();
		} else if (option.equalsIgnoreCase("getTopThree")) {
			getTopThree = true;
		} else if (option.equalsIgnoreCase("getTopScores")) {
			getTopScores = true;
		} else if (option.equalsIgnoreCase("getTopTime")) {
			getTopTime = true;
		}
	}

	/**
	 * function to computer the distance between player's current position and
	 * the goal position.
	 */
	private void computeDistance() {
		index = currentLocation.indexOf(",");
		float longitude1 = Float.valueOf(currentLocation.substring(0, index));
		float latitude1 = Float.valueOf(currentLocation.substring(index + 1));
		index = goalLocation.indexOf(",");
		float longitude2 = Float.valueOf(goalLocation.substring(0, index));
		float latitude2 = Float.valueOf(goalLocation.substring(index + 1));
		distance = (float) Math.sqrt(Math.pow(longitude1 - longitude2, 2)
				+ Math.pow(latitude1 - latitude2, 2));
		System.out.println("distance: " + distance + " degrees");
	}

	/**
	 * function to computer the player's elapsed time.
	 */
	private long computeElapsedTime() {
		currentTime = System.currentTimeMillis();
		if (player.getStartTime() != 0) {
			elapsedTime = currentTime - Long.valueOf(player.getStartTime());
		} else {
			elapsedTime = 0;
		}
		return elapsedTime;
	}

	/**
	 * function to update the top three teams closest to the goal
	 */
	private synchronized void updateTopThree() {
		float ld = distance, tmpd;
		String lid = playerID, tmpn, lc = clue, tmpc;
		if (clue.equals("Win")) {
			for (int i = 0; i < 3; i++) {
				topThreeTeams[i] = "*****";
				topThreeClues[i] = "****";
				topThreeDistances[i] = 1000;
			}
		} else {
			for (int i = 0; i < 3; i++) {
				if (ld < topThreeDistances[i]) {
					tmpn = topThreeTeams[i];
					tmpc = topThreeClues[i];
					tmpd = topThreeDistances[i];
					topThreeTeams[i] = lid;
					topThreeClues[i] = lc;
					topThreeDistances[i] = ld;
					lid = tmpn;
					lc = tmpc;
					ld = tmpd;
				}
			}
		}
	}

	/**
	 * function to update the top score teams
	 */
	private synchronized void updateTopScoreTeams() {
		if (topScoreTeams.size() <= 5) {
			topScoreTeams.put(playerID, player.getPlayerPoints());
		} else {
			String key = "";
			Integer min = Integer.valueOf(Integer.MAX_VALUE);
			for (Map.Entry<String, Integer> entry : topScoreTeams.entrySet()) {
				if (min.compareTo(entry.getValue()) > 0) {
					key = entry.getKey();
					min = entry.getValue();
				}
			}
			if (player.getPlayerPoints() > min) {
				topScoreTeams.remove(key);
				topScoreTeams.put(playerID, player.getPlayerPoints());
			}
		}
	}

	/**
	 * function to update the top time ranked teams
	 */
	private synchronized void updateTopTimeTeams() {
		if (topTimeTeams.size() <= 5) {
			topTimeTeams.put(playerID, elapsedTime / 1000);
		} else {
			String key = "";
			Long max = Long.valueOf(Long.MIN_VALUE);
			for (Map.Entry<String, Long> entry : topTimeTeams.entrySet()) {
				if (max.compareTo(entry.getValue()) < 0) {
					key = entry.getKey();
					max = entry.getValue();
				}
			}
			if (elapsedTime / 1000 < max) {
				topTimeTeams.remove(key);
				topTimeTeams.put(playerID, elapsedTime / 1000);
			}
		}
	}

	/**
	 * function to generate the proper clue
	 */
	private synchronized void setClue() {
		if (player.checkStolenWin()) {
			clue = "Win";
			totalPoints += 500;
			if (elapsedTime <= 60000) {
				totalPoints += 500;
			}
			player.resetStolenWin();
			updateTopTimeTeams();
			ServerTable.removeGoal();
			ServerTable.resetGame();
		} else if (player.checkTaunt()) {
			clue = "taunt";
			player.resetTaunt();
		} else {
			if (!player.getCurrentEffect().equals("")) {
				currentEffect = player.getCurrentEffect();
			}
			if (distance <= 0.00005) {
				if (currentEffect.equals("steal")) {
					clue = "steal";
					ServerTable.getPlayerInfo(player.getStealer())
							.setStolenWin();
				} else if (currentEffect.equals("lock-out")) {
					clue = "lock-out";
				} else {
					clue = "Win";
					totalPoints += 500;
					if (elapsedTime <= 60000) {
						totalPoints += 500;
					}
					updateTopTimeTeams();
					ServerTable.removeGoal();
					ServerTable.resetGame();
				}
			} else if (distance <= 0.00050) {
				clue = "Hot";
				if (currentEffect.equals("") && !player.checkHotOnce()) {
					totalPoints += 200;
					player.setHotOnce();
				}
			} else if (distance <= 0.00100) {
				clue = "Warm";
				if (currentEffect.equals("") && !player.checkWarmOnce()) {
					totalPoints += 100;
					player.setWarmOnce();
				}
			} else {
				clue = "Cold";
			}
			if (!clue.equals("Win")) {
				if (currentEffect.equals("smokeBomb")) {
					distance = 1000000;
					clue = "smokeBomb";
				} else if (currentEffect.equals("drunkMonkey")) {
					distance = (float) 0.00200;
					clue = "Cold";
				}
			}
		}
		player.setClue(clue);
		player.setPlayerPoints(totalPoints);
		updateTopScoreTeams();
	}

	/**
	 * function to write the proper response
	 */
	public void writeResponse() throws IOException {
		BufferedOutputStream out = new BufferedOutputStream(
				client.getOutputStream());
		Date date = new Date();
		PrintWriter writer = new PrintWriter(out, true);
		writer.println("HTTP/1.1 200 OK");
		writer.println("Server: SFSUMapServer");
		writer.println("Date: " + dateFormat.format(date));
		writer.println("Content-Type: text/plain");
		writer.println("Connection: close");
		String output = "{";
		if (option.equalsIgnoreCase("signIn")) {
			output += "\"signIn\":\"";
			if (!authenticationFailure) {
				output += "Good";
			} else {
				output += "Bad";
			}
			output += "\"";
		} else if (option.equalsIgnoreCase("getClue")) {
			output += "\"clue\":\"" + clue + "\"";
			output += ", \"distance\":\"" + distance + "\"";
			output += ", \"goalLocation\":\"" + goalLocation + "\"";

			// for elapsed time
			output += ", \"elapsedTime\":\""
					+ dateFormat.format(new Date(elapsedTime)) + "\"";

			output += ", \"playerPoints\":\"" + player.getPlayerPoints() + "\"";
		} else if (option.equalsIgnoreCase("setTool")) {
			output += "\"tool\":\"" + tool + "\"";
			output += ", \"distance\":\"" + distance + "\"";
			output += ", \"goalLocation\":\"" + goalLocation + "\"";

			// for elapsed time
			output += ", \"elapsedTime\":\""
					+ dateFormat.format(new Date(elapsedTime)) + "\"";

			output += ", \"playerPoints\":\"" + player.getPlayerPoints() + "\"";
			output += ", \"targetPlayer\":\"" + targetPlayer + "\"";
		} else if (getTopThree) {
			// get each top 3 team's clue and distance by using keywords
			// "TopTeam1",
			// "TopTeam2", "TopTeam3"
			output += "\"TopTeam1\":\"" + topThreeTeams[0] + " "
					+ topThreeClues[0] + " " + topThreeDistances[0] + "\"";
			output += ", \"TopTeam2\":\"" + topThreeTeams[1] + " "
					+ topThreeClues[1] + " " + topThreeDistances[1] + "\"";
			output += ", \"TopTeam3\":\"" + topThreeTeams[2] + " "
					+ topThreeClues[2] + " " + topThreeDistances[2] + "\"";
		} else if (getTopScores) {
			// get each top 5 team's top score by using keywords "TopTeam1",
			// "TopTeam2", "TopTeam3", etc.
			HashMap<String, Integer> tmp = new HashMap<String, Integer>();
			tmp.putAll(topScoreTeams);
			String key = "";
			Integer max = Integer.valueOf(Integer.MIN_VALUE);
			for (int i = 0; i < 5; i++) {
				for (Map.Entry<String, Integer> entry : tmp.entrySet()) {
					if (max.compareTo(entry.getValue()) < 0) {
						key = entry.getKey();
						max = entry.getValue();
					}
				}
				if (tmp.get(key) != null) {
					output += "\"TopTeam" + (i + 1) + "\":\"" + key + ": "
							+ tmp.get(key) + " points\", ";
					tmp.remove(key);
				}
			}
		} else if (getTopTime) {
			// get each top 5 team's top time by using keywords "TopTeam1",
			// "TopTeam2", "TopTeam3", etc.
			HashMap<String, Long> tmp = new HashMap<String, Long>();
			tmp.putAll(topTimeTeams);
			String key = "";
			Long min = Long.valueOf(Long.MAX_VALUE);
			for (int i = 0; i < 5; i++) {
				for (Map.Entry<String, Long> entry : tmp.entrySet()) {
					if (min.compareTo(entry.getValue()) > 0) {
						key = entry.getKey();
						min = entry.getValue();
					}
				}
				if (tmp.get(key) != null) {
					output += "\"TopTeam" + (i + 1) + "\":\"" + key + ": "
							+ tmp.get(key) + " seconds\", ";
					tmp.remove(key);
				}
			}
		}
		output += "}";
		writer.println("Content-Length: " + output.length());
		System.out.println("Content-Length: " + output.length());
		writer.println("");
		writer.println(output);
		System.out.println("\n" + output + "\n");
		writer.flush();
		writer.close();
		out.close();
	}

	/**
	 * function to record all the stats to log files
	 */
	public synchronized void writeToLog() throws Exception {
		try {
			player = ServerTable.getPlayerInfo(playerID);
			loggedPlayer = new PlayerLog(playerID);
			System.out.println("player points: " + player.getPlayerPoints());
			loggedPlayer.update(String.valueOf(player.getPlayerPoints()));
			String log = "";
			for (Map.Entry<String, Integer> entry : topScoreTeams.entrySet()) {
				log += entry.getKey() + ":" + entry.getValue() + " ";
			}
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(
					"score rankings.log")));
			bw.write(log);
			bw.flush();
			bw.close();
			if (clue.equals("Win")) {
				log = "";
				for (Map.Entry<String, Long> entry : topTimeTeams.entrySet()) {
					log += entry.getKey() + ":" + entry.getValue() + " ";
				}
				bw = new BufferedWriter(new FileWriter(new File(
						"time rankings.log")));
				bw.write(log);
				bw.flush();
				bw.close();
			}
		} catch (IOException e) { // if there is an error in reading the file
			System.err.println(e.getMessage());
		}
	}
}
