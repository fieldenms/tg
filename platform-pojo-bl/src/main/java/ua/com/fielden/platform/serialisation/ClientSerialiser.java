package ua.com.fielden.platform.serialisation;

import ua.com.fielden.platform.entity.factory.EntityFactory;

import com.google.inject.Inject;

/**
 * Client side XML serialisation implementation.
 *
 *
 * @author TG Team
 *
 */
public class ClientSerialiser extends XStreamSerialiser {

    public ClientSerialiser(final EntityFactory factory, final boolean compact) {
	super(compact);
	registerConverter(new ClientEntityConverter(factory));
    }

    @Inject
    public ClientSerialiser(final EntityFactory factory) {
	this(factory, true);
    }
}
