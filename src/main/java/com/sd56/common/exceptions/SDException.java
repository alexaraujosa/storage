package com.sd56.common.exceptions;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

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
