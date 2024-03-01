package org.jire.swiftfup.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import org.jetbrains.annotations.Nullable;

import java.net.SocketAddress;
import java.util.function.Consumer;

import static org.jire.swiftfup.client.codec.HandshakeResponseHandler.HANDSHAKE_RESPONSE_ATTRIBUTE_KEY;

/**
 * @author Jire
 */
public final class FileClient {

    public static final long DEFAULT_TIMEOUT_NANOS = 5L * 1000 * 1000 * 1000;
    public static final long MAX_TIMEOUT_NANOS = 30L * 1000 * 1000 * 1000;

    private final FileRequests fileRequests;
    private final Bootstrap bootstrap;
    private final Consumer<Channel> whenReconnected;
    private final SocketAddress[] remoteAddresses;

    private volatile Channel channel;

    public FileClient(FileRequests fileRequests,
                      EventLoopGroup eventLoopGroup,
                      Class<? extends Channel> channelClass,
                      @Nullable Consumer<Channel> whenReconnected,
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
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 120_000)
                .option(ChannelOption.SO_SNDBUF, 2 << 15)
                .option(ChannelOption.SO_RCVBUF, 2 << 15);

        this.whenReconnected = whenReconnected;

        this.remoteAddresses = remoteAddresses;
    }

    public Channel getChannel() {
        return channel;
    }

    public Channel createChannelFuture(final boolean reconnect,
                                       @Nullable final Runnable whileWaiting,
                                       final Consumer<Channel> whenReconnected,
                                       final long timeoutNanos) {
        final Bootstrap bootstrap = this.bootstrap
                .handler(new FileClientChannelInitializer(fileRequests));

        final ChannelFuture[] futures = new ChannelFuture[remoteAddresses.length];
        for (int i = 0; i < futures.length; i++) {
            futures[i] = bootstrap.connect(remoteAddresses[i]);
        }

        final long start = System.nanoTime();
        while (!Thread.interrupted() && System.nanoTime() - start < timeoutNanos) {
            for (final ChannelFuture future : futures) {
                if (!future.isSuccess()) continue;

                final Channel channel = future.channel();
                if (!channel.isActive()) continue;

                final HandshakeResponse handshakeResponse = channel.attr(HANDSHAKE_RESPONSE_ATTRIBUTE_KEY).get();
                if (handshakeResponse == null) continue;

                for (final ChannelFuture otherFuture : futures) {
                    if (otherFuture != future) {
                        otherFuture.cancel(true);

                        if (otherFuture.isSuccess()) {
                            final Channel otherChannel = otherFuture.channel();
                            otherChannel.close();
                        }
                    }
                }

                if (reconnect) {
                    fileRequests.checksums();
                }

                if (whenReconnected != null) {
                    whenReconnected.accept(channel);
                }

                return channel;
            }

            if (whileWaiting != null) {
                whileWaiting.run();
            }

            Thread.yield();
        }

        if (Thread.interrupted()) {
            throw new FileClientConnectInterruptedException("Thread was interrupted during connect");
        }

        return createChannelFuture(reconnect, whileWaiting, whenReconnected,
                Math.min(timeoutNanos + DEFAULT_TIMEOUT_NANOS, MAX_TIMEOUT_NANOS));
    }

    public Channel connect(final boolean reconnect,
                           @Nullable final Runnable whileWaiting,
                           @Nullable final Consumer<Channel> whenReconnected) {
        final Channel channel = createChannelFuture(
                reconnect,
                whileWaiting,
                whenReconnected,
                DEFAULT_TIMEOUT_NANOS);
        this.channel = channel;
        return channel;
    }

    public Channel getConnectedChannel() {
        Channel channel = this.channel;
        if (channel == null) {
            return null;
        } else if (channel.isActive()) {
            return channel;
        }

        // await the full reconnect before resuming our thread...
        return connect(true, null, whenReconnected);
    }

}
