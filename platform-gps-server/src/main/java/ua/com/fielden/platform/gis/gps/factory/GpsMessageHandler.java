package ua.com.fielden.platform.gis.gps.factory;

import ua.com.fielden.platform.gis.gps.AbstractAvlMachine;
import ua.com.fielden.platform.gis.gps.AbstractAvlMessage;
import ua.com.fielden.platform.gis.gps.AvlData;
import ua.com.fielden.platform.gis.gps.IMessageHandler;
import ua.com.fielden.platform.gis.gps.actors.AbstractAvlMachineActor;
import ua.com.fielden.platform.gis.gps.actors.AbstractActors;

public class GpsMessageHandler<T extends AbstractAvlMessage, M extends AbstractAvlMachine<T>, N extends AbstractAvlMachineActor<T, M>> implements IMessageHandler<T, M> {
    private final AbstractActors<T, M, N> actors;

    public GpsMessageHandler(final AbstractActors<T, M, N> actors) {
	this.actors = actors;
    }

    @Override
    public IMessageHandler<T, M> handle(final M machine, final AvlData[] data) {
	actors.dataReceived(machine, data);
	return this;
    }
}