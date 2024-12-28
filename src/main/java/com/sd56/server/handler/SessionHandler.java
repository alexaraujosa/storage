package com.sd56.server.handler;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
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
import com.sd56.common.datagram.ResponseGetDatagram;
import com.sd56.common.datagram.ResponseGetWhenDatagram;
import com.sd56.common.datagram.ResponseGetWhenDatagram;
import com.sd56.common.datagram.ResponseMultiGetDatagram;
import com.sd56.common.datagram.ResponseMultiPutDatagram;
import com.sd56.common.datagram.ResponsePutDatagram;
import com.sd56.common.util.LockedResource;
import com.sd56.server.DatabaseManager;

public class SessionHandler implements Runnable {
    private final AtomicInteger currentSessions;
    private final DatabaseManager dbManager;
    private final LockedResource<LinkedList<Map.Entry<LockedResource<LinkedList<Map.Entry<TaggedConnection.Frame, Datagram>>, ?>, TaggedConnection.Frame>>, ?> requests;
    private final LockedResource<LinkedList<Socket>, ?> clients;

    public SessionHandler(
        AtomicInteger currentSessions,
        DatabaseManager dbManager,
        LockedResource<LinkedList<Map.Entry<LockedResource<LinkedList<Map.Entry<TaggedConnection.Frame, Datagram>>, ?>, TaggedConnection.Frame>>, ?> requests,
        LockedResource<LinkedList<Socket>, ?> clients
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
            try{
                Socket socket = this.getSocket();
                socket.setSoTimeout(250);

                LockedResource<LinkedList<Map.Entry<TaggedConnection.Frame, Datagram>>, ?> responses = new LockedResource<>(new LinkedList<>());
                String user = "";
                
                System.out.println("[SERVER] - New client connection established.");
                
                // System.out.println("[SERVER/DEBUG] - USERS");
                // for (Map.Entry<String, Map.Entry<String, Boolean>> entry : this.dbManager.getUsers().entrySet()) {
                //     System.out.println(entry.getKey() + " : " + entry.getValue().getKey() + " : " + entry.getValue().getValue());
                // }
                // for (Entry<String, String> entry : this.dbManager.getUsers().entrySet())
                // System.out.println("[SERVER/DEBUG] " + entry.getKey() + " : " + entry.getValue());
                
                // System.out.println("[SERVER/DEBUG] - DATABASE");
                // for (Entry<String, byte[]> entry : this.dbManager.getDb().entrySet())
                //     System.out.println("[SERVER/DEBUG] " + entry.getKey() + " : " + new String(entry.getValue()));
                try{
                    TaggedConnection connection = new TaggedConnection(socket);
                    while (!socket.isInputShutdown()) {
                        try {
                            TaggedConnection.Frame frame = connection.receive();
                            int threadID = frame.tag;
                            byte[] recivedBytes = frame.data;
                            
                            Datagram datagram = Datagram.deserialize(recivedBytes);
                            
                            switch (datagram.getType()) {
                                case DATAGRAM_TYPE_REQUEST_AUTHENTICATION:
                                    RequestAuthDatagram reqAuth = RequestAuthDatagram.deserialize(recivedBytes);
                                    System.out.println(reqAuth);
                                    
                                    Boolean authValidation = this.dbManager.authentication(reqAuth.getUsername(), reqAuth.getPassword());
                                    if (authValidation) user = reqAuth.getUsername();
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
                                        this.requests.getResource().add(new AbstractMap.SimpleEntry<>(responses, frame));
                                        this.requests.signalAll();
                                    } finally {
                                        this.requests.unlock();
                                    }
                                    break;
                                case DATAGRAM_TYPE_REQUEST_GET:
                                    this.requests.lock();
                                    try {
                                        this.requests.getResource().add(new AbstractMap.SimpleEntry<>(responses, frame));
                                        this.requests.signalAll();
                                    } finally {
                                        this.requests.unlock();
                                    }
                                    break;
                                case DATAGRAM_TYPE_REQUEST_MULTIGET:
                                    this.requests.lock();
                                    try {
                                        this.requests.getResource().add(new AbstractMap.SimpleEntry<>(responses, frame));
                                        this.requests.signalAll();
                                    } finally {
                                        this.requests.unlock();
                                    }
                                    break;
                                case DATAGRAM_TYPE_REQUEST_MULTIPUT:
                                    this.requests.lock();
                                    try {
                                        this.requests.getResource().add(new AbstractMap.SimpleEntry<>(responses, frame));
                                        this.requests.signalAll();
                                    } finally {
                                        this.requests.unlock();
                                    }
                                    break;
                                case DATAGRAM_TYPE_REQUEST_GETWHEN:
                                    this.requests.lock();
                                    try {
                                        this.requests.getResource().add(new AbstractMap.SimpleEntry<>(responses, frame));
                                        this.requests.signalAll();
                                    } finally {
                                        this.requests.unlock();
                                    }
                                    break;
                                case DATAGRAM_TYPE_REQUEST_CLOSE:
                                    this.dbManager.disconnect(user);
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

                            responses.lock();
                            try {
                                while (!responses.getResource().isEmpty()) {
                                    Map.Entry<TaggedConnection.Frame, Datagram> entrada = responses.getResource().poll();
                                    int responseTag = entrada.getKey().tag;
                                    Datagram responseDatagram = entrada.getValue();
                                    switch (responseDatagram.getType()) {
                                        case DATAGRAM_TYPE_RESPONSE_GET:
                                            ResponseGetDatagram responseGetDg = (ResponseGetDatagram) responseDatagram;
                                            connection.send(responseTag, responseGetDg.serialize());
                                            break;
                                        case DATAGRAM_TYPE_RESPONSE_PUT:
                                            ResponsePutDatagram responsePutDg = (ResponsePutDatagram) responseDatagram;
                                            connection.send(responseTag, responsePutDg.serialize());
                                            break;
                                        case DATAGRAM_TYPE_RESPONSE_MULTIGET:
                                            ResponseMultiGetDatagram responseMultiGetDg = (ResponseMultiGetDatagram) responseDatagram;
                                            connection.send(responseTag, responseMultiGetDg.serialize());
                                            break;
                                        case DATAGRAM_TYPE_RESPONSE_MULTIPUT:
                                            ResponseMultiPutDatagram responseMultiPutDg = (ResponseMultiPutDatagram) responseDatagram;
                                            connection.send(responseTag, responseMultiPutDg.serialize());
                                            break;
                                        case DATAGRAM_TYPE_RESPONSE_GETWHEN:
                                            ResponseGetWhenDatagram responseGetWhenDg = (ResponseGetWhenDatagram) responseDatagram;
                                            connection.send(responseTag, responseGetWhenDg.serialize());
                                            break;
                                        default:
                                            break;
                                    }
                                }
                            } finally {
                                responses.unlock();
                            }
                        } catch (SocketTimeoutException e) {
                            // System.out.println("Acabou o tempo");
                            responses.lock();
                            try {
                                while (!responses.getResource().isEmpty()) {
                                    Map.Entry<TaggedConnection.Frame, Datagram> entrada = responses.getResource().poll();
                                    int responseTag = entrada.getKey().tag;
                                    Datagram responseDatagram = entrada.getValue();
                                    switch (responseDatagram.getType()) {
                                        case DATAGRAM_TYPE_RESPONSE_GET:
                                            ResponseGetDatagram responseGetDg = (ResponseGetDatagram) responseDatagram;
                                            connection.send(responseTag, responseGetDg.serialize());
                                            break;
                                        case DATAGRAM_TYPE_RESPONSE_PUT:
                                            ResponsePutDatagram responsePutDg = (ResponsePutDatagram) responseDatagram;
                                            connection.send(responseTag, responsePutDg.serialize());
                                            break;
                                        case DATAGRAM_TYPE_RESPONSE_MULTIGET:
                                            ResponseMultiGetDatagram responseMultiGetDg = (ResponseMultiGetDatagram) responseDatagram;
                                            connection.send(responseTag, responseMultiGetDg.serialize());
                                            break;
                                        case DATAGRAM_TYPE_RESPONSE_MULTIPUT:
                                            ResponseMultiPutDatagram responseMultiPutDg = (ResponseMultiPutDatagram) responseDatagram;
                                            connection.send(responseTag, responseMultiPutDg.serialize());
                                            break;
                                        case DATAGRAM_TYPE_RESPONSE_GETWHEN:
                                            ResponseGetWhenDatagram responseGetWhenDg = (ResponseGetWhenDatagram) responseDatagram;
                                            connection.send(responseTag, responseGetWhenDg.serialize());
                                            break;
                                        default:
                                            break;
                                    }
                                }
                            } finally {
                                responses.unlock();
                            }
                        }
                    }
                } catch (EOFException e) {
                    this.dbManager.disconnect(user);
                    System.out.println("A conexão foi encerrada, já não há mais nada a ler!");;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (SocketException ex) {
                ex.printStackTrace();
            }
        }
    }
}