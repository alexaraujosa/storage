package com.sd56.client;

import com.sd56.common.util.LockedResource;

import java.time.Instant;
import java.util.Arrays;

/**
 * Represents a message entry in the client request queue.
 * Each entry acts both as the request and the response for a given request to the server.
 * <p>
 * In order to use it in a "sync" mode, using ClientRequestQueueEntry#await() is required.
 * <pre> {@code
 *     ClientRequestQueueEntry<?> entry = this.client.authenticate(username, password);
 *     entry.lock();
 *     entry.await();
 *
 *     // Do stuff here...
 *
 *     entry.unlock();
 * }</pre>
 * <p>
 * In order to use it in an "async" mode, using Client#getEventEmitter() is required.
 * <pre> {@code
 *     ClientRequestQueueEntry<?> entry = this.client.get(key);
 *     this.client.getEventEmmiter().once(String.valueOf(entry.getId()), (e) -> {
 *         entry.lock();
 *
 *         // Do stuff here...
 *
 *         entry.unlock();
 *     });
 * }</pre>
 *
 * @param <T> The type of the response.
 */
public class ClientRequestQueueEntry<T> extends LockedResource<byte[], ClientRequestQueueEntry<T>> {
    private final int id;
    private boolean success;
    private T response;
    private Exception error;

    public ClientRequestQueueEntry(byte[] message) {
        super(null);
        this.id = Instant.now().getNano();
        this.setResource(message);

        this.success = true;
        this.response = null;
        this.error = null;
    }

    public ClientRequestQueueEntry(int id, byte[] message) {
        super(null);
        this.id = id;
        this.setResource(message);
    }

    /**
     * Returns the ID of the request.
     */
    public int getId() {
        return this.id;
    }

    /**
     * Returns whether the request succeeded or not.
     */
    public boolean succeeded() {
        return this.success;
    }

    /**
     * Sets the success of the request. By default, the method {@link #setResponse(Object)} sets the success flag to
     * true, while the method {@link #setError(Exception)} sets the success flag to false.
     * This method is used to explicitly override the success flag, and should be used after all calls to those
     * two methods.
     */
    public void setSuccess(boolean succeeded) {
        this.success = succeeded;
    }

    /**
     * Returns the response of the request.
     */
    public T getResponse() {
        return this.response;
    }

    /**
     * Sets the response of the request. This method is used to add a response to this request, indicating that it
     * has been processed, and can be read. It is recommended to use {@link #autoExec(LockedResourceAutoRunnable)}
     * to run this method without having to manually lock and unlock the entry.
     * As explained in {@link #setSuccess(boolean)}, this method also sets the success flag to true.
     */
    public void setResponse(T response) {
        this.success = true;
        this.response = response;
    }

    /**
     * Returns the error of the request.
     */
    public Exception getError() {
        return this.error;
    }

    /**
     * Sets the error of the request. This method is used to add an error to this request, indicating that it has
     * been processed and failed. It is recommended to use {@link #autoExec(LockedResourceAutoRunnable)} to run
     * this method without having to manually lock and unlock the entry.
     * As explained in {@link #setSuccess(boolean)}, this method also sets the success flag to false.
     */
    public void setError(Exception e) {
        this.success = false;
        this.error = e;
    }

    @Override
    public String toString() {
        return "ClientRequestQueueEntry{"
                + "id=" + id
                + ", message=" + Arrays.toString(getResource())
                + ", success=" + success
                + ", response=" + response
                + ", error=" + error
                + '}';
    }
}
