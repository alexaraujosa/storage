package com.sd56.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

import com.sd56.common.datagram.Datagram;
import com.sd56.common.datagram.RequestAuthDatagram;
import com.sd56.common.datagram.ResponseAuthDatagram;

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
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            while (!socket.isInputShutdown()) {
                Datagram datagram = Datagram.deserialize(in);
            
                switch (datagram.getType()) {
                    case DATAGRAM_TYPE_REQUEST_AUTHENTICATION: 
                        RequestAuthDatagram reqAuth = RequestAuthDatagram.deserialize(in, datagram);
                        //TODO: Boolean validation = DatabaseManager -> verify credencials
                        Boolean validation = true; // TEMPORARY PURPOSES
                        ResponseAuthDatagram resAuth = new ResponseAuthDatagram(validation);
                        resAuth.serialize(out);
                        break;
                    case DATAGRAM_TYPE_REQUEST_PUT:
                        //TODO
                        break;
                    case DATAGRAM_TYPE_REQUEST_GET:
                        //TODO
                        break;
                    default:
                        // Ignore it ig?
                        break;
                }
            }
        } catch (EOFException e) {
            System.out.println("A conexão foi encerrada, já não há mais nada a ler!");;
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
