package ua.com.fielden.platform.serialisation.api.impl;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.serialisation.api.ISerialisationClassProvider;
import ua.com.fielden.platform.serialisation.api.ISerialiserEngine;

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
