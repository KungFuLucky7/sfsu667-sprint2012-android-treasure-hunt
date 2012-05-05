package sfsumapserver;


/**
 * A class for keeping track of all of a player's stats.
 *
 * @author Terry Wong
 */
public class PlayerStats {

    private String playerID = "", currentLocation = "", playerGoal = "", currentClue = "", toolInEffect = "", stealer = "";
    private int playerPoints = 500;
    private float playerDistance;
    private boolean HotOnce = false, WarmOnce = false, isTaunt = false, stolenWin = false;
    private long startTime = 0, effectStartTime = 0, effectDuration = 0;

    public PlayerStats(String ID) {
        playerID = ID;
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

    public void setStealer(String s) {
        stealer = s;
    }

    public String getStealer() {
        return stealer;
    }

    public void activateTool(String tool) {
        if (!toolInEffect.equals("") && tool.equals("clearSky")) {
            toolInEffect = "";
            effectStartTime = 0;
        } else if (ServerTable.getDurationalTools().contains(tool)) {
            toolInEffect = tool;
            effectStartTime = System.currentTimeMillis();
            if (toolInEffect.equals("lock-out") || toolInEffect.equals("steal")) {
                effectDuration = 120;
            } else {
                effectDuration = 60;
            }
        }
    }

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
}
