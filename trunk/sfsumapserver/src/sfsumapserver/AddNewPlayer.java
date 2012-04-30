package sfsumapserver;

import java.io.*;

/**
 * Add new username/password pair to the user.txt file.
 *
 * @author Terry Wong
 */
public class AddNewPlayer {

    private static String authenticationString, line;

    public AddNewPlayer(String ID, String pw) {
        authenticationString = ID + ":" + pw;
    }

    public void add() {
        try {
            File file = new File("htpasswd.log");
            if (!file.exists()) {
                file.createNewFile();
            }
            BufferedReader reader = new BufferedReader(new FileReader(file));
            line = reader.readLine();
            if (line == null || !line.contains(authenticationString)) {
                BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));
                bw.write(authenticationString + " ");
                bw.flush();
                bw.close();
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}
