package org.jire.swiftfup.client;

import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.kqueue.KQueue;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.channel.kqueue.KQueueSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.jetbrains.annotations.Nullable;

import java.net.SocketAddress;
import java.util.function.Consumer;

/**
 * @author Jire
 */
public interface FileClientGroup {

    EventLoopGroup getEventLoopGroup();

    Class<? extends Channel> getChannelClass();

    default FileClient createClient(FileRequests fileRequests,
                                    @Nullable Consumer<Channel> whenReconnected,
                                    SocketAddress... remoteAddresses) {
        return new FileClient(
                fileRequests,
                getEventLoopGroup(),
                getChannelClass(),
                whenReconnected,
                remoteAddresses);
    }

    default EventLoopGroup createEventLoopGroup(int threads) {
        return Epoll.isAvailable() ? new EpollEventLoopGroup(threads)
                : KQueue.isAvailable() ? new KQueueEventLoopGroup(threads)
                : new NioEventLoopGroup(threads);
    }

    default Class<? extends Channel> createChannelClass(EventLoopGroup group) {
        return group instanceof EpollEventLoopGroup ? EpollSocketChannel.class
                : group instanceof KQueueEventLoopGroup ? KQueueSocketChannel.class
                : NioSocketChannel.class;
    }

}
