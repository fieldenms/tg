package ua.com.fielden.platform.gis.gps.server;

import static java.lang.String.format;

import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.group.ChannelGroup;

import ua.com.fielden.platform.gis.gps.AbstractAvlMachine;
import ua.com.fielden.platform.gis.gps.AbstractAvlMessage;
import ua.com.fielden.platform.gis.gps.AbstractAvlModule;
import ua.com.fielden.platform.gis.gps.AvlData;
import ua.com.fielden.platform.gis.gps.IMessageHandler;
import ua.com.fielden.platform.gis.gps.IModuleLookup;
import ua.com.fielden.platform.gis.gps.Option;

public class ServerTeltonikaHandler<
	MESSAGE extends AbstractAvlMessage,
	MACHINE extends AbstractAvlMachine<MESSAGE>,
	MODULE extends AbstractAvlModule
> extends SimpleChannelUpstreamHandler {

    private static final byte LOGIN_DENY = 0x0;
    private static final byte LOGIN_ALLOW = 0x1;

    private final ChannelGroup allChannels;
    private final ConcurrentHashMap<String, Channel> existingConnections;

    private String imei;
    private MODULE module;
    private final Logger log = Logger.getLogger(ServerTeltonikaHandler.class);
    private final ChannelBuffer ack = ChannelBuffers.buffer(4);
    private final IModuleLookup<MODULE> moduleLookup;
    private final IMessageHandler<MODULE> messageHandler;

    public ServerTeltonikaHandler(final ConcurrentHashMap<String, Channel> existingConnections, final ChannelGroup allChannels, final IModuleLookup<MODULE> moduleLookup, final IMessageHandler<MODULE> messageHandler) {
	this.existingConnections = existingConnections;
	this.allChannels = allChannels;
	this.moduleLookup = moduleLookup;
	this.messageHandler = messageHandler;
    }

    @Override
    public void messageReceived(final ChannelHandlerContext ctx, final MessageEvent e) throws Exception {
	final Object msg = e.getMessage();
	if (msg instanceof String) {
	    setImei((String) msg);
	    final Channel prevChannel = existingConnections.get(getImei());
	    if (prevChannel != null && prevChannel != ctx.getChannel()) { // need to close previous channel
		log.debug(format("Attempting to close previous connection for IMEI[%s]", getImei()));
		try {
		    allChannels.remove(prevChannel);
		    prevChannel.close().awaitUninterruptibly();

		} catch (final Exception ex) {
		    log.warn(format("Life sucks and previous connection for IMEI %s could not be closed.", getImei()));
		}
	    }
	    existingConnections.put(getImei(), ctx.getChannel());
	    // IMEI
	    handleLogin(ctx, getImei()); // process the initial handshake that result is successful or unsuccessful IMEI recognition
	} else if (msg instanceof AvlData[]) { // AVL data array
	    handleData(ctx, getModule(), (AvlData[]) msg);
	} else {
	    super.messageReceived(ctx, e);
	}
    }

    @Override
    public void channelConnected(final ChannelHandlerContext ctx, final ChannelStateEvent e) throws Exception {
	super.channelConnected(ctx, e);
	allChannels.add(ctx.getChannel());
	log.debug("Client channel connected.");
    }

    @Override
    public void channelDisconnected(final ChannelHandlerContext ctx, final ChannelStateEvent e) throws Exception {
	log.debug("Originating channel has disconnected.");
	allChannels.remove(ctx.getChannel());
	super.channelDisconnected(ctx, e);
    }

    @Override
    public void channelClosed(final ChannelHandlerContext ctx, final ChannelStateEvent e) throws Exception {
	handleLogoff(ctx, 0);

	super.channelClosed(ctx, e);
	log.debug("Client channel closed.");
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final ExceptionEvent e) {
	log.error("-- Exception!\n");
	log.error(e.getCause() + "\n");
	final StackTraceElement[] elem = e.getCause().getStackTrace();
	for (final StackTraceElement stackTraceElement : elem) {
	    log.error("\t" + stackTraceElement.toString() + "\n");
	}
	log.debug("Closing client channel...");
	final Channel channel = e.getChannel();
	channel.close();
    }

    private void handleLogin(final ChannelHandlerContext ctx, final String imei) {
	log.debug("Logging in client [" + imei + "].");
	final Channel channel = ctx.getChannel();
	final ChannelBuffer msg = ChannelBuffers.buffer(1);
	try {
	    final Option<MODULE> module = moduleLookup.get(imei);
	    if (module.hasValue()) {
		log.debug("Authorised IMEI [" + imei + "].");
		msg.writeByte(LOGIN_ALLOW);
		setImei(imei);
		setModule(module.value());
	    } else {
		log.warn("Unrecognised IMEI [" + imei + "].");
		msg.writeByte(LOGIN_DENY);
		//channel.close(); // FIXME relies on multiplexer to close the channel
	    }
	} finally {
	    channel.write(msg);
	}
    }

    private void handleLogoff(final ChannelHandlerContext ctx, final Integer reason) {
	log.debug("Logging off client [" + getImei() + "].");
    }

    private void handleData(final ChannelHandlerContext ctx, final MODULE module, final AvlData[] data) {
	final Channel channel = ctx.getChannel();
	log.debug("Received GPS data from IMEI [" + getImei() + "]");
	final int count = data.length;
	log.debug("AVL data count = [" + count + "]");

	messageHandler.handle(module, data);

	ack.resetWriterIndex();
	ack.writeInt(count);
	channel.write(ack);
    }

    public String getImei() {
	return imei;
    }

    private void setImei(final String deviceId) {
	this.imei = deviceId;
    }

    public MODULE getModule() {
	return module;
    }

    private void setModule(final MODULE module) {
	this.module = module;
    }
}
