package sfsumapserver;

import java.util.LinkedList;
import java.util.Queue;

/**
 * A class for keeping track of all of a player's stats.
 *
 * @author Terry Wong
 */
public class PlayerStats {

    private String playerID = "", currentLocation = "", playerGoal = "", currentClue = "", tool = "", toolInEffect = "";
    private int playerPoints = 0;
    private float playerDistance;
    private boolean HotOnce = false, WarmOnce = false;
    private long startTime = 0, effectStartTime = 0;
    private Queue<String> toolsStats;

    public PlayerStats(String ID) {
        playerID = ID;
        toolsStats = new LinkedList<String>();
    }

    public String getPlayerID() {
        return playerID;
    }

    public void setCurrentLocation(String location) {
        currentLocation = location;
    }

    public void setGoal(String goal) {
        playerGoal = goal;
    }

    public String getGoal() {
        return playerGoal;
    }

    public void setClue(String clue) {
        currentClue = clue;
    }

    public String getClue() {
        return currentClue;
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

    public void setTool(String tool) {
        toolsStats.offer(tool);
    }

    public String activateTool() {
        if ((toolInEffect.equals("smokeBomb") || toolInEffect.equals("drunkMonkey")) && toolsStats.peek().equals("clearSky")) {
            toolInEffect = "";
            effectStartTime = 0;
        } else if (toolsStats.peek().equals("smokeBomb") || toolsStats.peek().equals("drunkMonkey")) {
            toolInEffect = toolsStats.peek();
            effectStartTime = System.currentTimeMillis();
        }
        tool = toolsStats.poll();
        return tool;
    }

    public boolean checkTool() {
        return toolsStats.isEmpty();
    }

    public void endEffect() {
        toolInEffect = "";
    }

    public String getCurrentEffect() {
        return toolInEffect;
    }

    public boolean checkEffect() {
        if (!toolInEffect.equals("")) {
            long duration = (System.currentTimeMillis() - effectStartTime) / 1000;
            if (duration > 60) {
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
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
}
