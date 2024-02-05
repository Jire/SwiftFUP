package org.jire.swiftfup.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Holds default SLF4J logger.
 *
 * @author Jire
 * @see #getLogger()
 */
public enum Logging {
    ;

    private static final Logger logger = LoggerFactory.getLogger(Logging.class);

    /**
     * Use when you need a general logger for when you don't want to make a virtual logger instance.
     */
    public static Logger getLogger() {
        return logger;
    }

}
