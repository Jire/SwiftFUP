package org.jire.swiftfup.client;

import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.IoHandlerFactory;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollIoHandler;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.kqueue.KQueue;
import io.netty.channel.kqueue.KQueueIoHandler;
import io.netty.channel.kqueue.KQueueSocketChannel;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.channel.uring.IoUring;
import io.netty.channel.uring.IoUringIoHandler;
import io.netty.channel.uring.IoUringSocketChannel;
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

    default IoHandlerFactory createIoHandlerFactory() {
        return IoUring.isAvailable() ? IoUringIoHandler.newFactory()
                : Epoll.isAvailable() ? EpollIoHandler.newFactory()
                : KQueue.isAvailable() ? KQueueIoHandler.newFactory()
                : NioIoHandler.newFactory();
    }

    default EventLoopGroup createEventLoopGroup(int threads) {
        return new MultiThreadIoEventLoopGroup(threads, createIoHandlerFactory());
    }

    default Class<? extends Channel> createChannelClass() {
        return IoUring.isAvailable() ? IoUringSocketChannel.class
                : Epoll.isAvailable() ? EpollSocketChannel.class
                : KQueue.isAvailable() ? KQueueSocketChannel.class
                : NioSocketChannel.class;
    }

}
