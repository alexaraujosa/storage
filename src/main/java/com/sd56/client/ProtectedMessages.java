package com.sd56.client;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ProtectedMessages {
    private final ReentrantLock lock = new ReentrantLock();
    private final Queue<byte[]> messagesToSend;
    private Condition isEmpty = lock.newCondition();

    public ProtectedMessages() {
        messagesToSend = new LinkedList<>();
    }

    public ReentrantLock getLock() {
        return this.lock;
    }

    public Condition getEmptyCondition() {
        return this.isEmpty;
    }

    public Queue<byte[]> getMessagesToSend() {
        return this.messagesToSend;
    }

    public byte[] getMessage() {
        lock.lock();
        try{
            return messagesToSend.poll();
        } finally {
            lock.unlock();
        }
    }

    public void setMessage(byte[] message) {
        lock.lock();
        try {
            messagesToSend.add(message);
            this.isEmpty.signal();
        } finally {
            lock.unlock();
        }
    }
}
