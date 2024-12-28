package com.sd56.common.util;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class LockedResource<T, THIS extends LockedResource<T, THIS>> {
    private T resource;
    private final ReentrantLock l;
    private final Condition c;

    public LockedResource(T resource) {
        this.resource = resource;
        this.l = new ReentrantLock();
        this.c = this.l.newCondition();
    }

    @SuppressWarnings("unchecked")
    private THIS self() {
        return (THIS)this;
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

    public <R> R exec(LockedResourceRunnable<T, R> r) {
        this.l.lock();
        try {
            return r.run(this.resource);
        } finally {
            this.l.unlock();
        }
    }

    public <R> R autoExec(LockedResourceAutoRunnable<T, R, THIS> lr) {
        this.l.lock();
        try {
            return lr.run(self());
        } finally {
            this.l.unlock();
        }
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

    public interface LockedResourceRunnable<T, R> {
        R run(T r);
    }

    public interface LockedResourceAutoRunnable<T, R, THIS extends LockedResource<T, THIS>> {
        R run(THIS lr);
    }
}
