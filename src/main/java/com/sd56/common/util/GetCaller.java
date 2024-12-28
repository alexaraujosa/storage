package com.sd56.common.util;

public class GetCaller {
    public static final String DEFAULT_ROOT = "src/main/java/";

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
