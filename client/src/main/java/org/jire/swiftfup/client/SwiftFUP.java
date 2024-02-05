package org.jire.swiftfup.client;

import java.util.concurrent.TimeUnit;

/**
 * @author Jire
 */
public enum SwiftFUP {
    ;

    public static final AutoProcessor DEFAULT_AUTO_PROCESSOR = AutoProcessors.SLOW;

    public static final int DEFAULT_EXPECTED_REQUESTS = 2 << 15;

    public static final int DEFAULT_CHECKSUMS_TIMEOUT = 60;
    public static final TimeUnit DEFAULT_CHECKSUMS_TIMEOUT_UNIT = TimeUnit.SECONDS;

}
