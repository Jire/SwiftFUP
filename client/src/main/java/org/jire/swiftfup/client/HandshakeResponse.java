package org.jire.swiftfup.client;

/**
 * @author Jire
 */
public final class HandshakeResponse {

    private final int status;
    private final int version;
    private final long timestamp;

    public HandshakeResponse(int status, int version, long timestamp) {
        this.status = status;
        this.version = version;
        this.timestamp = timestamp;
    }

    public HandshakeResponse(int status, int version) {
        this(status, version, SwiftFUP.getTimestamp());
    }

    public int getStatus() {
        return status;
    }

    public int getVersion() {
        return version;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getElapsed(HandshakeRequest handshakeRequest) {
        return getTimestamp() - handshakeRequest.getTimestamp();
    }

}
