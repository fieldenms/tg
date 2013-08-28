package ua.com.fielden.platform.gis.gps.server;

import org.apache.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

import ua.com.fielden.platform.gis.gps.AbstractAvlMachine;
import ua.com.fielden.platform.gis.gps.AbstractAvlMessage;
import ua.com.fielden.platform.gis.gps.AvlData;
import ua.com.fielden.platform.gis.gps.IMachineLookup;
import ua.com.fielden.platform.gis.gps.IMessageHandler;
import ua.com.fielden.platform.gis.gps.Option;

public class ServerTeltonikaHandler<T extends AbstractAvlMessage, M extends AbstractAvlMachine<T>> extends SimpleChannelUpstreamHandler {

    private static final byte LOGIN_DENY = 0x0;
    private static final byte LOGIN_ALLOW = 0x1;

    private String imei;
    private M machine;
    private final Logger log = Logger.getLogger(ServerTeltonikaHandler.class);
    private final ChannelBuffer ack = ChannelBuffers.buffer(4);
    private final IMachineLookup<T, M> machineLookup;
    private final IMessageHandler<T, M> messageHandler;

    public ServerTeltonikaHandler(final IMachineLookup<T, M> machineLookup, final IMessageHandler<T, M> messageHandler) {
	this.machineLookup = machineLookup;
	this.messageHandler = messageHandler;
    }

//    @Override
//    public void handleUpstream(final ChannelHandlerContext ctx, final ChannelEvent e) throws Exception {
//	if (e instanceof ChannelStateEvent) {
//	    // logger.info(e.toString());
//	}
//	super.handleUpstream(ctx, e);
//	// System.out.print("event:" + e.toString() + "\n");
//    }

    @Override
    public void channelConnected(final ChannelHandlerContext ctx, final ChannelStateEvent e) throws Exception {
	super.channelConnected(ctx, e);
	log.debug("Client channel connected.");
    }

    @Override
    public void messageReceived(final ChannelHandlerContext ctx, final MessageEvent e) throws Exception {
	final Object msg = e.getMessage();
	if (msg instanceof String) {
	    handleLogin(ctx, (String) msg); // process the initial handshake that result is successful or unsuccessful IMEI recognition
	} else if (msg instanceof AvlData[]) { // AVL data array
	    handleData(ctx, getMachine(), (AvlData[]) msg);
	} else {
	    super.messageReceived(ctx, e);
	}
    }

    @Override
    public void channelDisconnected(final ChannelHandlerContext ctx, final ChannelStateEvent e) throws Exception {
	log.debug("Client channel disconnected.");
    }

    @Override
    public void channelClosed(final ChannelHandlerContext ctx, final ChannelStateEvent e) throws Exception {
	handleLogoff(ctx, 0);

	super.channelClosed(ctx, e);
	log.info("Client channel closed.");
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
	    final Option<M> machine = machineLookup.get(imei);
	    if (machine.hasValue()) {
		log.info("Authorised IMEI [" + imei + "].");
		msg.writeByte(LOGIN_ALLOW);
		setImei(imei);
		setMachine(machine.value());
	    } else {
		log.warn("Unrecognised IMEI [" + imei + "].");
		msg.writeByte(LOGIN_DENY);
		channel.close();
	    }
	} finally {
	    channel.write(msg);
	}
    }

    private void handleLogoff(final ChannelHandlerContext ctx, final Integer reason) {
	log.debug("Logging off client [" + getImei() + "].");
    }

    private void handleData(final ChannelHandlerContext ctx, final M machine, final AvlData[] data) {
	final Channel channel = ctx.getChannel();
	log.debug("Received GPS data from IMEI [" + getImei() + "]");
	final int count = data.length;
	log.debug("AVL data count = [" + count + "]");

	messageHandler.handle(machine, data);

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

    public M getMachine() {
	return machine;
    }

    private void setMachine(final M machine) {
	this.machine = machine;
    }
}
