package ua.com.fielden.platform.domaintree.impl;

import java.util.concurrent.ConcurrentHashMap;

import ua.com.fielden.platform.domaintree.IGlobalDomainTreeManager;
import ua.com.fielden.platform.domaintree.IServerGlobalDomainTreeManager;

import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * The server global domain tree manager implementation.
 *
 * @author TG Team
 *
 */
public class ServerGlobalDomainTreeManager implements IServerGlobalDomainTreeManager {
    private final Provider<IGlobalDomainTreeManager> gdtmProvider;
    private final ConcurrentHashMap<String, IGlobalDomainTreeManager> managersByUser;

    @Inject
    public ServerGlobalDomainTreeManager(final Provider<IGlobalDomainTreeManager> gdtmProvider) {
        this.gdtmProvider = gdtmProvider;
        this.managersByUser = new ConcurrentHashMap<>();
    }

    @Override
    public IGlobalDomainTreeManager get(final String username) {
        // lazy initialisation using computeIfAbsent and 'name -> gdtmProvider.get()' is required not to reinitialise gdtm every time after putting it into concurrent hash map
        return managersByUser.computeIfAbsent(username, name -> gdtmProvider.get());
    }
}
