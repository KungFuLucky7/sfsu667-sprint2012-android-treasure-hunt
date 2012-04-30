package sfsumapserver;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Multi-threaded server. Receives request and sends "200 response to client.
public class Listen {

    private ServerSocket server;
    private Socket client = null;
    private static int threadID = 0;
    private ExecutorService threadPoolExecutor;
    private final int portNumber = 9226;
    private final int maxThreads = 5;

    public void webServerRun() throws InterruptedException {
        //Open server socket.
        try {
            server = new ServerSocket(portNumber);
            System.out.println(dateStamp("[dd/MMM/yyyy:HH:mm:ss Z]") + ": Listening to port " + portNumber + ".");
        } catch (IOException ie) {
            System.out.println("Failed to open socket on port " + portNumber + ": " + ie);
            System.exit(-1);
        }

        //Set up thread pool.
        threadPoolExecutor = Executors.newFixedThreadPool(maxThreads);

        //Listen for client.
        while (true) {
            try {
                client = server.accept();
                threadPoolExecutor.execute(new RequestThread(client, threadID));

                //Cycles through thread pool identifiers.
                if (threadID == maxThreads) {
                    threadID = 0;
                } else {
                    threadID++;
                }
            } catch (IOException ie) {
                System.out.println("Thread " + threadID + " failed with client (" + client.getInetAddress());
                System.exit(-1);
            }
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        Listen listenServer = new Listen();
        listenServer.webServerRun();
    }

    private String dateStamp(String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        Date resultDate = new Date();
        return dateFormat.format(resultDate);
    }
}

class RequestThread implements Runnable {

    private Socket client;
    //private BufferedInputStreamReader requestStream;
    private BufferedOutputStream out;
    private int threadID;

    public RequestThread(Socket clientSocket, int tID) {
        this.client = clientSocket;
        this.threadID = tID;

        try {
            // requestStream = new BufferedInputStreamReader(new BufferedInputStream(client.getInputStream()));
            out = new BufferedOutputStream(client.getOutputStream());
        } catch (IOException ie) {
            System.out.println("Error in Request Thread(" + threadID + ": " + ie);
        }
    }

    //Process client request.
    public void run() {
        try {
            BufferedReader fromClient = new BufferedReader(new InputStreamReader(client.getInputStream()));

            //Reads the first line to get the request method, URI and HTTP version
            String line = fromClient.readLine();
            StringTokenizer firstline = new StringTokenizer(line);
            //ParseFirstLine(line);
            System.out.println(dateStamp("[dd/MMM/yyyy:HH:mm:ss Z]") + "(tID:" + threadID + ")" + "-> Receiving from Client (" + client.getInetAddress() + ")");
            System.out.println(dateStamp("[dd/MMM/yyyy:HH:mm:ss Z]") + "(tID:" + threadID + ")" + "-> Client sent: " + line);

            //Read Headers. Not important. Could just ignore rest of input. Only above first line is important.
            int x = 5;
            while (x-- > 0) {
                line = fromClient.readLine();
                System.out.println(dateStamp("[dd/MMM/yyyy:HH:mm:ss Z]") + "(tID:" + threadID + ")" + "-> " + line);
            }
            System.out.println(":::" + line);

            String requestCommand = firstline.nextToken();
            requestCommand = firstline.nextToken();
            String sendText = "This is a test string.\nthreadID: " + threadID;

            if (requestCommand.compareTo("/HEADERS") == 0) {
                System.out.println(dateStamp("[dd/MMM/yyyy:HH:mm:ss Z]") + "(tID:" + threadID + ")" + "-> --User Headers Requested.--");
                sendText = "<UserID>\n<GroupID>\n<Title>\n<TaskData>\n<Address>\n<Latitude>\n<Longitude>\n";
            } else if (requestCommand.compareTo("/UPDATE") == 0) {
                System.out.println(dateStamp("[dd/MMM/yyyy:HH:mm:ss Z]") + "(tID:" + threadID + ")" + "-> --User Update Requested.--");
                sendText = "01\n101\nBasic Server Test\nPull Data from server.\nSFSU\n37.724802\n-122.479277\n";
            }
            sendResponse(sendText);
        } catch (IOException ie) {
            System.out.println("IO failed: " + ie);
            System.exit(-1);
        }
    }

    //Send back to client any data. Currently on sends "200" message.
    private void sendResponse(String sendText) {
        try {
            BufferedOutputStream outputToClient = new BufferedOutputStream(client.getOutputStream());
            PrintWriter writer = new PrintWriter(outputToClient, true);

            System.out.println(dateStamp("[dd/MMM/yyyy:HH:mm:ss Z]") + "(tID:" + threadID + ")" + "-> Sending to Client ");
            System.out.println(dateStamp("[dd/MMM/yyyy:HH:mm:ss Z]") + "(tID:" + threadID + ")" + "-> HTTP/1.1 200 OK");
            System.out.println(dateStamp("[dd/MMM/yyyy:HH:mm:ss Z]") + "(tID:" + threadID + ")" + "-> Server: LISTEN");
            System.out.println(dateStamp("[dd/MMM/yyyy:HH:mm:ss Z]") + "(tID:" + threadID + ")" + "-> Date: " + dateStamp("EEE, d MMM yyyy HH:mm:ss z"));
            System.out.println(dateStamp("[dd/MMM/yyyy:HH:mm:ss Z]") + "(tID:" + threadID + ")" + "-> Content-Type: text/plain");
            System.out.println(dateStamp("[dd/MMM/yyyy:HH:mm:ss Z]") + "(tID:" + threadID + ")" + "-> Connection: close");
            System.out.println(dateStamp("[dd/MMM/yyyy:HH:mm:ss Z]") + "(tID:" + threadID + ")" + "-> Content-Length: " + sendText.length());
            System.out.println(dateStamp("[dd/MMM/yyyy:HH:mm:ss Z]") + "(tID:" + threadID + ")" + "-> Age: 0");

            writer.println("HTTP/1.1 200 OK");
            writer.println("Server: LISTEN");
            writer.println("Date: " + dateStamp("EEE, d MMM yyyy HH:mm:ss z"));
            writer.println("Content-Type: text/plain");
            writer.println("Connection: close");
            writer.println("Content-Length: " + sendText.length());
            writer.println("Age: 0");
            writer.println("");
            writer.println(sendText + "\n\r");

            writer.flush();
            writer.close();

            System.out.println(dateStamp("[dd/MMM/yyyy:HH:mm:ss Z]") + "(tID:" + threadID + ")" + "-> Closed connection with " + client.getInetAddress());

            out.close();
            client.close();
        } catch (IOException ex) {
            System.out.println("Access Failed (" + client.getInetAddress() + ": " + ex.getMessage());
        }
    }

    private String dateStamp(String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        Date resultDate = new Date();
        return dateFormat.format(resultDate);
    }
}