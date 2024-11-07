package com.sd56.server;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import com.sd56.common.datagram.Datagram;

public class Server {
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

        try (ServerSocket ss = new ServerSocket(1337)) {
            System.out.println("Server successfully created. Awaiting for connections...");

            while (true) {
                Socket socket = ss.accept();
                System.out.println("Client connection accepted.");

                DataInputStream in = new DataInputStream(socket.getInputStream());
                while (!socket.isInputShutdown()) {
                    Datagram datagram = Datagram.deserialize(in);
                    System.out.println("Received: " + datagram.toString());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
