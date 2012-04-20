package sfsumapserver;

import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.TimeZone;

/**
 *
 * @author Terry Wong
 */
public class Process {

    private final Socket client;
    private int index, totalPoints = 0;
    private String line, requestline, playerID, goal, currentLocation, goalLocation, option, tool, clue;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    private float distance;
    private long startTime, endTime, elapsedTime;
    private PlayerStats player;
    private boolean playerIDInUse = false, getTopThree = false;
    private String[] topThreeTeams = {"*****", "*****", "*****"};
    private String[] topThreeClues = {"****", "****", "****"};
    private float[] topThreeDistances = {1000, 1000, 1000};

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
     * @param inFile BufferedReader object that comes through the socket. Needs
     * to be processed properly before the data stored within it can be used.
     */
    public void readRequest() throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        requestline = in.readLine();
        if (requestline != null && requestline.trim().length() > 0) {
            StringTokenizer st = new StringTokenizer(requestline);
            if (st.hasMoreTokens()) {
                st.nextToken();
                playerID = st.nextToken();
                playerID = playerID.replaceAll("/", "").trim();
                System.out.println("playerID: " + playerID);
                currentLocation = st.nextToken();
                System.out.println("currentLocation: " + currentLocation);
                option = st.nextToken();
                System.out.println("option: " + option);
                if (st.hasMoreTokens()) {
                    tool = st.nextToken();
                    System.out.println("tool: " + tool);
                }
            }
        }
    }

    public void processRequest() {
        if (option.equalsIgnoreCase("signIn")) {
            if (!ServerTable.playerInfoContains(playerID)) {
                ServerTable.setPlayerInfo(playerID);
            } else {
                playerIDInUse = true;
            }
        } else if (option.equalsIgnoreCase("getClue")) {
            goal = ServerTable.getGoal();
            if (goal.equals("")) {
                goal = ServerTable.setNewGoal();
            }
            System.out.println("goal: " + goal);
            player = ServerTable.getPlayerInfo(playerID);
            if (player.getGoal().equals("")) {
                player.setGoal(goal);
                startTime = System.currentTimeMillis();
                player.setStartTime(startTime);
            }
            goalLocation = ServerTable.getGoalLocation();
            computeDistance();
            player.setDistance(distance);
            updateTopThree();
            setClue();
        } else if (option.equalsIgnoreCase("setTool")) {
            System.out.println("tool: " + tool);
            totalPoints -= ServerTable.getToolPrice(tool);
            player.setTool(tool);
            setClue();
        } else if (option.equalsIgnoreCase("getTopThree")) {
            getTopThree = true;
        }
    }

    private void computeDistance() {
        index = currentLocation.indexOf(",");
        float longitude1 = Float.valueOf(currentLocation.substring(0, index));
        float latitude1 = Float.valueOf(currentLocation.substring(index + 1));
        index = goalLocation.indexOf(",");
        float longitude2 = Float.valueOf(goalLocation.substring(0, index));
        float latitude2 = Float.valueOf(goalLocation.substring(index + 1));
        distance = (float) Math.sqrt(Math.pow(longitude1 - longitude2, 2) + Math.pow(latitude1 - latitude2, 2));
        System.out.println("distance:" + distance + " degrees");
    }

    private void updateTopThree() {
        int i = 2;
        float ld = distance, tmpd;
        String lid = playerID, tmpn, lc = clue, tmpc;
        while (i >= 0) {
            if (ld < topThreeDistances[i]) {
                tmpn = topThreeTeams[i];
                topThreeTeams[i] = lid;
                lid = tmpn;
                tmpc = topThreeClues[i];
                topThreeClues[i] = lc;
                lc = tmpc;
                tmpd = topThreeDistances[i];
                topThreeDistances[i] = ld;
                ld = tmpd;
            }
            i--;
        }
    }

    private void setClue() {
        if (!player.checkTool()) {
            clue = player.activateTool();
            totalPoints += ServerTable.getToolDamage(tool);
        } else if (player.checkEffect()) {
            if (distance <= 0.00005) {
                clue = "Win";
                totalPoints += 50;
                ServerTable.removeGoal();
                ServerTable.resetGame();
            } else if (distance <= 0.00050) {
                clue = "Hot";
                if (!player.checkHotOnce()) {
                    totalPoints += 20;
                    player.setHotOnce();
                }
            } else if (distance <= 0.00100) {
                clue = "Warm";
                if (!player.checkWarmOnce()) {
                    totalPoints += 20;
                    player.setWarmOnce();
                }
            } else {
                clue = "Cold";
            }
        } else {
            clue = player.getCurrentEffect();
        }
        if (clue.equals("smokeBomb")) {
            distance = 1000000;
        }
        if (clue.equals("drunkMonkey")) {
            distance = (float) 0.00200;
            clue = "Cold";
        }
        player.setClue(clue);
    }

    public void writeResponse() throws IOException {
        BufferedOutputStream out = new BufferedOutputStream(client.getOutputStream());
        PrintWriter writer = new PrintWriter(out, true);
        if (option.equalsIgnoreCase("signIn")) {
            if (!playerIDInUse) {
                writer.println(playerID + " Sign-In success");
            } else {
                writer.println(playerID + " Sign-In fail: ID already in use");
            }
        } else if (!getTopThree) {
            writer.print(playerID + " " + clue);
            endTime = System.currentTimeMillis();
            if (player.getStartTime() != 0) {
                elapsedTime = endTime - Long.valueOf(player.getStartTime());
            } else {
                elapsedTime = 0;
            }
            writer.print(" " + distance);
            writer.print(" " + goalLocation);
            writer.print(" " + dateFormat.format(new Date(elapsedTime)));
            player.setPlayerPoints(totalPoints);
            writer.println(" " + player.getPlayerPoints());
        } else {
            writer.println("1. " + topThreeTeams[0] + " " + topThreeClues[0] + " " + topThreeDistances[0]);
            writer.println("2. " + topThreeTeams[1] + " " + topThreeClues[1] + " " + topThreeDistances[1]);
            writer.println("3. " + topThreeTeams[2] + " " + topThreeClues[2] + " " + topThreeDistances[2]);
        }
    }

    private void writeToLog() {
        try {
            BufferedWriter writer1 = new BufferedWriter(new FileWriter(new File("scores rankings.log"), true));
            writer1.write(playerID + " ");
            writer1.write(player.getPlayerPoints());
            writer1.newLine();
            writer1.close();
            BufferedWriter writer2 = new BufferedWriter(new FileWriter(new File("time rankings.log"), true));
            writer2.write(playerID + " ");
            writer2.write(elapsedTime + " ");
            writer2.newLine();
            writer2.close();
        } catch (IOException e) {  // if there is an error in reading the file
            System.err.println(e.getMessage());
        }
    }
}
