package ua.com.fielden.platform.gis.gps.factory;

import java.util.concurrent.ConcurrentHashMap;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelUpstreamHandler;
import org.jboss.netty.channel.group.ChannelGroup;

import ua.com.fielden.platform.gis.gps.AbstractAvlMachine;
import ua.com.fielden.platform.gis.gps.AbstractAvlMachineModuleTemporalAssociation;
import ua.com.fielden.platform.gis.gps.AbstractAvlMessage;
import ua.com.fielden.platform.gis.gps.AbstractAvlModule;
import ua.com.fielden.platform.gis.gps.actors.AbstractActors;
import ua.com.fielden.platform.gis.gps.actors.AbstractAvlMachineActor;
import ua.com.fielden.platform.gis.gps.actors.AbstractAvlModuleActor;
import ua.com.fielden.platform.gis.gps.actors.AbstractViolatingMessageResolverActor;
import ua.com.fielden.platform.gis.gps.server.ServerTeltonikaHandler;

public class DefaultGpsHandlerFactory<MESSAGE extends AbstractAvlMessage, MACHINE extends AbstractAvlMachine<MESSAGE>, MODULE extends AbstractAvlModule, ASSOCIATION extends AbstractAvlMachineModuleTemporalAssociation<MESSAGE, MACHINE, MODULE>, MACHINE_ACTOR extends AbstractAvlMachineActor<MESSAGE, MACHINE>, MODULE_ACTOR extends AbstractAvlModuleActor<MESSAGE, MACHINE, MODULE, ASSOCIATION>, VIO_RESOLVER_ACTOR extends AbstractViolatingMessageResolverActor<MESSAGE>> implements IGpsHandlerFactory {
    private final AbstractActors<MESSAGE, MACHINE, MODULE, ASSOCIATION, MACHINE_ACTOR, MODULE_ACTOR, VIO_RESOLVER_ACTOR> actors;
    private final ChannelGroup allChannels;
    private final ConcurrentHashMap<String, Channel> existingConnections;

    public DefaultGpsHandlerFactory(final ConcurrentHashMap<String, Channel> existingConnections, final ChannelGroup allChannels, final AbstractActors<MESSAGE, MACHINE, MODULE, ASSOCIATION, MACHINE_ACTOR, MODULE_ACTOR, VIO_RESOLVER_ACTOR> actors) {
        this.existingConnections = existingConnections;
        this.allChannels = allChannels;
        this.actors = actors;
    }

    @Override
    public ChannelUpstreamHandler create() {
        return new ServerTeltonikaHandler<MESSAGE, MACHINE, MODULE>(existingConnections, allChannels, actors, new GpsMessageHandler<MODULE>(actors));
    }
}