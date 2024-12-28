package com.sd56.common.exceptions;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * This class represents an exception that can be thrown by the SD56 system. It is used as a base for other exceptions
 * specific to the project, and contains an utility method to get the stacktrace of a {@link Throwable}.
 * It extends the {@link Exception} class.
 */
public class SDException extends Exception {
    public SDException(String message) {
        super(message);
    }

    /**
     * Get the stacktrace of a {@link Throwable}.
     * The stacktrace will be returned in the form of a string.
     */
    public static String getStackTrace(Throwable e) {
        assert e != null : "The exception must not be null";

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter printWriter = new PrintWriter(baos, true);
        e.printStackTrace(printWriter);
        String stackTrace = baos.toString();

        try {
            printWriter.close();
            baos.close();
        } catch(IOException ignore) {}

        return stackTrace;
    }
}
