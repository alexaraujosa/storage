package com.sd56.server.handler;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.util.AbstractMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.sd56.common.connectors.Demultiplexer;
import com.sd56.common.connectors.TaggedConnection;
import com.sd56.common.datagram.Datagram;
import com.sd56.common.datagram.RequestAuthDatagram;
import com.sd56.common.datagram.RequestGetDatagram;
import com.sd56.common.datagram.RequestGetWhenDatagram;
import com.sd56.common.datagram.RequestMultiGetDatagram;
import com.sd56.common.datagram.RequestMultiPutDatagram;
import com.sd56.common.datagram.RequestPutDatagram;
import com.sd56.common.datagram.ResponseAuthDatagram;
import com.sd56.common.util.LockedResource;
import com.sd56.server.DatabaseManager;

public class SessionHandler implements Runnable {
    private final AtomicInteger currentSessions;
    private final DatabaseManager dbManager;
    private final LockedResource<LinkedList<Map.Entry<TaggedConnection, TaggedConnection.Frame>>> requests;
    private final LockedResource<LinkedList<Socket>> clients;

    public SessionHandler(
        AtomicInteger currentSessions,
        DatabaseManager dbManager,
        LockedResource<LinkedList<Map.Entry<TaggedConnection, TaggedConnection.Frame>>> requests,
        LockedResource<LinkedList<Socket>> clients
    ) {
        this.currentSessions = currentSessions;
        this.dbManager = dbManager;
        this.requests = requests;
        this.clients = clients;
    }

    private Socket getSocket() {
        this.clients.lock();
        try{
            this.currentSessions.getAndDecrement();
            while (this.clients.getResource().isEmpty()) {
                this.clients.await();
            }
            System.out.println("Removed a client from the waiting list!");
            this.currentSessions.getAndIncrement();
            return this.clients.getResource().poll();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            this.clients.unlock();
        }
    }

    @Override
    public void run() {
        while(true){
            Socket socket = this.getSocket();

            System.out.println("[SERVER] - New client connection established.");

            // System.out.println("[SERVER/DEBUG] - USERS");
            // for (Entry<String, String> entry : this.dbManager.getUsers().entrySet())
            //     System.out.println("[SERVER/DEBUG] " + entry.getKey() + " : " + entry.getValue());

            // System.out.println("[SERVER/DEBUG] - DATABASE");
            // for (Entry<String, byte[]> entry : this.dbManager.getDb().entrySet())
            //     System.out.println("[SERVER/DEBUG] " + entry.getKey() + " : " + new String(entry.getValue()));

            try{
                TaggedConnection connection = new TaggedConnection(socket);
                String user = "";
                while (!socket.isInputShutdown()) {
                    TaggedConnection.Frame frame = connection.receive();
                    int threadID = frame.tag;
                    byte[] recivedBytes = frame.data;

                    Datagram datagram = Datagram.deserialize(recivedBytes);

                    switch (datagram.getType()) {
                        case DATAGRAM_TYPE_REQUEST_AUTHENTICATION:
                            RequestAuthDatagram reqAuth = RequestAuthDatagram.deserialize(recivedBytes);
                            System.out.println(reqAuth);

                            Boolean authValidation = this.dbManager.authentication(reqAuth.getUsername(), reqAuth.getPassword());
                            user = reqAuth.getUsername();
                            ResponseAuthDatagram resAuth = new ResponseAuthDatagram(authValidation);
                            connection.send(threadID,resAuth.serialize());
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

                                Boolean authValidation = this.dbManager.authentication(reqAuth.getUsername(), reqAuth.getPassword());
                                if (authValidation) server.setCurrentSessions(server.getCurrentSessions() + 1);
                                ResponseAuthDatagram resAuth = new ResponseAuthDatagram(authValidation);
                                resAuth.serialize(out);
                            } finally {
                                l.unlock();
                            }
                            */
                            break;
                        case DATAGRAM_TYPE_REQUEST_PUT:
                            this.requests.lock();
                            try {
                                this.requests.getResource().add(new AbstractMap.SimpleEntry<>(connection, frame));
                                this.requests.signalAll();
                            } finally {
                                this.requests.unlock();
                            }
                            break;
                        case DATAGRAM_TYPE_REQUEST_GET:
                            this.requests.lock();
                            try {
                                this.requests.getResource().add(new AbstractMap.SimpleEntry<>(connection, frame));
                                this.requests.signalAll();
                            } finally {
                                this.requests.unlock();
                            }
                            break;
                        case DATAGRAM_TYPE_REQUEST_MULTIGET:
                            this.requests.lock();
                            try {
                                this.requests.getResource().add(new AbstractMap.SimpleEntry<>(connection, frame));
                                this.requests.signalAll();
                            } finally {
                                this.requests.unlock();
                            }
                            break;
                        case DATAGRAM_TYPE_REQUEST_MULTIPUT:
                            this.requests.lock();
                            try {
                                this.requests.getResource().add(new AbstractMap.SimpleEntry<>(connection, frame));
                                this.requests.signalAll();
                            } finally {
                                this.requests.unlock();
                            }
                            break;
                        case DATAGRAM_TYPE_REQUEST_GETWHEN:
                            this.requests.lock();
                            try {
                                this.requests.getResource().add(new AbstractMap.SimpleEntry<>(connection, frame));
                                this.requests.signalAll();
                            } finally {
                                this.requests.unlock();
                            }
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