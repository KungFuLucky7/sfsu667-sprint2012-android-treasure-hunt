package sfsumapserver;

import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.TimeZone;

import com.google.gson.*;


/**
 *
 * @author Terry Wong
 */
public class Process {

    private final Socket client;
    private int index, totalPoints = 0;
    private String line, requestline, playerID, goal, currentLocation, goalLocation, option,
            password = "", tool = "", targetPlayer = "", clue = "", currentEffect = "", ContentLength = "";
    private SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    private float distance;
    private long startTime, currentTime, elapsedTime;
    private PlayerStats player;
    private boolean playerIDInUse = false, authenticationFailure = false, getTopThree = false;
    private static String[] topThreeTeams = {"*****", "*****", "*****"};
    private static String[] topThreeClues = {"****", "****", "****"};
    private static float[] topThreeDistances = {1000, 1000, 1000};
    private AddNewPlayer newPlayer;
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
     * @param inFile BufferedReader object that comes through the socket. Needs
     * to be processed properly before the data stored within it can be used.
     */
    public void readRequest() throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        requestline = in.readLine();
        while(!(requestline.isEmpty())) {
        	// Debug
        	// System.out.println("requestline : "+requestline);
        	requestline = in.readLine();
        }
        requestline = in.readLine();

		JsonObject jsonReceived = new JsonParser().parse(requestline).getAsJsonObject();
        
		// Debug
        // System.out.println("json received : "+jsonReceived.toString());        
        // System.out.println("playerid : "+jsonReceived.getAsJsonPrimitive("playerID").getAsString());
        
        if(jsonReceived.has("playerID")) {
        	playerID = jsonReceived.getAsJsonPrimitive("playerID").getAsString();
        	// Debug
        	// System.out.println("playerID : "+playerID);
        }
        		
        if(jsonReceived.has("currentLocation")) {
        	currentLocation = jsonReceived.getAsJsonPrimitive("currentLocation").getAsString();
        	// Debug
        	// System.out.println("currentLocation : "+currentLocation);
        }
        
        if(jsonReceived.has("option")) {
        	option = jsonReceived.getAsJsonPrimitive("option").getAsString();
        	// Debug
        	// System.out.println("option : "+option);
        }
        
        if(jsonReceived.has("password")) {
        	password = jsonReceived.getAsJsonPrimitive("password").getAsString();
        	// Debug
        	// System.out.println("password : "+password);
        }
        
        if(jsonReceived.has("tool")) {
        	tool = jsonReceived.getAsJsonPrimitive("tool").getAsString();
        	// Debug
        	// System.out.println("tool : "+tool);
        }
        
        if(jsonReceived.has("targetPlayer")) {
        	targetPlayer = jsonReceived.getAsJsonPrimitive("targetPlayer").getAsString();
        	// Debug
        	// System.out.println("targetPlayer : "+targetPlayer);
        }
        else {
        	targetPlayer = playerID;
        	// Debug
        	// System.out.println("targetPlayer : "+targetPlayer);
        }
    }

    public synchronized void processRequest() throws IOException {
        if (option.equalsIgnoreCase("signUp")) {
            if (!ServerTable.playerInfoContains(playerID)) {
                newPlayer = new AddNewPlayer(playerID, password);
                newPlayer.add();
                ServerTable.setPlayerInfo(playerID);
            } else {
                playerIDInUse = true;
            }
        } else if (option.equalsIgnoreCase("signIn")) {
            authen = new Authentication(playerID, password);
            String ID = authen.checkAuth();
            if (ID == null || !ServerTable.playerInfoContains(playerID)) {
                authenticationFailure = true;
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
            updateTopThree();
            setClue();
        } else if (option.equalsIgnoreCase("setTool")) {
            totalPoints -= ServerTable.getToolPrice(tool);
            ServerTable.getPlayerInfo(targetPlayer).activateTool(tool);
            if (tool.equals("steal")) {
                ServerTable.getPlayerInfo(targetPlayer).setStealer(playerID);
            }
            ServerTable.getPlayerInfo(targetPlayer).setPlayerPoints(ServerTable.getToolDamage(tool));
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
        System.out.println("distance: " + distance + " degrees");
    }

    private long computeElapsedTime() {
        currentTime = System.currentTimeMillis();
        if (player.getStartTime() != 0) {
            elapsedTime = currentTime - Long.valueOf(player.getStartTime());
        } else {
            elapsedTime = 0;
        }
        return elapsedTime;
    }

    private synchronized void updateTopThree() {
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

    private synchronized void setClue() {
        if (player.checkStolenWin()) {
            clue = "Win";
            totalPoints += 50;
            if (elapsedTime <= 60000) {
                totalPoints += 50;
            }
            player.resetStolenWin();
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
                    ServerTable.getPlayerInfo(player.getStealer()).setStolenWin();
                } else if (currentEffect.equals("lock-out")) {
                    clue = "lock-out";
                } else {
                    clue = "Win";
                    totalPoints += 50;
                    if (elapsedTime <= 60000) {
                        totalPoints += 50;
                    }
                    ServerTable.removeGoal();
                    ServerTable.resetGame();
                }
            } else if (distance <= 0.00050) {
                clue = "Hot";
                if (currentEffect.equals("") && !player.checkHotOnce()) {
                    totalPoints += 20;
                    player.setHotOnce();
                }
            } else if (distance <= 0.00100) {
                clue = "Warm";
                if (currentEffect.equals("") && !player.checkWarmOnce()) {
                    totalPoints += 20;
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
    }

    public void writeResponse() throws IOException {
        BufferedOutputStream out = new BufferedOutputStream(client.getOutputStream());
        Date date = new Date();
        PrintWriter writer = new PrintWriter(out, true);
        writer.println("HTTP/1.1 200 OK");
        writer.println("Server: SFSUMapServer");
        writer.println("Date: " + dateFormat.format(date));
        writer.println("Content-Type: text/plain");
        writer.println("Connection: close");
        String output = "{";
        if (option.equalsIgnoreCase("signUp")) {
        	output += "\"signUp\":\"";
            if (!playerIDInUse) {
                output += "Good";
            } else {
                output += "Bad";
            }
            output += "\"";
        } else if (option.equalsIgnoreCase("signIn")) {
        	output += "\"signIn\":\"";
            if (!authenticationFailure) {
                output += "Good";
            } else {
                output += "Bad";
            }
            output += "\"";
        } else if (option.equalsIgnoreCase("getClue")) {
        	output += ",\"clue\":\""+ clue + "\"";
            output += ",\"distance\":\""+ distance + "\"";
            output += ",\"goalLocation\":\""+ goalLocation + "\"";
            
            //What is this for
            output += ",\"misc\":\""+ dateFormat.format(new Date(elapsedTime)) + "\"";
            
            output += ",\"playerPoints\":\""+ player.getPlayerPoints() + "\"";
        } else if (option.equalsIgnoreCase("setTool")) {
        	output += ",\"tool\":\""+ tool + "\"";
        	output += ",\"distance\":\""+ distance + "\"";
        	output += ",\"goalLocation\":\""+ goalLocation + "\"";

            //What is this for
        	output += ",\"misc\":\""+ dateFormat.format(new Date(elapsedTime)) + "\"";

            output += ",\"playerPoints\":\""+ player.getPlayerPoints() + "\"";
        } else if (getTopThree) {
        	
        	output += ",\"1.\":\""+ topThreeTeams[0] + "\"";
        	output += ",\"2.\":\""+ topThreeClues[0] + "\"";
        	output += ",\"3.\":\""+ topThreeDistances[0] + "\"";
        	
        	
            //output += "1. " + topThreeTeams[0] + " " + topThreeClues[0] + " " + topThreeDistances[0] + "\n";
            //output += "2. " + topThreeTeams[1] + " " + topThreeClues[1] + " " + topThreeDistances[1] + "\n";
            //output += "3. " + topThreeTeams[2] + " " + topThreeClues[2] + " " + topThreeDistances[2] + "\n";
        }
        output += "}";
        System.out.println("output"+output);
        writer.println("Content-Length: " + output.length());
        System.out.println("Content-Length: " + output.length());
        writer.println("");
        writer.println(output);
        System.out.println("\n" + output);
        writer.flush();
        writer.close();
        out.close();
    }

    private synchronized void writeToLog() {
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
