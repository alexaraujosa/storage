package com.sd56.client;

import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;

import com.sd56.common.connectors.Demultiplexer;
import com.sd56.common.connectors.TaggedConnection;
import com.sd56.common.datagram.*;
import com.sd56.common.datagram.Datagram.DatagramType;

public class Client {
    private final String HOST = "localhost";
    private final int PORT = 1337;

    private boolean authenticated;
    private Demultiplexer demultiplexer;

    private Thread[] workers;
    private ProtectedMessages messagesToSend;

    //TODO Acrescentar numero de workers
    public Client(int maxSessions) {
        try {
            this.authenticated = false;
            this.messagesToSend = new ProtectedMessages();
            this.workers = new Thread[maxSessions];
            this.demultiplexer = new Demultiplexer(new TaggedConnection(new Socket(HOST, PORT)));
            this.demultiplexer.start();
            System.out.println("Connection established.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Demultiplexer getDemultiplexer() {
        return this.demultiplexer;
    }

    public ProtectedMessages getMessagesToSend() {
        return this.messagesToSend;
    }

    public boolean isAuthenticated(){
        return this.authenticated;
    }

    public void setAuthenticated(boolean authenticated){
        this.authenticated = authenticated;
    }

    public void close() {
        try {
            Datagram closeDg = new Datagram(DatagramType.DATAGRAM_TYPE_REQUEST_CLOSE);

            byte[] message = closeDg.serialize();
            this.messagesToSend.setMessage(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void authenticate(String username, String password) {
        try {
            RequestAuthDatagram reqAuth = new RequestAuthDatagram(username, password);

            byte[] message = reqAuth.serialize();
            this.messagesToSend.setMessage(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void get(String key) {
        try {
            RequestGetDatagram reqGet = new RequestGetDatagram(key);

            byte[] message = reqGet.serialize();
            this.messagesToSend.setMessage(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void put(String key, String value) {
        try {
            RequestPutDatagram reqPut = new RequestPutDatagram(key, value.getBytes(StandardCharsets.UTF_8));

            byte[] message = reqPut.serialize();
            this.messagesToSend.setMessage(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void multiGet(Set<String> keys) {
        try {
            RequestMultiGetDatagram reqMultiGet = new RequestMultiGetDatagram(keys);

            byte[] message = reqMultiGet.serialize();
            this.messagesToSend.setMessage(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void multiPut(Map<String, byte[]> values) {
        try {
            RequestMultiPutDatagram reqMultiPut = new RequestMultiPutDatagram(values);

            byte[] message = reqMultiPut.serialize();
            this.messagesToSend.setMessage(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //todo: oioioio passar para o handler
    public void getWhen(String key, String keyCond, byte[] valueCond){
        try{
            //System.out.println("\nKEY: " + key + "\nKeyCond: " + keyCond + "\nValueCond: " + new String(valueCond, StandardCharsets.UTF_8));
            RequestGetWhenDatagram reqGetWhen = new RequestGetWhenDatagram(key, keyCond, valueCond);

            byte[] message = reqGetWhen.serialize();
            this.messagesToSend.setMessage(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        if(args.length != 1) {
            System.err.println("Missing arguments. Usage: java Client <maxThreads>");
            return;
        }

        Client client = new Client(Integer.parseInt(args[0]));

        for (int i = 0; i < client.workers.length; i++){
            client.workers[i] = new Thread(new ClientHandler(i, client));
            client.workers[i].start();
        }

        ClientUI menu = new ClientUI(client);

        menu.run();
    }
}
