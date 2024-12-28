package com.sd56.common.util;

/**
 * Singleton class to get the caller for a method.
 */
public class GetCaller {
    public static final String DEFAULT_ROOT = "src/main/java/";

    /**
     * Get the caller of the method that called this method.
     *
     * @param root The root of the project.
     *             For example, if the root is "src/main/java/" and the class that called this method was
     *             com.sd56.client.Client, the caller will be returned as "src/main/java/com/sd56/client/Client.java:42".
     * @param skip The number of frames to skip.
     * @return The caller of the method that called this method.
     */
    public static String getCaller(String root, int skip) {
        StackWalker.StackFrame frame = StackWalker
                .getInstance(StackWalker.Option.SHOW_HIDDEN_FRAMES)
                .walk((s) -> s.skip(skip + 1).findFirst())
                .get();

        String caller = root
                + frame.getClassName().replaceAll("\\.", "/")
                + ".java:"
                + frame.getLineNumber();

        return caller;
    }
}
