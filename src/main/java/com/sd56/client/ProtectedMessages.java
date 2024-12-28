package com.sd56.client;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ProtectedMessages {
    private final ReentrantLock lock = new ReentrantLock();
    private final Queue<ClientRequestQueueEntry<Object>> messagesToSend;
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

    public Queue<ClientRequestQueueEntry<Object>> getMessagesToSend() {
        return this.messagesToSend;
    }

    public ClientRequestQueueEntry<Object> getMessage() {
        lock.lock();
        try{
            return messagesToSend.poll();
        } finally {
            lock.unlock();
        }
    }

    public void setMessage(ClientRequestQueueEntry<Object> entry) {
        lock.lock();
        try {
            messagesToSend.add(entry);
            this.isEmpty.signal();
        } finally {
            lock.unlock();
        }
    }
}
