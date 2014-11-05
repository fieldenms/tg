package ua.com.fielden.platform.gis.gps.factory;

import ua.com.fielden.platform.gis.gps.AbstractAvlModule;
import ua.com.fielden.platform.gis.gps.AvlData;
import ua.com.fielden.platform.gis.gps.IMessageHandler;
import ua.com.fielden.platform.gis.gps.actors.AbstractActors;

public class GpsMessageHandler<MODULE extends AbstractAvlModule> implements IMessageHandler {
    private final AbstractActors<?, ?, MODULE, ?, ?, ?, ?> actors;

    public GpsMessageHandler(final AbstractActors<?, ?, MODULE, ?, ?, ?, ?> actors) {
        this.actors = actors;
    }

    @Override
    public IMessageHandler handle(final String imei, final AvlData[] data) {
        actors.dataReceived(imei, data);
        return this;
    }
}