package org.jire.swiftfup.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.AbstractChannel;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
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
		EventLoopGroup eventLoopGroup
				= Epoll.isAvailable() ? new EpollEventLoopGroup(netThreads)
				: KQueue.isAvailable() ? new KQueueEventLoopGroup(netThreads)
				: new NioEventLoopGroup(netThreads);
		Class<? extends AbstractChannel> channelClass
				= Epoll.isAvailable() ? EpollSocketChannel.class
				: KQueue.isAvailable() ? KQueueSocketChannel.class
				: NioSocketChannel.class;
		bootstrap = new Bootstrap()
				.channel(channelClass)
				.group(eventLoopGroup)
				.option(ChannelOption.TCP_NODELAY, true)
				.remoteAddress(host, port);
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
	
	public Channel connect(boolean reconnect) {
		Channel channel = bootstrap
				.handler(new FileClientChannelInitializer(getFileRequests(), reconnect))
				.connect().syncUninterruptibly().channel();
		
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
