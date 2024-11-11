package com.sd56.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

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

                Thread thread = new Thread(new Handler(socket,new DatabaseManager()));
                thread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
