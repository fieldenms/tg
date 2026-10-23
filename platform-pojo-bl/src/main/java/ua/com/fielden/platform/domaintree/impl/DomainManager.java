package ua.com.fielden.platform.domaintree.impl;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.domaintree.IDomainManager;
import ua.com.fielden.platform.domaintree.IDomainTreeEnhancer;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.security.user.User;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/// This implementation of [IDomainManager] creates [IDomainTreeEnhancer] for a user when first requested and retains it.
///
@Singleton
public class DomainManager implements IDomainManager {

    private final ConcurrentMap<String, IDomainTreeEnhancer> domainTreeEnhancers;
    private final EntityFactory entityFactory;
    private final IApplicationDomainProvider appDomainProvider;

    @Inject
    protected DomainManager(final EntityFactory entityFactory, final IApplicationDomainProvider appDomainProvider) {
        this.entityFactory = entityFactory;
        this.appDomainProvider = appDomainProvider;
        this.domainTreeEnhancers = new ConcurrentHashMap<>();
    }

    @Override
    public IDomainTreeEnhancer domainTreeEnhancer(final User user) {
        return domainTreeEnhancers.computeIfAbsent(user.getKey(), _ -> new DomainTreeEnhancer(entityFactory, Set.copyOf(appDomainProvider.entityTypes())));
    }

}
