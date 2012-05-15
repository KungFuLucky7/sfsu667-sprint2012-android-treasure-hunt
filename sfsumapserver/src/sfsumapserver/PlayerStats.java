package sfsumapserver;

/**
 * A class for keeping track of all of a player's stats.
 * 
 * @author Terry Wong
 */
public class PlayerStats {

	private String playerID = "", playerGoal = "", currentIndicator = "",
			toolInEffect = "", stealer = "";
	// private currentLocation = ""; -For future use.
	private int playerPoints = 0;
	private float playerDistance;
	private boolean HotOnce = false, WarmOnce = false, isTaunt = false,
			isClearSky = false, stolenWin = false;
	private long startTime = 0, effectStartTime = 0, effectDuration = 0;

	public PlayerStats(String ID) {
		playerID = ID;
	}

	public String getPlayerID() {
		return playerID;
	}

	/*
	 * public void setCurrentLocation(String location) { currentLocation =
	 * location; }
	 * 
	 * public String getCurrentLocation() { return currentLocation; }
	 */

	public void setGoal(String goal) {
		playerGoal = goal;
	}

	public String getGoal() {
		return playerGoal;
	}

	public void setIndicator(String indicator) {
		currentIndicator = indicator;
	}

	public String getIndicator() {
		return currentIndicator;
	}

	public void setStartTime(long time) {
		startTime = time;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setDistance(float distance) {
		playerDistance = distance;
	}

	public float getDistance() {
		return playerDistance;
	}

	public void setPlayerPoints(int points) {
		playerPoints += points;
	}

	public int getPlayerPoints() {
		return playerPoints;
	}

	public void setStealer(String s) {
		stealer = s;
	}

	public String getStealer() {
		return stealer;
	}

	// Dizzy, SmokeBomb, Clear, Taunt
	public void activateTool(String tool) {
		if (!toolInEffect.equals("") && tool.equals("clearSky")) {
			isClearSky = true;
			toolInEffect = "";
			effectStartTime = 0;
		} else if (ServerTable.getDurationalTools().contains(tool)) {
			toolInEffect = tool;
			effectStartTime = System.currentTimeMillis();
			if (toolInEffect.equals("lock-out") || toolInEffect.equals("steal")) {
				effectDuration = 120;
			} else {
				effectDuration = 90;
			}
		}
	}

	// Deals with "dizzyMonkey", "smokeBomb", "steal", "lock-out", "compass"
	public String getCurrentEffect() {
		if (!toolInEffect.equals("")) {
			long duration = (System.currentTimeMillis() - effectStartTime) / 1000;
			if (duration > effectDuration) {
				toolInEffect = "";
				return toolInEffect;
			} else {
				return toolInEffect;
			}
		} else {
			return toolInEffect;
		}
	}

	// Reset currentEffect to nothing
	public void resetCurrentEffect() {
		toolInEffect = "";
	}

	public void setHotOnce() {
		HotOnce = true;
	}

	public boolean checkHotOnce() {
		return HotOnce;
	}

	public void setWarmOnce() {
		WarmOnce = true;
	}

	public boolean checkWarmOnce() {
		return WarmOnce;
	}

	public void setTaunt() {
		isTaunt = true;
	}

	public boolean checkTaunt() {
		return isTaunt;
	}

	public void resetTaunt() {
		isTaunt = false;
	}

	public boolean checkClearSky() {
		return isClearSky;
	}

	public void resetClearSky() {
		isClearSky = false;
	}

	public void setStolenWin() {
		stolenWin = true;
	}

	public boolean checkStolenWin() {
		return stolenWin;
	}

	public void resetStolenWin() {
		stolenWin = false;
	}
}
