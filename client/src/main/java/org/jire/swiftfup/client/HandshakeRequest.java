package org.jire.swiftfup.client;

import java.util.concurrent.CompletableFuture;

/**
 * @author Jire
 */
public final class HandshakeRequest extends CompletableFuture<HandshakeResponse> {

    private final int version;
    private final long timestamp;

    public HandshakeRequest(int version, long timestamp) {
        this.version = version;
        this.timestamp = timestamp;
    }

    public HandshakeRequest() {
        this(SwiftFUP.VERSION, SwiftFUP.getTimestamp());
    }

    public int getVersion() {
        return version;
    }

    public long getTimestamp() {
        return timestamp;
    }

}
