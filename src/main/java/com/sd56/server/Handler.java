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
    private final Server server;

    public Handler(Server server) {
        this.server = server;
    }

    private Socket getSocket(){
        try{
            this.server.getLock().lock();
            this.server.setCurrentSessions(this.server.getCurrentSessions()-1);
            while (this.server.getClients().isEmpty()){
                this.server.getNotEmptyCond().await();
            }
            System.out.println("Removed a client from the waiting list!");
            this.server.setCurrentSessions(this.server.getCurrentSessions()+1);
            return this.server.getClients().poll();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            this.server.getLock().unlock();
        }
    }

    @Override
    public void run() {
        while(true){
            Socket socket = this.getSocket();

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
                String user = "";
                while (!socket.isInputShutdown()) {
                    Datagram datagram = Datagram.deserialize(in);

                    switch (datagram.getType()) {
                        case DATAGRAM_TYPE_REQUEST_AUTHENTICATION:
                            RequestAuthDatagram reqAuth = RequestAuthDatagram.deserialize(in, datagram);

                            Boolean authValidation = server.getDbManager().authentication(reqAuth.getUsername(), reqAuth.getPassword());
                            user = reqAuth.getUsername();
                            ResponseAuthDatagram resAuth = new ResponseAuthDatagram(authValidation);
                            resAuth.serialize(out);
                            /* A ideia aqui é verificar os valores na DB... como getDB.Manager().Authentication tem um lock
                             * ela é atomica e locks a este nivel não devem ser necessários.
                            l.lock();
                            try {
                                if (server.getCurrentSessions() >= server.getMaxSessions()) {
                                    server.getAwaitingSessions().add(this);
                                    System.out.println("[SERVER/DEBUG] FULL SESSIONS.");
                                    System.out.println("[SERVER/DEBUG] " + server.getAwaitingSessions().toString());
                                    while (server.getCurrentSessions() >= server.getMaxSessions()) {
                                        c.await();
                                    }
                                }

                                Boolean authValidation = server.getDbManager().authentication(reqAuth.getUsername(), reqAuth.getPassword());
                                if (authValidation) server.setCurrentSessions(server.getCurrentSessions() + 1);
                                ResponseAuthDatagram resAuth = new ResponseAuthDatagram(authValidation);
                                resAuth.serialize(out);
                            } finally {
                                l.unlock();
                            }
                            */
                            break;
                        case DATAGRAM_TYPE_REQUEST_PUT:
                            RequestPutDatagram reqPut = RequestPutDatagram.deserialize(in,datagram);
                            String putKey = reqPut.getKey();
                            byte[] putValue = reqPut.getValue();
                            server.getDbManager().put(putKey, putValue);
                            boolean putValidation = server.getDbManager().getDb().containsKey(putKey) && Arrays.equals(server.getDbManager().getDb().get(putKey), putValue);
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
                            for (Entry<String, byte[]> entry : multiPutValues.entrySet()) {
                                server.getDbManager().put(entry.getKey(), entry.getValue());
                                boolean multiPutValidation = server.getDbManager().getDb().containsKey(entry.getKey()) &&
                                        Arrays.equals(server.getDbManager().getDb().get(entry.getKey()), entry.getValue());
                                multiPutValidations.put(entry.getKey(), multiPutValidation);
                            }

                            ResponseMultiPutDatagram resMultiPut = new ResponseMultiPutDatagram(multiPutValidations);
                            resMultiPut.serialize(out);
                            break;
                        case DATAGRAM_TYPE_REQUEST_CLOSE:
                            System.out.println(user + " disconnected!");
                            /*
                            Handler first = server.getAwaitingSessions().poll();
                            if (first != null) {
                                first.l.lock();
                                try {
                                    first.c.signal();
                                } finally {
                                    first.l.unlock();
                                }
                            }
                            server.setCurrentSessions(server.getCurrentSessions() - 1);
                             */
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
}