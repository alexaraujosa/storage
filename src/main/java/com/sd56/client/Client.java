package com.sd56.client;

import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.*;

import com.sd56.common.connectors.Demultiplexer;
import com.sd56.common.connectors.TaggedConnection;
import com.sd56.common.datagram.*;
import com.sd56.common.datagram.Datagram.DatagramType;
import com.sd56.common.exceptions.ConnectionException;
import com.sd56.common.exceptions.DataFileException;
import com.sd56.common.exceptions.SDException;
import com.sd56.common.menu.CLI.CLI;
import com.sd56.common.util.event.EventEmitter;
import com.sd56.common.util.logger.Logger;
import com.sd56.common.util.logger.LoggerLevel;
import com.sd56.common.util.logger.LoggerOptions;

public class Client {
    private final String HOST = "localhost";
    private final int PORT = 1337;

    private boolean authenticated;
    private Demultiplexer demultiplexer;

    private Thread[] workers;
    private ProtectedMessages messagesToSend;
//    private final LockedResource<Queue<ClientRequestQueueEntry>> requestQueue;
//    private final ClientRequestQueue requestQueue;

    private EventEmitter eventEmitter;
    private Logger logger;

    //TODO Acrescentar numero de workers
    public Client(int maxSessions, Logger logger) {
        this.authenticated = false;
        this.workers = new Thread[maxSessions];
        this.messagesToSend = new ProtectedMessages();
//        this.requestQueue = new LockedResource<>(new LinkedList<>());
//        this.requestQueue = new ClientRequestQueue();
        this.eventEmitter = new EventEmitter();
        this.logger = logger;

        try {
            logger.debug("Connecting to the server...");
            this.demultiplexer = new Demultiplexer(new TaggedConnection(new Socket(HOST, PORT)));
            this.demultiplexer.start();
            logger.success("Connection established.");
        } catch (Exception e) {
            logger.error("Error connecting to the server: " + SDException.getStackTrace(e));
        }
    }

//    public LockedResource<Queue<ClientRequestQueueEntry>> getRequestQueue() {
//        return this.requestQueue;
//    }

    public EventEmitter getEventEmitter() {
        return this.eventEmitter;
    }

    public Logger getLogger() {
        return this.logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
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

    public void closeWorkers() {
        for (Thread worker : this.workers) {
            if (worker != null) worker.interrupt();
        }
    }

    public ClientRequestQueueEntry<Object> close() throws ConnectionException {
        try {
            Datagram closeDg = new Datagram(DatagramType.DATAGRAM_TYPE_REQUEST_CLOSE);

            byte[] message = closeDg.serialize();
            ClientRequestQueueEntry<Object> entry = new ClientRequestQueueEntry<>(message);
            this.messagesToSend.setMessage(entry);

            return entry;
        } catch (Exception e) {
            throw new ConnectionException("[CLOSE] Error on CLOSE: " + SDException.getStackTrace(e));
        }
    }

    public ClientRequestQueueEntry<Object> authenticate(String username, String password) throws ConnectionException {
        try {
            RequestAuthDatagram reqAuth = new RequestAuthDatagram(username, password);


            byte[] message = reqAuth.serialize();
            ClientRequestQueueEntry<Object> entry = new ClientRequestQueueEntry<>(message);
            this.messagesToSend.setMessage(entry);

            return entry;
        } catch (Exception e) {
            throw new ConnectionException("[AUTH] Error on AUTH: " + SDException.getStackTrace(e));
        }
    }

    public ClientRequestQueueEntry<Object> get(String key) throws ConnectionException {
        try {
            RequestGetDatagram reqGet = new RequestGetDatagram(key);

            byte[] message = reqGet.serialize();
            ClientRequestQueueEntry<Object> entry = new ClientRequestQueueEntry<>(message);
            this.messagesToSend.setMessage(entry);

            return entry;
        } catch (Exception e) {
            throw new ConnectionException("[GET] Error on GET " + key + ": " + SDException.getStackTrace(e));
        }
    }

    public ClientRequestQueueEntry<Object> put(String key, String value) throws ConnectionException {
        try {
            RequestPutDatagram reqPut = new RequestPutDatagram(key, value.getBytes(StandardCharsets.UTF_8));

            byte[] message = reqPut.serialize();
            ClientRequestQueueEntry<Object> entry = new ClientRequestQueueEntry<>(message);
            this.messagesToSend.setMessage(entry);

            return entry;
        } catch (Exception e) {
            throw new ConnectionException("[PUT] Error on PUT " + key + ": " + SDException.getStackTrace(e));
        }
    }

    public ClientRequestQueueEntry<Object> multiGet(Set<String> keys) throws ConnectionException {
        try {
            RequestMultiGetDatagram reqMultiGet = new RequestMultiGetDatagram(keys);

            byte[] message = reqMultiGet.serialize();
            ClientRequestQueueEntry<Object> entry = new ClientRequestQueueEntry<>(message);
            this.messagesToSend.setMessage(entry);

            return entry;
        } catch (Exception e) {
            throw new ConnectionException("[MULTIGET] Error on MULTIGET " + keys.toString() + ": " + SDException.getStackTrace(e));
        }
    }

    public ClientRequestQueueEntry<Object> multiPut(Map<String, byte[]> values) throws ConnectionException {
        try {
            RequestMultiPutDatagram reqMultiPut = new RequestMultiPutDatagram(values);

            byte[] message = reqMultiPut.serialize();
            ClientRequestQueueEntry<Object> entry = new ClientRequestQueueEntry<>(message);
            this.messagesToSend.setMessage(entry);

            return entry;
        } catch (Exception e) {
            throw new ConnectionException("[MULTIPUT] Error on MULTIPUT " + values.keySet() + ": " + SDException.getStackTrace(e));
        }
    }

    //todo: oioioio passar para o handler
    public ClientRequestQueueEntry<Object> getWhen(String key, String keyCond, byte[] valueCond) throws ConnectionException {
        try{
            //System.out.println("\nKEY: " + key + "\nKeyCond: " + keyCond + "\nValueCond: " + new String(valueCond, StandardCharsets.UTF_8));
            RequestGetWhenDatagram reqGetWhen = new RequestGetWhenDatagram(key, keyCond, valueCond);

            byte[] message = reqGetWhen.serialize();
            ClientRequestQueueEntry<Object> entry = new ClientRequestQueueEntry<>(message);
            this.messagesToSend.setMessage(entry);

            return entry;
        } catch (Exception e) {
            throw new ConnectionException("[GETWHEN] Error on GET " + key + " WHEN " + keyCond + ": " + SDException.getStackTrace(e));
        }
    }

    public static void main(String[] args) throws IOException, DataFileException, ConnectionException {
        System.out.println("ARGS: " + Arrays.toString(args));
        CLI parsedArgs = new CLI(args);
        System.out.println("PARSED ARGS:\n" + parsedArgs.printParsedArgs());

        // ------- Show help message -------
        if (parsedArgs.getFlag("help")) {
            System.out.println("Usage: client [--headless] [--datafile=<path>]");
            return;
        }

        if(parsedArgs.getRemainingArgs().isEmpty()) {
            System.err.println("Missing arguments. Usage: java Client <maxThreads>");
            return;
        }

        Logger logger = new Logger(new LoggerOptions(
                LoggerLevel.DEFAULT_LEVELS,
                LoggerLevel.DEFAULT_LEVEL_ORDER,
                parsedArgs.getFlag("debug")
        ));
        if (parsedArgs.getFlag("silent")) logger.setLevel(LoggerLevel.LEVEL._SILENT);
        else if (parsedArgs.getFlag("debug")) logger.setLevel(LoggerLevel.LEVEL.DEBUG);
        else logger.setLevel(LoggerLevel.LEVEL.SUCCESS);
        Logger.setGlobalLogger(logger);

        Client client = new Client(Integer.parseInt(parsedArgs.getRemainingArgs().getFirst()), logger);

        try {
            if (parsedArgs.getFlag("headless")) { // ------- Headless mode -------
                if (!parsedArgs.has("datafile")) {
                    System.err.println("Missing Data File. Use --datafile=<path> to supply a data file.");
                    return;
                }

                logger.info("Running in headless mode.");

                ClientHeadless clientHeadless = new ClientHeadless(client, (String) parsedArgs.getSingle("datafile"));

                for (int i = 0; i < client.workers.length; i++) {
                    client.workers[i] = new Thread(new ClientHandler(i, client));
                    client.workers[i].start();
                }

                clientHeadless.run();

            } else { // ------- Interactive mode -------
                for (int i = 0; i < client.workers.length; i++) {
                    client.workers[i] = new Thread(new ClientHandler(i, client));
                    client.workers[i].start();
                }

                ClientUI menu = new ClientUI(client);

                menu.run();
            }
        } catch (Exception e) {
            logger.error("Uncaught Exception: " + SDException.getStackTrace(e));
            client.close();
            client.closeWorkers();
            System.exit(1);
        }

//        client.close();
//        client.closeWorkers();
//        System.exit(0);
    }
}
