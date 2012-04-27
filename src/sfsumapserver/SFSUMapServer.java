package sfsumapserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * This is the web server's main class for execution. It also contains a thread
 * class that applies support of the multi-threading functionality for the main
 * program.
 *
 * This is an enhanced version. <p>Copyright: Copyright (c) 2012</p>
 *
 * @author Terry Wong
 */
public class SFSUMapServer {
    //class variable to keep track of number of threads created

    public static int numThreadsCreated = 0;
    private static final int portNumber = 9225, maxThreads = 100;

    public SFSUMapServer() {
        ServerTable.init();
    }

    void runServer() {
        // TODO Auto-generated method stub
        ServerSocket server = null;
        Socket client = null;

        try {
            // Start the server with the pre-defined port number
            server = new ServerSocket(portNumber);
            System.out.println("Opened socket " + portNumber);
            while (true) {
                // keeps listening for new clients, one at a time 
                try {
                    client = server.accept(); // waits for client here 
                } catch (IOException e) {
                    System.err.println(e.getMessage());
                    System.err.println("Error opening socket");
                    System.exit(1);
                }
                if (numThreadsCreated <= maxThreads) {
                    new clientThread(client).start();
                } else {
                    System.err.println("The maximum number of threads has been reached.");
                }
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.err.println("Error starting server");
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        new SFSUMapServer().runServer();
    }
}

/**
 * Private class used for multithreading
 */
class clientThread extends Thread {

    private Socket client = null;
    private Process process;

    /**
     * Constructor used to start a thread.
     *
     * @param incoming Socket value which designates the socket used by the
     * client
     * @param hcf HttpdConf object created upon server startup.
     */
    public clientThread(Socket client) {
        this.client = client;
        incrementNumThreadsCreated();
    }

    /**
     * Used to run your server thread. Here is where you will be processing all
     * requests and returning your responses.
     */
    public void run() {
        // Connection is built, so read stream from the socket and parse request
        try {
            process = new Process(client);
            process.readRequest();
            process.processRequest();
            process.writeResponse();
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.err.println("Error processing request");
        }
        finally {
            try {
                this.client.close();
            	decrementNumThreadsCreated();
            }
            catch(IOException e) {
                System.out.println("Error closing socket");
            }
        }
    }

    public synchronized void incrementNumThreadsCreated() {
        SFSUMapServer.numThreadsCreated++;
    }

    public synchronized void decrementNumThreadsCreated() {
        SFSUMapServer.numThreadsCreated--;
    }
}
