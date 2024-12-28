package com.sd56.common.exceptions;

/**
 * This class represents an exception that can be thrown when an error occurs within a connection.
 */
public class ConnectionException extends SDException {
    public ConnectionException(String message) {
        super(message);
    }
}
