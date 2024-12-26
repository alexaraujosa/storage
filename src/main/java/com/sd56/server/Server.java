package com.sd56.server;

import com.sd56.common.datagram.Datagram;
import com.sd56.server.handler.RequestHandler;
import com.sd56.server.handler.SessionHandler;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import com.sd56.common.util.LockedResource;

public class Server {
    private final static int PORT = 1337;
    private final AtomicInteger currentSessions;
    private final LockedResource<LinkedList<Socket>> clients;
    private final LockedResource<LinkedList<Map.Entry<DataOutputStream, Datagram>>> requests;
    private final LockedResource<LinkedList<GetWhenTuple>> getWhen;
    private final DatabaseManager dbManager;
    private final Thread[] sessionWorkers;
    private final Thread[] requestWorkers;
    private final ReentrantLock lock;
    private final Condition notEmpty;


    public Server(int maxSessions, int maxRequestWorkers) {
        this.currentSessions = new AtomicInteger(maxSessions);
        this.clients = new LockedResource<>(new LinkedList<>());
        this.requests = new LockedResource<>(new LinkedList<>());
        this.getWhen = new LockedResource<>(new LinkedList<>());
        this.dbManager = new DatabaseManager(this.getWhen.getResource());
        this.sessionWorkers = new Thread[maxSessions];
        this.requestWorkers = new Thread[maxRequestWorkers];
        this.lock = new ReentrantLock();
        this.notEmpty = this.lock.newCondition();
    }

    public LockedResource<LinkedList<Socket>> getClients() { return this.clients; }
    public LockedResource<LinkedList<Map.Entry<DataOutputStream, Datagram>>> getRequests() { return this.requests; }
    public LockedResource<LinkedList<GetWhenTuple>> getWhenLR() { return this.getWhen; }
    public DatabaseManager getDbManager() { return this.dbManager; }
    public ReentrantLock getLock() { return this.lock; }
    public Condition getNotEmptyCond() { return this.notEmpty; }
    public AtomicInteger getCurrentSessions() { return this.currentSessions; }

    public static void main(String[] args) throws IOException {
        if(args.length != 2) {
            System.err.println("Missing arguments. Usage: java Server <maxSessions> <maxRequestWorkers>");
            return;
        }

        Server server = new Server(
            Integer.parseInt(args[0]), 
            Integer.parseInt(args[1])
        );

        for (int i = 0; i < server.sessionWorkers.length ; i++) {
            server.sessionWorkers[i] = new Thread(
                new SessionHandler(
                    server.getCurrentSessions(), 
                    server.getDbManager(), 
                    server.getRequests(), 
                    server.getClients()
                )
            );
            server.sessionWorkers[i].start();
        }

        for (int i = 0 ; i < server.requestWorkers.length ; i++) {
            server.requestWorkers[i] = new Thread(
                new RequestHandler(
                    server.getRequests(), 
                    server.getWhenLR(), 
                    server.getDbManager()
                )
            );
            server.requestWorkers[i].start();
        }

        try (ServerSocket ss = new ServerSocket(PORT)) {
            System.out.println("Server successfully created. Awaiting for connections...");

            while (true) {
                Socket socket = ss.accept();
                System.out.println("Client connection accepted.");
                System.out.println("The availability of the server is " + server.currentSessions + " out of " + server.sessionWorkers.length + ".");
                
                server.clients.lock();
                try {
                    server.clients.getResource().add(socket);
                    System.out.println("Added a client to the waiting list!");
                    server.clients.signalAll();
                } finally {
                    server.clients.unlock();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
