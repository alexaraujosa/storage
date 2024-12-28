package com.sd56.common.util;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This class offers a wrapper for a resource that enables it to be used in multithreaded environments.
 * It provides a lock mechanism to ensure that only one thread can access the resource at a time, along with some QoL
 * methods.
 *
 * @param <T> The type of the resource.
 * @param <THIS> The type of the class that extends this class. If used to extend this class, it should be the subclass
 *              that is extending it. Otherwise, using <code>?</code> is recommended.
 */
public class LockedResource<T, THIS extends LockedResource<T, THIS>> {
    private T resource;
    private final ReentrantLock l;
    private final Condition c;

    public LockedResource(T resource) {
        this.resource = resource;
        this.l = new ReentrantLock();
        this.c = this.l.newCondition();
    }

    /**
     * Auxiliary method to return the instance of the subclass, typed to the THIS type, no matter where in the chain
     * it is used.
     */
    @SuppressWarnings("unchecked")
    private THIS self() {
        return (THIS)this;
    }

    /**
     * Returns the resource associated with this guard. This method does not perform automatic locking, deferring the
     * responsibility to the caller.
     */
    public T getResource() {
        return this.resource;
    }

    /**
     * Sets the resource associated with this guard. This method does not perform automatic locking, deferring the
     * responsibility to the caller.
     */
    public void setResource(T resource) {
        this.resource = resource;
    }

    /**
     * Locks the resource, preventing other threads from accessing while the caller doesn't unlock it.
     */
    public void lock() {
        this.l.lock();
    }

    /**
     * Unlocks the resource, allowing other threads to access it.
     */
    public void unlock() {
        this.l.unlock();
    }

    /**
     * Executes a runnable that uses the resource in some way, and returns a value of the same type as the resource.
     * The resource guard is locked before the runnable is executed, and unlocked after it finishes.
     *
     * @param r The runnable to be executed.
     **/
    public <R> R exec(LockedResourceRunnable<T, R> r) {
        this.l.lock();
        try {
            return r.run(this.resource);
        } finally {
            this.l.unlock();
        }
    }

    /**
     * Executes a runnable that uses the resource guard in some way, and returns a value of the same type as the resource.
     * The resource guard is locked before the runnable is executed, and unlocked after it finishes.
     * It is not recommended to use the locking mechanism inside the runnable section, given it is automatically managed
     * by the method itself.
     *
     * @param lr The runnable
     **/
    public <R> R autoExec(LockedResourceAutoRunnable<T, R, THIS> lr) {
        this.l.lock();
        try {
            return lr.run(self());
        } finally {
            this.l.unlock();
        }
    }

    /**
     * Waits for a signal to be sent to the resource guard. This method should be used inside a locked section, as it
     * will unlock the resource guard while waiting for the signal.
     */
    public void await() throws InterruptedException {
        try {
            this.c.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends a signal to the resource guard, waking up any thread that is waiting for it.
     */
    public void signalAll() {
        this.c.signalAll();
    }

    /**
     * Represents a runnable that uses a resource guard in some way, and returns a value of the same type as the resource.
     */
    public interface LockedResourceRunnable<T, R> {
        R run(T r);
    }

    /**
     * Represents a runnable that uses a resource guard in some way, and returns a value of the same type as the resource.
     * The resource guard is passed as an argument to the runnable, allowing it to be used inside the runnable section.
     */
    public interface LockedResourceAutoRunnable<T, R, THIS extends LockedResource<T, THIS>> {
        R run(THIS lr);
    }
}
