package com.sd56.server;

import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;

public class DatabaseManager {
    private final HashMap<String, String> users;
    private final HashMap<String, byte[]> db;
    private final ReentrantLock l;

    public DatabaseManager(){
        this.users = new HashMap<>();
        this.db = new HashMap<>();
        this.l = new ReentrantLock();
    }

    public HashMap<String, String> getUsers() { return this.users; }
    public HashMap<String, byte[]> getDb() { return this.db; }

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
            this.db.put(key, value);
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