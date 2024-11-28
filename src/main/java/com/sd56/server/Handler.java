package com.sd56.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import com.sd56.common.datagram.Datagram;
import com.sd56.common.datagram.RequestAuthDatagram;
import com.sd56.common.datagram.RequestGetDatagram;
import com.sd56.common.datagram.RequestMultiGetDatagram;
import com.sd56.common.datagram.RequestMultiPutDatagram;
import com.sd56.common.datagram.RequestPutDatagram;
import com.sd56.common.datagram.ResponseAuthDatagram;
import com.sd56.common.datagram.ResponseGetDatagram;
import com.sd56.common.datagram.ResponseMultiGetDatagram;
import com.sd56.common.datagram.ResponseMultiPutDatagram;
import com.sd56.common.datagram.ResponsePutDatagram;

public class Handler implements Runnable {
    private final Socket socket;
    private final Server server;
    private final ReentrantLock l;
    private final Condition c;

    public Handler(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
        this.l = new ReentrantLock();
        this.c = l.newCondition();
    }

    @Override
    public void run() {
        System.out.println("[SERVER] - New client connection established.");

        System.out.println("[SERVER/DEBUG] - USERS");
        for (Entry<String, String> entry : server.getDbManager().getUsers().entrySet())
            System.out.println("[SERVER/DEBUG] " + entry.getKey() + " : " + entry.getValue());

        System.out.println("[SERVER/DEBUG] - DATABASE");
        for (Entry<String, byte[]> entry : server.getDbManager().getDb().entrySet())
            System.out.println("[SERVER/DEBUG] " + entry.getKey() + " : " + new String(entry.getValue()));

        try{
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            while (!socket.isInputShutdown()) {
                Datagram datagram = Datagram.deserialize(in);
            
                switch (datagram.getType()) {
                    case DATAGRAM_TYPE_REQUEST_AUTHENTICATION:
                        RequestAuthDatagram reqAuth = RequestAuthDatagram.deserialize(in, datagram);
                        l.lock();
                        try {
                            if (server.getCurrentSessions() >= server.getMaxSessions()) {
                                server.getAwaitingSessions().add(this);
                                System.out.println("[SERVER/DEBUG] FULL SESSIONS.");
                                System.out.println("[SERVER/DEBUG] " + server.getAwaitingSessions().toString());
                                while (server.getCurrentSessions() >= server.getMaxSessions())
                                    c.await();
                            } 

                            Boolean authValidation = server.getDbManager().authentication(reqAuth.getUsername(), reqAuth.getPassword());
                            if (authValidation) server.setCurrentSessions(server.getCurrentSessions() + 1);
                            ResponseAuthDatagram resAuth = new ResponseAuthDatagram(authValidation);
                            resAuth.serialize(out);
                        } finally {
                            l.unlock();
                        }

                        break;
                    case DATAGRAM_TYPE_REQUEST_PUT:
                        RequestPutDatagram reqPut = RequestPutDatagram.deserialize(in,datagram);
                        String putKey = reqPut.getKey();
                        byte[] putValue = reqPut.getValue();
                        server.getDbManager().put(putKey, putValue);
                        Boolean putValidation = false;
                        if(server.getDbManager().getDb().containsKey(putKey) && Arrays.equals(server.getDbManager().getDb().get(putKey),putValue))
                            putValidation = true;
                        ResponsePutDatagram resPut = new ResponsePutDatagram(putValidation);
                        resPut.serialize(out);
                        break;
                    case DATAGRAM_TYPE_REQUEST_GET:
                        RequestGetDatagram reqGet = RequestGetDatagram.deserialize(in,datagram);
                        String getKey = reqGet.getKey();
                        byte[] getValue = server.getDbManager().get(getKey);
                        ResponseGetDatagram resGet = new ResponseGetDatagram(getValue);
                        resGet.serialize(out);
                        break;
                    case DATAGRAM_TYPE_REQUEST_MULTIGET:
                        RequestMultiGetDatagram reqMultiGet = RequestMultiGetDatagram.deserialize(in, datagram);
                        Set<String> multiGetKeys = reqMultiGet.getKeys();
                        Map<String, byte[]> multiGetValues = new HashMap<>();
                        for (String key : multiGetKeys) {
                            multiGetValues.put(key, server.getDbManager().getDb().get(key));
                        }
                        ResponseMultiGetDatagram resMultiGet = new ResponseMultiGetDatagram(multiGetValues);
                        resMultiGet.serialize(out);
                        break;
                    case DATAGRAM_TYPE_REQUEST_MULTIPUT:
                        RequestMultiPutDatagram reqMultiPut = RequestMultiPutDatagram.deserialize(in, datagram);
                        Map<String, byte[]> multiPutValues = reqMultiPut.getValues();
                        Map<String, Boolean> multiPutValidations = new HashMap<>();
                        for (Map.Entry<String, byte[]> entry : multiPutValues.entrySet()) {
                            server.getDbManager().put(entry.getKey(), entry.getValue());
                            Boolean multiPutValidation = false;
                            if (
                                server.getDbManager().getDb().containsKey(entry.getKey()) && 
                                Arrays.equals(server.getDbManager().getDb().get(entry.getKey()), entry.getValue())
                            ) {
                                multiPutValidation = true;
                            }
                            multiPutValidations.put(entry.getKey(), multiPutValidation);
                        }

                        ResponseMultiPutDatagram resMultiPut = new ResponseMultiPutDatagram(multiPutValidations);
                        resMultiPut.serialize(out);
                        break;
                    case DATAGRAM_TYPE_REQUEST_CLOSE:
                        server.setCurrentSessions(server.getCurrentSessions() - 1);
                        Handler first = server.getAwaitingSessions().poll();
                        if (first != null) {
                            first.l.lock();
                            try {
                                first.c.signal();
                            } finally {
                                first.l.unlock();
                            }
                        }
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
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}