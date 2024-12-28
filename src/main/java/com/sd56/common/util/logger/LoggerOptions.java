package com.sd56.common.util.logger;

import java.util.List;

/**
 * This record represents the options that can be used to configure a logger.
 */
public record LoggerOptions(
        /**
         * The levels of severity that can be used to log messages.
         */
        List<LoggerLevel> levels,
        /**
         * The order in which the levels of severity should be considered.
         */
        List<LoggerLevel.LEVEL> levelOrder,
        /**
         * Whether the caller of each log should be printed in the log message. Useful for debugging.
         */
        boolean printCaller
) {

}
