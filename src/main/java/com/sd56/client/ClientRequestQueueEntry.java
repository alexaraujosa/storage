package com.sd56.client;

import com.sd56.common.util.LockedResource;

import java.time.Instant;
import java.util.Arrays;

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

    public int getId() {
        return this.id;
    }

    public boolean succeeded() {
        return this.success;
    }

    public void setSuccess(boolean succeeded) {
        this.success = succeeded;
    }

    public T getResponse() {
        return this.response;
    }

    public void setResponse(T response) {
        this.success = true;
        this.response = response;
    }

    public Exception getError() {
        return this.error;
    }

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
