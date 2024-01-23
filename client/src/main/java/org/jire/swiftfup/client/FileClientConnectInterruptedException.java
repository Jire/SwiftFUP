package org.jire.swiftfup.client;

/**
 * @author Jire
 */
public class FileClientConnectInterruptedException extends RuntimeException {

    public FileClientConnectInterruptedException() {
    }

    public FileClientConnectInterruptedException(String message) {
        super(message);
    }

    public FileClientConnectInterruptedException(String message, Throwable cause) {
        super(message, cause);
    }

    public FileClientConnectInterruptedException(Throwable cause) {
        super(cause);
    }

    public FileClientConnectInterruptedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
