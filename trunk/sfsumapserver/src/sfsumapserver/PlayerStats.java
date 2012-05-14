package sfsumapserver;

/**
 * A class for keeping track of all of a player's stats.
 * 
 * @author Terry Wong
 */
public class PlayerStats {

	private String playerID = "", playerGoal = "", currentIndicator = "", clue = "";
	
	private String toolInEffect = "";
	
	private int playerPoints = 0;
	private float playerDistance;
	
	private boolean HotOnce = false, WarmOnce = false, isTaunt = false, stolenWin = false;
	
	private long startTime = 0, effectStartTime = 0, effectDuration = 2;

	// private currentLocation = "";  -For future use.
	
	
/*
	public boolean activateTool(String tool) {
		if (!(toolInEffect.equals("")) && tool.equals("clearSky")) {
			toolInEffect = "";
			effectStartTime = 0;
		} else if (toolInEffect.equals("")) {
			toolInEffect = tool;
			effectStartTime = System.currentTimeMillis();
		} else return false;
		return true;
	}
*/
	
	// Dizzy, SmokeBomb, Clear, Taunt
	public boolean activateTool(String tool, String message) {
		if (!(toolInEffect.equals("")) && tool.equals("clearSky")) {
			toolInEffect = "";
			effectStartTime = 0;
		} else if (toolInEffect.equals("")) {
			toolInEffect = tool;
			effectStartTime = System.currentTimeMillis();
			if(message != null) {
				clue = message;
			}
		} else return false;
		return true;
	}
	
	// Add check for stealer and lockout
	// Deals with Dizzy, SmokeBomb, and Taunt being set
	public String getCurrentEffect() {
		if (!(toolInEffect.equals(""))) {
			long duration = (System.currentTimeMillis() - effectStartTime) / 1000;
			System.out.println("Duration :"+duration);
			System.out.println("effectDuration :"+effectDuration);
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
	
	
	public PlayerStats(String ID) {
		playerID = ID;
	}

	public String getPlayerID() {
		return playerID;
	}

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

	
	
	public void setStolenWin() {
		stolenWin = true;
	}

	public boolean checkStolenWin() {
		return stolenWin;
	}

	public void resetStolenWin() {
		stolenWin = false;
	}
	
	
/* For future use in keeping track of all current location of players
	public void setCurrentLocation(String location) {
		currentLocation = location;
	}

	public String getCurrentLocation() {
		return currentLocation;
	}
	
*/	
	
}