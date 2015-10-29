package ua.com.fielden.platform.web.test.server;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.domain.PlatformDomainTypes;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.sample.domain.TgEntityForColourMaster;
import ua.com.fielden.platform.sample.domain.TgCentreInvokerWithCentreContext;
import ua.com.fielden.platform.sample.domain.TgExportFunctionalEntity;
import ua.com.fielden.platform.sample.domain.TgFetchProviderTestEntity;
import ua.com.fielden.platform.sample.domain.TgFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.sample.domain.TgIRStatusActivationFunctionalEntity;
import ua.com.fielden.platform.sample.domain.TgISStatusActivationFunctionalEntity;
import ua.com.fielden.platform.sample.domain.TgONStatusActivationFunctionalEntity;
import ua.com.fielden.platform.sample.domain.TgPersistentCompositeEntity;
import ua.com.fielden.platform.sample.domain.TgPersistentEntityWithProperties;
import ua.com.fielden.platform.sample.domain.TgPersistentStatus;
import ua.com.fielden.platform.sample.domain.TgPerson;
import ua.com.fielden.platform.sample.domain.TgSRStatusActivationFunctionalEntity;
import ua.com.fielden.platform.sample.domain.TgStatusActivationFunctionalEntity;

/**
 * A temporary class to enlist domain entities for Web UI Testing Server.
 *
 * @author TG Team
 *
 */
public class TgTestApplicationDomain implements IApplicationDomainProvider {
    private static final List<Class<? extends AbstractEntity<?>>> entityTypes = new ArrayList<Class<? extends AbstractEntity<?>>>();
    private static final List<Class<? extends AbstractEntity<?>>> domainTypes = new ArrayList<Class<? extends AbstractEntity<?>>>();

    private static void add(final Class<? extends AbstractEntity<?>> domainType) {
        entityTypes.add(domainType);
        domainTypes.add(domainType);
    }

    static {
        entityTypes.addAll(PlatformDomainTypes.types);
        add(TgPerson.class);
        add(TgPersistentEntityWithProperties.class);
        add(TgExportFunctionalEntity.class);
        add(TgPersistentCompositeEntity.class);
        add(TgFunctionalEntityWithCentreContext.class);
        add(TgStatusActivationFunctionalEntity.class);
        add(TgISStatusActivationFunctionalEntity.class);
        add(TgIRStatusActivationFunctionalEntity.class);
        add(TgONStatusActivationFunctionalEntity.class);
        add(TgSRStatusActivationFunctionalEntity.class);
        add(TgPersistentStatus.class);
        add(TgFetchProviderTestEntity.class);
        add(TgCentreInvokerWithCentreContext.class);
	add(TgEntityForColourMaster.class);
   }

    @Override
    public List<Class<? extends AbstractEntity<?>>> entityTypes() {
        return entityTypes;
    }

    public List<Class<? extends AbstractEntity<?>>> domainTypes() {
        return domainTypes;
    }
}
