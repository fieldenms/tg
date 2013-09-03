package ua.com.fielden.platform.gis.gps.factory;

import java.util.concurrent.ConcurrentHashMap;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelUpstreamHandler;
import org.jboss.netty.channel.group.ChannelGroup;

import ua.com.fielden.platform.gis.gps.AbstractAvlMachine;
import ua.com.fielden.platform.gis.gps.AbstractAvlMessage;
import ua.com.fielden.platform.gis.gps.IMachineLookup;
import ua.com.fielden.platform.gis.gps.actors.AbstractActors;
import ua.com.fielden.platform.gis.gps.actors.AbstractAvlMachineActor;
import ua.com.fielden.platform.gis.gps.server.ServerTeltonikaHandler;

public class DefaultGpsHandlerFactory<T extends AbstractAvlMessage, M extends AbstractAvlMachine<T>, N extends AbstractAvlMachineActor<T, M>> implements IGpsHandlerFactory {
    private final IMachineLookup<T, M> machineLookup;
    private final AbstractActors<T, M, N> actors;
    private final ChannelGroup allChannels;
    private final ConcurrentHashMap<String, Channel> existingConnections;

    public DefaultGpsHandlerFactory(final ConcurrentHashMap<String, Channel> existingConnections, final ChannelGroup allChannels, final AbstractActors<T, M, N> actors) {
	this.existingConnections = existingConnections;
	this.allChannels = allChannels;
	this.actors = actors;
	machineLookup = new MachineLookupDao<T, M>(actors.getCache());
    }

    @Override
    public ChannelUpstreamHandler create() {
	return new ServerTeltonikaHandler<T, M>(existingConnections, allChannels, machineLookup, new GpsMessageHandler<T, M, N>(actors));
    }
}