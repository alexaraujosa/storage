package com.sd56.common.exceptions;

/**
 * This class represents an exception that can be thrown when an error occurs while reading or processing a
 * {@link com.sd56.client.ClientDataFile ClientDataFile}.
 */
public class DataFileException extends SDException {
    public DataFileException(String message) {
        super(message);
    }
}
