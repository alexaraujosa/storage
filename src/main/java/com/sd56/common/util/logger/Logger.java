package com.sd56.common.util.logger;

import com.sd56.common.util.GetCaller;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This class is an implementation of a logger, which allows for message logging with different configurable levels of severity.
 */
public class Logger {
    private static Logger globalLogger;

    private final List<LoggerLevel.LEVEL> levelOrder;
    private LoggerLevel.LEVEL currentLevel;
    private final Map<LoggerLevel.LEVEL, LoggerLevel> levels;
    private final boolean printCaller;

    public Logger() {
        this(new LoggerOptions(
                LoggerLevel.DEFAULT_LEVELS,
                LoggerLevel.DEFAULT_LEVEL_ORDER,
                true
        ));
        this.setLevel(LoggerLevel.LEVEL.INFO);
    }

    public Logger(LoggerOptions options) {
        this.levelOrder = new ArrayList<>(options.levelOrder());
        // Add silent level to the beginning of the list, to allow for complete disabling of logging
        this.levelOrder.addFirst(LoggerLevel.LEVEL._SILENT);

        this.currentLevel = options.levelOrder().getLast();

        this.levels = new HashMap<>();
        for (LoggerLevel level : options.levels()) {
            this.levels.put(level.level(), level);
        }

        this.levels.put(LoggerLevel.LEVEL._DEFAULT, LoggerLevel.DEFAULT);

        this.printCaller = options.printCaller();
    }

    /**
     * Sets the current logging level. Only messages with a level equal to or higher than the current level will be
     * logged, while messages with a lower level will be ignored.
     * @param level A level contained in the level order list.
     */
    public void setLevel(LoggerLevel.LEVEL level) {
        if (this.levelOrder.contains(level)) {
            this.currentLevel = level;
        }
    }

    public void log(String message) {
        if (this.levels.containsKey(LoggerLevel.LEVEL._DEFAULT)) log(LoggerLevel.DEFAULT, message);
    }
    public void debug(String message) {
        if (this.levelOrder.indexOf(this.currentLevel) < this.levelOrder.indexOf(LoggerLevel.LEVEL.DEBUG)) return;
        if (this.levels.containsKey(LoggerLevel.LEVEL.DEBUG)) log(LoggerLevel.DEBUG, message);
    }
    public void success(String message) {
        if (this.levelOrder.indexOf(this.currentLevel) < this.levelOrder.indexOf(LoggerLevel.LEVEL.SUCCESS)) return;
        if (this.levels.containsKey(LoggerLevel.LEVEL.SUCCESS)) log(LoggerLevel.SUCCESS, message);
    }
    public void info(String message) {
        if (this.levelOrder.indexOf(this.currentLevel) < this.levelOrder.indexOf(LoggerLevel.LEVEL.INFO)) return;
        if (this.levels.containsKey(LoggerLevel.LEVEL.INFO)) log(LoggerLevel.INFO, message);
    }
    public void warn(String message) {
        if (this.levelOrder.indexOf(this.currentLevel) < this.levelOrder.indexOf(LoggerLevel.LEVEL.WARN)) return;
        if (this.levels.containsKey(LoggerLevel.LEVEL.WARN)) log(LoggerLevel.WARN, message);
    }
    public void error(String message) {
        if (this.levelOrder.indexOf(this.currentLevel) < this.levelOrder.indexOf(LoggerLevel.LEVEL.ERROR)) return;
        if (this.levels.containsKey(LoggerLevel.LEVEL.ERROR)) log(LoggerLevel.ERROR, message);
    }
    public void fatal(String message) {
        if (this.levelOrder.indexOf(this.currentLevel) < this.levelOrder.indexOf(LoggerLevel.LEVEL.FATAL)) return;
        if (this.levels.containsKey(LoggerLevel.LEVEL.FATAL)) log(LoggerLevel.FATAL, message);
    }

    public void log(LoggerLevel level, String message) {
        log(level.level(), message);
    }

    private void log(LoggerLevel.LEVEL level, String message) {
        LoggerLevel loggerLevel = this.levels.get(level);
        String formatterMessage = "";

        formatterMessage += "[" + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss:SSS").format(new Date()) + "] ";

        if (this.printCaller) {
            formatterMessage += "[" + GetCaller.getCaller(GetCaller.DEFAULT_ROOT, 3) + "] ";
        }

        formatterMessage += message;

        System.out.println(loggerLevel.format() + formatterMessage + "\u001B[0m");
    }

    /**
     * Returns the global logger instance. If no global logger instance exists, a new one is created.
     */
    public static Logger getGlobalLogger() {
        if (globalLogger == null) {
            globalLogger = new Logger();
        }

        return globalLogger;
    }

    /**
     * Sets the global logger instance to the specified logger.
     * @param logger A Logger instance to set as the global logger.
     */
    public static void setGlobalLogger(Logger logger) {
        globalLogger = logger;
    }
}
