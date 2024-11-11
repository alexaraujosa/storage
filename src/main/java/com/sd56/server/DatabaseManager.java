package com.sd56.server;

import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;

public class DatabaseManager {
    private HashMap<String, String> users;
    private HashMap<String, byte[]> db;
    private ReentrantLock lock;

    public DatabaseManager(){
        this.users = new HashMap<>();
        this.db = new HashMap<>();
        this.lock = new ReentrantLock();
    }

    /*
    // caso queiramos o registo separado do login
    public void addUser(String username , String password){
        if(!this.users.containsKey(username)){
            this.users.put(username,password);
        } else {
            // throw new Exception(User already exists);
        }
    }
     */

    public boolean login(String username, String password){
        boolean login = false;
        try {
            lock.lock();
            if (!this.users.containsKey(username)) {
                // caso em que o user ainda nao fez registo
                this.users.put(username, password);
                login = true;
            } else {
                // caso em que o user ja fez registo

                if (this.users.get(username).equals(password)) {
                    // password correta
                    login = true;
                }
            }
        } finally {
            lock.unlock();
        }
        return login;
    }


    void setDb(HashMap<String, byte[]> db) {
        this.db = db;
    }

    HashMap<String, byte[]> getDb() {
        return this.db;
    }

    public void put(String key, byte[] value){
        try {
            lock.lock();
            this.db.put(key, value);
        } finally {
            lock.unlock();
        }
        // mesmo que a chave ja exista, o valor e atualizado automaticamente
    }

    public byte[] get(String key){
        try {
            lock.lock();
            return this.db.get(key);
        } finally {
            lock.unlock();
        }
    }
}