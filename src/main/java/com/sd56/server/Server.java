package com.sd56.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;

import com.sd56.common.datagram.*;

class DatabaseManager {
    private HashMap<String, String> users;
    private HashMap<String, byte[]> db;

    public DatabaseManager(){
        this.users = new HashMap<>();
        this.db = new HashMap<>();
    }

    public void setUsers(HashMap<String, String> users){
        this.users = users;
    }

    public HashMap<String, String> getUsers() {
        return this.users;
    }

    /*
    // caso queiramos o registo separado do login
    public void addUser(String username , String password){
        if(!this.users.containsKey(username)){
            this.users.put(username,password);
        } else {
            // throw new Exception(User already exists);
        }
    }
     */

    public boolean login(String username, String password){
        boolean login = false;
        if(!this.users.containsKey(username)){
            // caso em que o user ainda nao fez registo
            this.users.put(username,password);
            login = true;
        } else {
            // caso em que o user ja fez registo

            if(this.users.get(username).equals(password)){
            // password correta
                login = true;
            }
        }
        return login;
    }


    public void setDb(HashMap<String, byte[]> db) {
        this.db = db;
    }

    public HashMap<String, byte[]> getDb() {
        return this.db;
    }

    public void put(String key, byte[] value){
        this.db.put(key,value);
        // mesmo que a chave ja exista, o valor e atualizado automaticamente
    }

    public byte[] get(String key){
        return this.db.get(key);
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
                        Boolean authValidation = dbManager.login(reqAuth.getUsername(), reqAuth.getPassword());
                        ResponseAuthDatagram resAuth = new ResponseAuthDatagram(authValidation);
                        resAuth.serialize(out);
                        break;
                    case DATAGRAM_TYPE_REQUEST_PUT:
                        RequestPutDatagram reqPut = RequestPutDatagram.deserialize(in,datagram);
                        String key = reqPut.getKey();
                        byte[] value = reqPut.getValue();
                        dbManager.put(key, value);
                        Boolean putValidation = false;
                        if(dbManager.getDb().containsKey(key) && Arrays.equals(dbManager.getDb().get(key),value))
                            putValidation = true;
                        ResponsePutDatagram resPut = new ResponsePutDatagram(putValidation);
                        resPut.serialize(out);
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
