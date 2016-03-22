package ua.com.fielden.platform.serialisation.xstream;

import ua.com.fielden.platform.entity.factory.EntityFactory;

import com.google.inject.Inject;

/**
 * Client side XML serialisation implementation.
 * 
 * 
 * @author TG Team
 * 
 */
@Deprecated
public class ClientSerialiser extends XStreamSerialiser {
    private final EntityFactory factory;

    @Deprecated
    public ClientSerialiser(final EntityFactory factory, final boolean compact) {
        super(compact);
        this.factory = factory;
        registerConverter(new ClientEntityConverter(this.factory));
    }

    @Deprecated
    @Inject
    public ClientSerialiser(final EntityFactory factory) {
        this(factory, true);
    }

    @Override
    public EntityFactory factory() {
        return factory;
    }
}
