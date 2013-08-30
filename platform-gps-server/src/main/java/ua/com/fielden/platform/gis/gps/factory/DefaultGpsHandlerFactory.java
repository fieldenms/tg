package ua.com.fielden.platform.gis.gps.factory;

import org.jboss.netty.channel.ChannelUpstreamHandler;

import ua.com.fielden.platform.gis.gps.AbstractAvlMachine;
import ua.com.fielden.platform.gis.gps.AbstractAvlMessage;
import ua.com.fielden.platform.gis.gps.IMachineLookup;
import ua.com.fielden.platform.gis.gps.actors.AbstractActors;
import ua.com.fielden.platform.gis.gps.actors.AbstractAvlMachineActor;
import ua.com.fielden.platform.gis.gps.server.ServerTeltonikaHandler;

public class DefaultGpsHandlerFactory<T extends AbstractAvlMessage, M extends AbstractAvlMachine<T>, N extends AbstractAvlMachineActor<T, M>> implements IGpsHandlerFactory {
    private final IMachineLookup<T, M> machineLookup;
    private final AbstractActors<T, M, N> actors;

    public DefaultGpsHandlerFactory(final AbstractActors<T, M, N> actors) {
	this.actors = actors;
	machineLookup = new MachineLookupDao<T, M>(actors.getCache());
    }

    @Override
    public ChannelUpstreamHandler create() {
	return new ServerTeltonikaHandler<T, M>(machineLookup, new GpsMessageHandler<T, M, N>(actors));
    }
}