package org.jire.swiftfup.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.kqueue.KQueue;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.channel.kqueue.KQueueSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * @author Jire
 */
public final class FileClient {

    private final FileRequests fileRequests;
    private final Bootstrap bootstrap;

    private volatile Channel channel;

    public FileClient(String host, int port,
                      FileRequests fileRequests) {
        this.fileRequests = fileRequests;

        int netThreads = 1;
        final EventLoopGroup group
                = Epoll.isAvailable() ? new EpollEventLoopGroup(netThreads)
                : KQueue.isAvailable() ? new KQueueEventLoopGroup(netThreads)
                : new NioEventLoopGroup(netThreads);
        final Class<? extends Channel> channelClass
                = group instanceof EpollEventLoopGroup ? EpollSocketChannel.class
                : group instanceof KQueueEventLoopGroup ? KQueueSocketChannel.class
                : NioSocketChannel.class;
        bootstrap = new Bootstrap()
                .channel(channelClass)
                .group(group)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 120_000)
                .remoteAddress(host, port);
        try {
            // unsupported on older Windows versions (2003 and XP)
            bootstrap.option(ChannelOption.IP_TOS, 0b100_000_10);
        } catch (final Exception e) {
            e.printStackTrace();
        }
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

    public ChannelFuture createChannelFuture(final boolean reconnect) {
        final ChannelHandler handler =
                new FileClientChannelInitializer(getFileRequests(), reconnect);

        final Bootstrap bootstrap = this.bootstrap;
        return bootstrap
                .handler(handler)
                .connect();
    }

    public Channel connect(final boolean reconnect) {
        final Channel channel = createChannelFuture(reconnect)
                .syncUninterruptibly()
                .channel();

        setChannel(channel);
        return channel;
    }

    public Channel connect() {
        return connect(false);
    }

    public Channel connectedChannel() {
        Channel channel = getChannel();
        return isConnected(channel) ? channel : connect(true);
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
