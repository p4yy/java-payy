package logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogUtil {

    public static void logInfo(String loggerName, String message, Object... args) {
        Logger customLogger = LoggerFactory.getLogger(loggerName);
        customLogger.info(message, args);
    }

    public static void logError(String loggerName, String message, Object... args) {
        Logger customLogger = LoggerFactory.getLogger(loggerName);
        customLogger.error(message, args);
    }

    public static void logError(String loggerName, String message, Throwable throwable) {
        Logger customLogger = LoggerFactory.getLogger(loggerName);
        customLogger.error(message, throwable);
    }

    public static void showAsciiArt() {
        Logger logger = LoggerFactory.getLogger("JavaPayy");
        logger.info("\u001B[35m      _                  ____                   \u001B[0m");
        logger.info("\u001B[35m     | | __ ___   ____ _|  _ \\ __ _ _   _ _   _ \u001B[0m");
        logger.info("\u001B[35m  _  | |/ _` \\ \\ / / _` | |_) / _` | | | | | | |\u001B[0m");
        logger.info("\u001B[35m | |_| | (_| |\\ V / (_| |  __/ (_| | |_| | |_| |\u001B[0m");
        logger.info("\u001B[35m  \\___/ \\__,_| \\_/ \\__,_|_|   \\__,_|\\__, |\\__, |\u001B[0m");
        logger.info("\u001B[35m                                    |___/ |___/ \u001B[0m");
        logger.info("\u001B[33mUse version 1.1\u001B[0m");
        logger.info("\u001B[33mCreated by iqlasss(456374610305220611)\u001B[0m");
        logger.info("\u001B[33mJoin discord:\u001B[0m");
        logger.info("\u001B[33mdiscord.gg/nysacFuJTs\u001B[0m");
        logger.info("\u001B[33mdiscord.gg/payy\u001B[0m");
    }

}
