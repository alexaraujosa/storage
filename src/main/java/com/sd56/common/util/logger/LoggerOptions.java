package com.sd56.common.util.logger;

import java.util.List;

public record LoggerOptions(
        List<LoggerLevel> levels,
        List<LoggerLevel.LEVEL> levelOrder,
        boolean printCaller
) {

}
