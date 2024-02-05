package org.jire.swiftfup.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;

import java.net.SocketAddress;

/**
 * @author Jire
 */
public final class FileClient {

    public static final long DEFAULT_TIMEOUT_NANOS = 5L * 1000 * 1000 * 1000;
    public static final long MAX_TIMEOUT_NANOS = 30L * 1000 * 1000 * 1000;

    private final FileRequests fileRequests;
    private final Bootstrap bootstrap;
    private final SocketAddress[] remoteAddresses;

    private volatile Channel channel;

    public FileClient(FileRequests fileRequests,
                      EventLoopGroup eventLoopGroup,
                      Class<? extends Channel> channelClass,
                      SocketAddress... remoteAddresses) {
        if (remoteAddresses == null || remoteAddresses.length == 0) {
            throw new IllegalArgumentException("Need to pass at least one address");
        }

        this.fileRequests = fileRequests;

        bootstrap = new Bootstrap()
                .channel(channelClass)
                .group(eventLoopGroup)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 120_000);
        try {
            // unsupported on older Windows versions (2003 and XP)
            bootstrap.option(ChannelOption.IP_TOS, 0b100_000_10);
        } catch (final Exception e) {
            e.printStackTrace();
        }

        this.remoteAddresses = remoteAddresses;
    }

    public FileRequests getFileRequests() {
        return fileRequests;
    }

    public Channel getChannel() {
        return channel;
    }

    public Channel createChannelFuture(final boolean reconnect,
                                       final Runnable whileWaiting,
                                       final long timeoutNanos) {
        final Bootstrap bootstrap = this.bootstrap
                .handler(new FileClientChannelInitializer(getFileRequests(), reconnect));

        final ChannelFuture[] futures = new ChannelFuture[remoteAddresses.length];
        for (int i = 0; i < futures.length; i++) {
            futures[i] = bootstrap.connect(remoteAddresses[i]);
        }

        if (whileWaiting != null) {
            whileWaiting.run();
        }

        long start = System.nanoTime();
        while (!Thread.interrupted() && System.nanoTime() - start < (timeoutNanos / 2)) {
            if (whileWaiting != null) {
                whileWaiting.run();
            }

            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        start = System.nanoTime();
        while (!Thread.interrupted() && System.nanoTime() - start < timeoutNanos) {
            for (ChannelFuture future : futures) {
                if (future.isSuccess()) {
                    Channel channel = future.syncUninterruptibly().channel();
                    if (channel.isActive()) {
                        for (ChannelFuture otherFuture : futures) {
                            if (otherFuture != future) {
                                otherFuture.cancel(true);
                            }
                        }
                        return channel;
                    }
                }
            }

            if (whileWaiting != null) {
                whileWaiting.run();
            }

            Thread.yield();
        }

        if (Thread.interrupted()) {
            throw new FileClientConnectInterruptedException(
                    "Thread was interrupted during " + (reconnect ? "re" : "") + "connect");
        }

        return createChannelFuture(reconnect, whileWaiting,
                Math.min(timeoutNanos + DEFAULT_TIMEOUT_NANOS, MAX_TIMEOUT_NANOS));
    }

    public Channel connect(final boolean reconnect,
                           final Runnable whileWaiting) {
        final Channel channel = createChannelFuture(reconnect, whileWaiting, DEFAULT_TIMEOUT_NANOS);
        this.channel = channel;
        return channel;
    }

    public FileRequest request(int filePair) {
        return fileRequests.filePair(filePair);
    }

    public FileRequest request(int index, int file) {
        return request(FilePair.create(index, file));
    }

    public void request(FileChecksumsRequest checksumsRequest) {
        Channel channel = getChannel();
        channel.writeAndFlush(checksumsRequest, channel.voidPromise());
    }

    public FileChecksumsRequest requestChecksums() {
        return getFileRequests().checksums(this);
    }

}
