package ua.com.fielden.platform.serialisation;

import ua.com.fielden.platform.entity.factory.EntityFactory;

import com.google.inject.Inject;

/**
 * Server side XML serialisation implementation.
 * 
 * 
 * @author TG Team
 * 
 */
public class ServerSerialiser extends XStreamSerialiser {
    private final EntityFactory factory;

    public ServerSerialiser(final EntityFactory factory, final boolean compact) {
        super(compact);
        this.factory = factory;
        registerConverter(new ServerEntityConverter(this.factory));
    }

    @Inject
    public ServerSerialiser(final EntityFactory factory) {
        this(factory, true);
    }

    @Override
    public EntityFactory factory() {
        return factory;
    }
}
