package com.sd56.server;

import com.sd56.common.datagram.ResponseGetWhenDatagram;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.locks.ReentrantLock;

public class DatabaseManager {
    private final HashMap<String, String> users;
    private final HashMap<String, byte[]> db;
    private final Queue<GetWhenTuple> getWhenQueue;
    private final ReentrantLock l;

    public DatabaseManager(Queue<GetWhenTuple> getWhenQueue){
        this.users = new HashMap<>();
        this.db = new HashMap<>();
        this.getWhenQueue = getWhenQueue;
        this.l = new ReentrantLock();
    }

    public HashMap<String, String> getUsers() { return this.users; }
    public HashMap<String, byte[]> getDb() { return this.db; }
    public Queue<GetWhenTuple> getGetWhenQueue() { return this.getWhenQueue; }

    public boolean authentication(String username, String password) {
        l.lock();
        try {
            // Wrong password
            if (users.containsKey(username) && !users.get(username).equals(password))
                return false;

            // New account
            if (!users.containsKey(username))
                users.put(username, password);
            
            return true;
        } finally {
            l.unlock();
        }
    }

    public void put(String key, byte[] value) {
        l.lock();
        try {
            // verificar se a key e igual a alguma keyCond presente na queue dos pedidos getWhen
            this.db.put(key, value);
            //this.printGetWhenQueue();
            this.changeFlag(key,value);
            //this.printGetWhenQueue();
            this.executeGetWhenRequests();
           // this.printGetWhenQueue();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            l.unlock();
        }
    }

    public void printGetWhenQueue(){
        for(GetWhenTuple g : this.getWhenQueue){
            System.out.println(g.toString());
        }
    }

    public void executeGetWhenRequests() throws IOException {
        for(GetWhenTuple g : this.getWhenQueue){
            if(g.getFlag() == 1){
                byte[] value = this.getDb().get(g.getKey());
                ResponseGetWhenDatagram resGetWhen = new ResponseGetWhenDatagram(value);
                resGetWhen.serialize(g.getDataOutputStream());
                this.removeGetWhenTple(g);
            }
        }
    }

    public void removeGetWhenTple(GetWhenTuple tuple){
        l.lock();
        try{
            boolean removed = this.getWhenQueue.remove(tuple);
            if(removed)
                System.out.println("\nTuple successfully removed!");
            else
                System.out.println("\nCould not remove that tuple!");
        } finally {
            l.unlock();
        }
    }

    public void changeFlag(String key, byte[] value) {
        l.lock();
        try {
            for (GetWhenTuple g : this.getWhenQueue) {
                if (Objects.equals(key, g.getKeyCond())) {
                    if (Arrays.equals(value, g.getValueCond())) {
                        g.setFlagTrue();
                    } else {
                        g.setFlagFalse();
                    }
                }
            }
        } finally {
            l.unlock();
        }
    }

    public byte[] get(String key) {
        l.lock();
        try {
            return this.db.get(key);
        } finally {
            l.unlock();
        }
    }
}