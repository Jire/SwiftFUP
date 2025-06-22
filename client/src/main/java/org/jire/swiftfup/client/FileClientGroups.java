package org.jire.swiftfup.client;

import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;

/**
 * @author Jire
 */
public enum FileClientGroups implements FileClientGroup {

    ONE_THREAD {
        @Override
        public EventLoopGroup createEventLoopGroup() {
            return createEventLoopGroup(1);
        }
    },
    MANY_THREADS {
        @Override
        public EventLoopGroup createEventLoopGroup() {
            return createEventLoopGroup(0);
        }
    };

    private final EventLoopGroup eventLoopGroup = createEventLoopGroup();
    private final Class<? extends Channel> channelClass = createChannelClass();

    protected abstract EventLoopGroup createEventLoopGroup();

    @Override
    public EventLoopGroup getEventLoopGroup() {
        return eventLoopGroup;
    }

    @Override
    public Class<? extends Channel> getChannelClass() {
        return channelClass;
    }

}
