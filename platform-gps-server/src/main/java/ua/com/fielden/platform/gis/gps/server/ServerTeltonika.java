package ua.com.fielden.platform.gis.gps.server;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import ua.com.fielden.platform.gis.gps.factory.IGpsHandlerFactory;

public class ServerTeltonika implements Runnable {

	private final Logger log = Logger.getLogger(ServerTeltonika.class);
	private final String host;
	private final int port;
	private final IGpsHandlerFactory handlerFactory;
	public static final ChannelGroup allChannels = new DefaultChannelGroup("gps-server");

	public ServerTeltonika(final String host, final int port, final IGpsHandlerFactory handlerFactory) {
		this.host = host;
		this.port = port;
		this.handlerFactory = handlerFactory;
	}

	@Override
	public void run() {
		// Configure the server.
		final ServerBootstrap bootstrap = new ServerBootstrap(
				new NioServerSocketChannelFactory(
						Executors.newCachedThreadPool(),
						Executors.newCachedThreadPool()));
		bootstrap.setOption("child.keepAlive", false);

		// Set up the pipeline factory.
		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
			@Override
			public ChannelPipeline getPipeline() throws Exception {
			    System.out.println("new pipe is requested");
				return Channels.pipeline(
						new TransparentEncoder(),
						new AvlFrameDecoder(),
						handlerFactory.create());
			}
		});

		// Bind and start to accept incoming connections.
		final Channel channel = bootstrap.bind(new InetSocketAddress(host, port));
		allChannels.add(channel);
		log.info("Server started on " + host + ":" + port);
	}
}
