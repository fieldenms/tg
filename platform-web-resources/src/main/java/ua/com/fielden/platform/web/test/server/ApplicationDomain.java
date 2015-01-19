package ua.com.fielden.platform.web.test.server;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.domain.PlatformDomainTypes;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.sample.domain.TgPersistentEntityWithInteger;
import ua.com.fielden.platform.sample.domain.TgPersistentEntityWithMoney;
import ua.com.fielden.platform.sample.domain.TgPerson;

/**
 * A temporary class to enlist domain entities for Web UI Testing Server.
 *
 * @author TG Team
 *
 */
public class ApplicationDomain implements IApplicationDomainProvider {
    private static final List<Class<? extends AbstractEntity<?>>> entityTypes = new ArrayList<Class<? extends AbstractEntity<?>>>();
    private static final List<Class<? extends AbstractEntity<?>>> domainTypes = new ArrayList<Class<? extends AbstractEntity<?>>>();

    private static void add(final Class<? extends AbstractEntity<?>> domainType) {
        entityTypes.add(domainType);
        domainTypes.add(domainType);
    }

    static {
        entityTypes.addAll(PlatformDomainTypes.types);
        add(TgPerson.class);
        add(TgPersistentEntityWithInteger.class);
        add(TgPersistentEntityWithMoney.class);
    }

    @Override
    public List<Class<? extends AbstractEntity<?>>> entityTypes() {
        return entityTypes;
    }

    public List<Class<? extends AbstractEntity<?>>> domainTypes() {
        return domainTypes;
    }
}
