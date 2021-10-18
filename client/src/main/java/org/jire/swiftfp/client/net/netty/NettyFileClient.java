package org.jire.swiftfp.client.net.netty;

import io.netty.bootstrap.Bootstrap;
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
import org.jire.swiftfp.client.FileChecksumRequester;
import org.jire.swiftfp.client.FileChecksumsRequest;
import org.jire.swiftfp.client.FileRequest;
import org.jire.swiftfp.client.FileRequester;
import org.jire.swiftfp.client.crc32.CRC32FileResponder;
import org.jire.swiftfp.client.crc32.CRC32FileResponse;
import org.jire.swiftfp.client.net.AbstractFileClient;

/**
 * @author Jire
 */
public class NettyFileClient
		<RESPONSE extends CRC32FileResponse,
				REQUESTER extends FileRequester<RESPONSE> & FileChecksumRequester>
		extends AbstractFileClient<RESPONSE, REQUESTER> {
	
	private final Bootstrap bootstrap;
	
	private Channel channel;
	
	public NettyFileClient(String host, int port,
	                       REQUESTER fileRequester,
	                       CRC32FileResponder<RESPONSE> fileResponder,
	                       int eventLoopThreads) {
		super(host, port, fileRequester);
		
		EventLoopGroup eventLoopGroup = Epoll.isAvailable() ? new EpollEventLoopGroup(eventLoopThreads)
				: KQueue.isAvailable() ? new KQueueEventLoopGroup(eventLoopThreads)
				: new NioEventLoopGroup(eventLoopThreads);
		
		Class<? extends Channel> channelClass = Epoll.isAvailable() ? EpollSocketChannel.class
				: KQueue.isAvailable() ? KQueueSocketChannel.class
				: NioSocketChannel.class;
		bootstrap = new Bootstrap()
				.channel(channelClass)
				.group(eventLoopGroup)
				.option(ChannelOption.SO_KEEPALIVE, true)
				.option(ChannelOption.TCP_NODELAY, true)
				.handler(new FileClientChannelInitializer(fileRequester, fileResponder));
	}
	
	public Channel getChannel() {
		return channel;
	}
	
	public void setChannel(Channel channel) {
		this.channel = channel;
	}
	
	public Channel connect() {
		Channel channel = bootstrap
				.connect(getHost(), getPort())
				.syncUninterruptibly()
				.channel();
		setChannel(channel);
		return channel;
	}
	
	public Channel ensureConnected() {
		Channel channel = getChannel();
		return channel == null || !channel.isOpen() ? connect() : channel;
	}
	
	public Channel flush() {
		Channel channel = getChannel();
		if (channel != null && channel.isOpen())
			channel.flush();
		return channel;
	}
	
	@Override
	public void request(FileRequest<RESPONSE> request) {
		Channel channel = ensureConnected();
		channel.write(request, channel.voidPromise());
	}
	
	@Override
	public void requestChecksums(FileChecksumsRequest request) {
		Channel channel = ensureConnected();
		channel.writeAndFlush(request, channel.voidPromise());
	}
	
}
