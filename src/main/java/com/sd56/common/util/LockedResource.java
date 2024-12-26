package com.sd56.common.util;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class LockedResource<T> {
    private T resource;
    private final ReentrantLock l;
    private final Condition c;

    public LockedResource(T resource) {
        this.resource = resource;
        this.l = new ReentrantLock();
        this.c = this.l.newCondition();
    }

    public T getResource() {
        return this.resource;
    }

    public void setResource(T resource) {
        this.resource = resource;
    }

    public void lock() {
        this.l.lock();
    }

    public void unlock() {
        this.l.unlock();
    }

    public void await() throws InterruptedException {
        try {
            this.c.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void signalAll() {
        this.c.signalAll();
    }
}
