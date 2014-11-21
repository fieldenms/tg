package ua.com.fielden.platform.domaintree.testing;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.serialisation.api.ISerialisationClassProvider;
import ua.com.fielden.platform.serialisation.api.ISerialiserEngine;
import ua.com.fielden.platform.serialisation.api.Serialiser;

import com.google.inject.Inject;

public class SerialiserForDomainTreesTestingPurposes extends Serialiser {

    @Inject
    public SerialiserForDomainTreesTestingPurposes(final EntityFactory factory, final ISerialisationClassProvider provider) {
        super(factory, provider);
    }

    @Override
    protected ISerialiserEngine createTgKryo(final EntityFactory factory, final ISerialisationClassProvider provider) {
        return new TgKryoForDomainTreesTestingPurposes(factory, provider, this);
    }
}
