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
    public final ChannelGroup allChannels;
    private ServerBootstrap bootstrap;
    private Channel serverChannel;

    public ServerTeltonika(final String host, final int port, final ChannelGroup allChannels, final IGpsHandlerFactory handlerFactory) {
	this.host = host;
	this.port = port;
	this.handlerFactory = handlerFactory;
	this.allChannels = allChannels;
    }

    @Override
    public void run() {
	if (bootstrap != null) {
	    throw new IllegalArgumentException("Server has already been run.");
	}
	log.info("\tNetty GPS server starting...");
	// Configure the server.
	bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));
	bootstrap.setOption("child.keepAlive", false);

	// Set up the pipeline factory.
	bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
	    @Override
	    public ChannelPipeline getPipeline() throws Exception {
		log.info("New pipe is requested.");
		return Channels.pipeline(new TransparentEncoder(), new AvlFrameDecoder(), handlerFactory.create());
	    }
	});

	// Bind and start to accept incoming connections.
	serverChannel = bootstrap.bind(new InetSocketAddress(host, port));
	allChannels.add(serverChannel);
	log.info("\tNetty GPS server started on " + host + ":" + port);
    }

    public void shutdown() {
	log.info("Shutdown initiated...");
	serverChannel.close();
	allChannels.close();
	log.info("Channels closed.");
	bootstrap.releaseExternalResources();
	log.info("External resources released.");
    }
}
