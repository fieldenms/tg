package ua.com.fielden.platform.serialisation.api.impl;

import ua.com.fielden.platform.domaintree.IDomainTreeEnhancerCache;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.serialisation.api.ISerialisationClassProvider;
import ua.com.fielden.platform.serialisation.api.ISerialiserEngine;

import com.google.inject.Inject;

@Deprecated
public class Serialiser0ForDomainTreesTestingPurposes extends Serialiser0 {

    @Inject
    @Deprecated
    public Serialiser0ForDomainTreesTestingPurposes(final EntityFactory factory, final ISerialisationClassProvider provider, final IDomainTreeEnhancerCache domainTreeEnhancerCache) {
        super(factory, provider, domainTreeEnhancerCache);
    }

    @Override
    protected ISerialiserEngine createTgKryo(final EntityFactory factory, final ISerialisationClassProvider provider, final IDomainTreeEnhancerCache domainTreeEnhancerCache) {
        return new TgKryo0ForDomainTreesTestingPurposes(factory, provider, this);
    }
}
