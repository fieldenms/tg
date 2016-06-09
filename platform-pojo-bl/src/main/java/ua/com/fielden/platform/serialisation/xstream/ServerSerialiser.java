package ua.com.fielden.platform.serialisation.xstream;

import ua.com.fielden.platform.entity.factory.EntityFactory;

import com.google.inject.Inject;

/**
 * Server side XML serialisation implementation.
 * 
 * 
 * @author TG Team
 * 
 */
@Deprecated
public class ServerSerialiser extends XStreamSerialiser {
    private final EntityFactory factory;

    @Deprecated
    public ServerSerialiser(final EntityFactory factory, final boolean compact) {
        super(compact);
        this.factory = factory;
        registerConverter(new ServerEntityConverter(this.factory));
    }

    @Deprecated
    @Inject
    public ServerSerialiser(final EntityFactory factory) {
        this(factory, true);
    }

    @Override
    public EntityFactory factory() {
        return factory;
    }
}
