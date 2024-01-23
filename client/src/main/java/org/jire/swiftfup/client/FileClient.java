package org.jire.swiftfup.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;

import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * @author Jire
 */
public final class FileClient {

    public static final long DEFAULT_TIMEOUT_NANOS = 5L * 1000 * 1000 * 1000;

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

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public boolean isConnected(Channel channel) {
        return channel != null && channel.isOpen();
    }

    public ChannelFuture createChannelFuture(final boolean reconnect,
                                             final Runnable whileWaiting,
                                             final long timeoutNanos) {
        final Bootstrap bootstrap = this.bootstrap
                .handler(new FileClientChannelInitializer(getFileRequests(), reconnect));

        final ChannelFuture[] futures = new ChannelFuture[remoteAddresses.length];
        for (int i = 0; i < futures.length; i++) {
            futures[i] = bootstrap.connect(remoteAddresses[i]);
        }

        long start = System.nanoTime();
        while (!Thread.interrupted() && System.nanoTime() - start < timeoutNanos) {
            for (ChannelFuture future : futures) {
                if (future.isSuccess()) {
                    for (ChannelFuture otherFuture : futures) {
                        if (otherFuture != future) {
                            otherFuture.cancel(true);
                        }
                    }
                    return future;
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

        System.err.println("timed out after " + TimeUnit.NANOSECONDS.toSeconds(timeoutNanos) + " seconds");
        return createChannelFuture(reconnect, whileWaiting, timeoutNanos + DEFAULT_TIMEOUT_NANOS);
    }

    public Channel connect(final boolean reconnect,
                           final Runnable whileWaiting) {
        final Channel channel = createChannelFuture(reconnect, whileWaiting, DEFAULT_TIMEOUT_NANOS)
                .syncUninterruptibly()
                .channel();

        setChannel(channel);
        return channel;
    }

    public Channel connect(final Runnable whileWaiting) {
        return connect(false, whileWaiting);
    }

    public Channel connectedChannel() {
        Channel channel = getChannel();
        return isConnected(channel) ? channel : connect(true, null);
    }

    public void flush() {
        Channel channel = getChannel();
        if (isConnected(channel))
            channel.flush();
    }

    public void request(FileRequest fileRequest) {
        Channel channel = connectedChannel();
        channel.write(fileRequest, channel.voidPromise());
        fileRequest.sent();
    }

    public FileRequest request(int filePair) {
        return getFileRequests().filePair(filePair, this);
    }

    public FileRequest request(int index, int file) {
        return request(FilePair.create(index, file));
    }

    public void request(FileChecksumsRequest checksumsRequest) {
        Channel channel = connectedChannel();
        channel.write(checksumsRequest, channel.voidPromise());
    }

    public FileChecksumsRequest requestChecksums() {
        return getFileRequests().checksums(this);
    }

}
