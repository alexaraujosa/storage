package com.sd56.server;

import com.sd56.server.DatabaseManager;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Server {
    private final static int PORT = 1337;
    private final int maxSessions;

    private Queue<Socket> clients;
    private Queue<GetWhenTuple> getWhenQueue;
    private Thread[] workers;
    private ReentrantLock lock;
    private Condition notEmpty;

    private int currentSessions;
    private final DatabaseManager dbManager;

    public Server(int maxSessions) {
        this.clients = new LinkedList<>();
        this.getWhenQueue = new LinkedList<>();
        this.workers = new Thread[maxSessions];
        this.lock = new ReentrantLock();
        this.notEmpty = this.lock.newCondition();
        this.currentSessions = maxSessions;
        this.maxSessions = maxSessions;
        this.dbManager = new DatabaseManager(this.getGetWhenQueue());
    }

    public Queue<Socket> getClients() { return this.clients; }
    public Queue<GetWhenTuple> getGetWhenQueue() { return this.getWhenQueue; }
    public ReentrantLock getLock() { return this.lock; }
    public Condition getNotEmptyCond() { return this.notEmpty; }

    public int getMaxSessions() { return this.maxSessions; }
    public int getCurrentSessions() { return this.currentSessions; }
    public void setCurrentSessions(int value) { this.currentSessions = value; }
    public DatabaseManager getDbManager() { return this.dbManager; }

    public void addGetWhenTuple(GetWhenTuple tuple){
        lock.lock();
        try{
            this.getWhenQueue.add(tuple);
            //if(this.getWhenQueue.contains(tuple))
                //System.out.println(tuple.toString() + " successfully added.");
        } finally {
            lock.unlock();
        }
    }

    public static void main(String[] args) throws IOException {
        if(args.length != 1) {
            System.err.println("Missing arguments. Usage: java Server <maxSessions>");
            return;
        }
        Server server = new Server(Integer.parseInt(args[0]));

        for (int i = 0; i < server.maxSessions; i++){
            server.workers[i] = new Thread(new Handler(server));
            server.workers[i].start();
        }

        try (ServerSocket ss = new ServerSocket(PORT)) {
            System.out.println("Server successfully created. Awaiting for connections...");

            while (true) {
                Socket socket = ss.accept();
                System.out.println("Client connection accepted.");

                System.out.println("The availability of the server is " + server.currentSessions + "\\" + server.maxSessions);
                try{
                    server.getLock().lock();
                    server.clients.add(socket);
                    System.out.println("Added a client to the waiting list!");
                    server.notEmpty.signal();
                } finally {
                    server.lock.unlock();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
