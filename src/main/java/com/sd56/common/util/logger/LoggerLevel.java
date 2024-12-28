package com.sd56.common.util.logger;

import java.util.List;

public record LoggerLevel(LEVEL level, String format) {
    public enum LEVEL {
        _SILENT,
        _DEFAULT,
        DEBUG,
        SUCCESS,
        INFO,
        WARN,
        ERROR,
        FATAL
    }

    public static final LoggerLevel DEFAULT = new LoggerLevel(LEVEL._DEFAULT, "\u001B[0m");
    public static final LoggerLevel DEBUG = new LoggerLevel(LEVEL.DEBUG, "\u001B[35m");
    public static final LoggerLevel SUCCESS = new LoggerLevel(LEVEL.SUCCESS, "\u001B[32m");
    public static final LoggerLevel INFO = new LoggerLevel(LEVEL.INFO, "\u001B[36m");
    public static final LoggerLevel WARN = new LoggerLevel(LEVEL.WARN, "\u001B[33m");
    public static final LoggerLevel ERROR = new LoggerLevel(LEVEL.ERROR, "\u001B[31m");
    public static final LoggerLevel FATAL = new LoggerLevel(LEVEL.FATAL, "\u001B[91m");

    public static List<LoggerLevel> DEFAULT_LEVELS = List.of(
            DEBUG,
            SUCCESS,
            INFO,
            WARN,
            ERROR,
            FATAL
    );

    public static List<LoggerLevel.LEVEL> DEFAULT_LEVEL_ORDER = List.of(
            LEVEL.FATAL,
            LEVEL.INFO,
            LEVEL.SUCCESS,
            LEVEL.ERROR,
            LEVEL.WARN,
            LEVEL.DEBUG
    );
}
