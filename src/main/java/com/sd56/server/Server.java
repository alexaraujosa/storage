package com.sd56.server;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

import com.sd56.common.datagram.Datagram;

class DatabaseManager {
    private HashMap<String, byte[]> db = new HashMap<>();

    public void update(String key) {

    }
}

class ClientHandler implements Runnable {
    private Socket socket;
    private DatabaseManager dbManager;

    public ClientHandler(Socket socket, DatabaseManager dbManager) {
        this.socket = socket;
        this.dbManager = dbManager;
    }

    @Override
    public void run() {
        System.out.println("Hello! Me am a Thread");

        try{
            DataInputStream in = new DataInputStream(socket.getInputStream());
            while (!socket.isInputShutdown()) {
                Datagram datagram = Datagram.deserialize(in);
                System.out.println("Received: " + datagram.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


public class Server {
    private final static int PORT = 1337;
    private final int maxSessions;

    public Server(int maxSessions) {
        this.maxSessions = maxSessions;
    }

    public static void main(String[] args) throws IOException {
        if(args.length != 1) {
            System.err.println("Missing arguments. Usage: java Server <maxSessions>");
            return;
        }
        Server server = new Server(Integer.parseInt(args[0]));

        try (ServerSocket ss = new ServerSocket(PORT)) {
            System.out.println("Server successfully created. Awaiting for connections...");

            while (true) {
                Socket socket = ss.accept();
                System.out.println("Client connection accepted.");

                Thread thread = new Thread(new ClientHandler(socket,new DatabaseManager()));
                thread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
