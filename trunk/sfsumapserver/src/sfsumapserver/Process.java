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
import java.util.Set;
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

	private String requestline, playerID = "", goalName = "",
			currentLocation = "", goalLocation = "", option = "",
			password = "", tool = "", targetPlayer = "", indicator = "",
			currentEffect = "", message = "";

	private SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
	private float distance;
	private long startTime, currentTime, elapsedTime;
	private PlayerStats player;

	private boolean authenticationFailure = false;

	private static HashMap<String, Float> topThreeTeams = new HashMap<String, Float>();
	private static HashMap<String, String> topThreeClues = new HashMap<String, String>();
	public static HashMap<String, Integer> topScoreTeams = new HashMap<String, Integer>();
	public static HashMap<String, Long> topTimeTeams = new HashMap<String, Long>();

	private PlayerLog loggedPlayer;
	private Authentication authen;
	private static String lastWinner = "nobody";

	/**
	 * Default constructor used to reset your variables and data structures for
	 * each new incoming request.
	 */
	public Process(Socket client) {
		this.client = client;
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
	}

	/**
	 * function used by the request object to parse the rest of the request
	 * message (e.g. other headers and the body of the message) from the client
	 * so it can be used later when actual processing of the request happens.
	 * 
	 */
	public void readRequest() throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(
				client.getInputStream()));
		requestline = in.readLine();
		while (!requestline.isEmpty())
			requestline = in.readLine();
		requestline = in.readLine();
		// Debug
		System.out.println("requestline : " + requestline);

		JsonObject jsonReceived = new JsonParser().parse(requestline)
				.getAsJsonObject();

		if (jsonReceived.has("playerID")) {
			playerID = jsonReceived.getAsJsonPrimitive("playerID")
					.getAsString();
		}

		if (jsonReceived.has("currentLocation")) {
			currentLocation = jsonReceived
					.getAsJsonPrimitive("currentLocation").getAsString();
		}

		if (jsonReceived.has("option")) {
			option = jsonReceived.getAsJsonPrimitive("option").getAsString();
		}

		if (jsonReceived.has("password")) {
			password = jsonReceived.getAsJsonPrimitive("password")
					.getAsString();
		}

		if (jsonReceived.has("tool")) {
			tool = jsonReceived.getAsJsonPrimitive("tool").getAsString();
		}

		if (jsonReceived.has("targetPlayer")) {
			targetPlayer = jsonReceived.getAsJsonPrimitive("targetPlayer")
					.getAsString();
		} else
			targetPlayer = playerID;

		if (jsonReceived.has("message")) {
			message = jsonReceived.getAsJsonPrimitive("message").getAsString();
		}
	}

	/**
	 * function to process each specific request
	 */
	public synchronized void processRequest() throws IOException {
		if (option.equalsIgnoreCase("signIn")) {
			if (!(ServerTable.playerInfoContains(playerID))) {
				loggedPlayer = new PlayerLog(playerID, password, "5000");
				loggedPlayer.add();
				ServerTable.setPlayerInfo(playerID);
				ServerTable.getPlayerInfo(playerID).setPlayerPoints(5000);
			} else {
				authen = new Authentication(playerID, password);
				String ID = authen.checkAuth();
				System.out.println("signIn response: " + ID);
				if (ID == null || !ServerTable.playerInfoContains(playerID)) {
					authenticationFailure = true;
				}
			}
			player = ServerTable.getPlayerInfo(playerID);
		} else if (option.equalsIgnoreCase("getClue")) {
			player = ServerTable.getPlayerInfo(playerID);
			goalName = ServerTable.getGoal();
			System.out.println("goalName: " + goalName);
			if (player.getGoal().equals("")) {
				player.setGoal(goalName);
				startTime = System.currentTimeMillis();
				System.out.println("player startTime: " + startTime);
				player.setStartTime(startTime);
				message = "Starting a new game, the last winner was "
						+ lastWinner + ". ";
			}
			goalLocation = ServerTable.getGoalLocation();
			computeDistance();
			computeElapsedTime();
			player.setDistance(distance);
			setIndicator();
			updateTopThree();
		} else if (option.equalsIgnoreCase("setTool")) {
			player = ServerTable.getPlayerInfo(playerID);
			goalName = ServerTable.getGoal();
			if (goalName.equals("")) {
				goalName = ServerTable.setNewGoal();
			}
			System.out.println("goal: " + goalName);
			if (player.getGoal().equals("")) {
				player.setGoal(goalName);
				startTime = System.currentTimeMillis();
				System.out.println("player startTime: " + startTime);
				player.setStartTime(startTime);
				message = "Starting a new game, the last winner was "
						+ lastWinner + ". ";
			}
			totalPoints -= ServerTable.getToolPrice(tool);
			ServerTable.getPlayerInfo(targetPlayer).activateTool(tool);
			if (tool.equals("steal")) {
				ServerTable.getPlayerInfo(targetPlayer).setStealer(playerID);
			}
			// Future Use. Not dealing damage to other players in this version
			// of game
			// ServerTable.getPlayerInfo(targetPlayer).setPlayerPoints(
			// ServerTable.getToolDamage(tool));
			goalLocation = ServerTable.getGoalLocation();
			computeDistance();
			computeElapsedTime();
			player.setDistance(distance);
			updateTopThree();
		}
	}

	/**
	 * function to compute the distance between player's current position and
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
	 * function to compute the player's elapsed time.
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
		if (indicator.equals("Win")) {
			topThreeTeams.clear();
			topThreeClues.clear();
		} else if (topThreeTeams.size() <= 3
				&& !topThreeTeams.containsKey(playerID)) {
			topThreeTeams.put(playerID, distance);
			topThreeClues.put(playerID, indicator);
			System.out.println(topThreeTeams.get(playerID));
		} else {
			String key = "";
			Float max = Float.valueOf(Float.MIN_VALUE);
			for (Map.Entry<String, Float> entry : topThreeTeams.entrySet()) {
				if (max.compareTo(entry.getValue()) < 0) {
					key = entry.getKey();
					max = entry.getValue();
				}
			}
			if (distance < max && !topThreeTeams.containsKey(playerID)) {
				topThreeTeams.put(playerID, distance);
				topThreeClues.put(playerID, indicator);
				topThreeTeams.remove(key);
			} else if (distance < max && topThreeTeams.containsKey(playerID)) {
				topThreeTeams.put(playerID, distance);
				topThreeClues.put(playerID, indicator);
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
	 * function to generate the indicator : WIN, HOT, WARM, COLD, SMOKE
	 */
	private synchronized void setIndicator() {
		if (player.checkStolenWin()) {
			indicator = "Win";
			message += "You have a stolen win!";
			totalPoints += 500;
			if (elapsedTime <= 120000) {
				totalPoints += 500;
			}
			lastWinner = playerID;
			player.resetStolenWin();
			updateTopTimeTeams();
			ServerTable.removeGoal();
			ServerTable.resetGame();
		} else if (player.checkTaunt()) {
			indicator = "taunt";
			if (message == null || message.equals(""))
				message = ServerTable.getToolMessage("taunt");
			player.resetTaunt();
		} else {
			if (player.getCurrentEffect().equals("")
					&& player.checkClearSky() == true) {
				message = ServerTable.getToolMessage("clearSky");
				player.resetClearSky();
			} else if (!player.getCurrentEffect().equals("")) {
				currentEffect = player.getCurrentEffect();
				message = ServerTable.getToolMessage(currentEffect);
			}
			if (distance <= 0.001) {
				if (currentEffect.equals("steal")) {
					indicator = "steal";
					message = "Your win have been stolen!";
					ServerTable.getPlayerInfo(player.getStealer())
							.setStolenWin();
				} else if (currentEffect.equals("lock-out")) {
					indicator = "lock-out";
				} else {
					indicator = "Win";
					message += "Congratulations! You have won!";
					totalPoints += 500;
					if (elapsedTime <= 120000) {
						totalPoints += 500;
					}
					lastWinner = playerID;
					updateTopTimeTeams();
					ServerTable.removeGoal();
					ServerTable.resetGame();
				}
			} else if (distance <= 0.003) {
				indicator = "Hot";
				if (currentEffect.equals("") && !player.checkHotOnce()) {
					message += "You are hot!";
					totalPoints += 200;
					player.setHotOnce();
				}
			} else if (distance <= 0.006) {
				indicator = "Warm";
				if (currentEffect.equals("") && !player.checkWarmOnce()) {
					message += "You are warm!";
					totalPoints += 100;
					player.setWarmOnce();
				}
			} else {
				indicator = "Cold";
			}
			if (!indicator.equals("Win")) {
				if (currentEffect.equals("smokeBomb")) {
					distance = 1000000;
					indicator = "smokeBomb";
				} else if (currentEffect.equals("dizzyMonkey")) {
					distance = (float) 0.00200;
					indicator = "Cold";
				}
			}
		}

		player.setIndicator(indicator);
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
				output += "Good\"";
				output += ", \"playerPoints\":\"" + player.getPlayerPoints();
			} else {
				output += "Bad";
			}
			output += "\"";
		} else if (option.equalsIgnoreCase("getClue")) {
			// output += "\"clue\":\"" + "There is a Peet's Coffee there!" +
			// "\"";
			output += "\"clue\":\"" + message + "\"";
			output += ", \"distance\":\"" + distance + "\"";
			output += ", \"goalLocation\":\"" + goalLocation + "\"";
			output += ", \"indicator\":\"" + indicator + "\"";

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
		} else if (option.equalsIgnoreCase("getTopThree")) {
			// get each top 3 team's clue and distance by using keywords
			// "TopTeam1",
			// "TopTeam2", "TopTeam3"
			HashMap<String, Float> tmp = new HashMap<String, Float>();
			tmp.putAll(topThreeTeams);
			String key = "";
			Float min = Float.valueOf(Integer.MAX_VALUE);
			for (int i = 0; i < 3; i++) {
				for (Map.Entry<String, Float> entry : tmp.entrySet()) {
					if (min.compareTo(entry.getValue()) > 0) {
						key = entry.getKey();
						min = entry.getValue();
					}
				}
				if (tmp.get(key) != null) {
					output += "\"TopTeam" + (i + 1) + "\":\"" + key + " "
							+ topThreeClues.get(key) + " " + tmp.get(key)
							+ "\",";
					tmp.remove(key);
					min = Float.valueOf(Integer.MAX_VALUE);
				}
			}
		} else if (option.equalsIgnoreCase("getTopScores")) {
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
					max = Integer.valueOf(Integer.MIN_VALUE);
				}
			}
		} else if (option.equalsIgnoreCase("getTopTime")) {
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
					min = Long.valueOf(Long.MAX_VALUE);
				}
			}
		} else if (option.equalsIgnoreCase("getPlayers")) {
			Set<String> keySet = ServerTable.getAllPlayerNames();
			String[] allPlayers = keySet.toArray(new String[0]);
			if (allPlayers.length > 0) {
				output += "\"players\":[\"" + allPlayers[0];
				for (int i = 1; i < allPlayers.length; i++) {
					output += "\", \"" + allPlayers[i];
				}
				output += "\"]";
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
			if (indicator.equals("Win")) {
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