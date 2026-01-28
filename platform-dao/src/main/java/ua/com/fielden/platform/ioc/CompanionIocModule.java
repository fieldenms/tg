package ua.com.fielden.platform.ioc;

import ua.com.fielden.platform.companion.EntityCompanionGenerationIocModule;
import ua.com.fielden.platform.dao.CommonEntityAggregatesDao;
import ua.com.fielden.platform.dao.EntityAggregatesDao;
import ua.com.fielden.platform.dao.IEntityAggregatesOperations;
import ua.com.fielden.platform.domain.PlatformDomainTypes;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.query.IEntityAggregates;
import ua.com.fielden.platform.menu.Action;
import ua.com.fielden.platform.menu.UserMenuInvisibilityAssociationBatchActionCo;
import ua.com.fielden.platform.menu.UserMenuInvisibilityAssociationBatchActionDao;
import ua.com.fielden.platform.ref_hierarchy.AbstractTreeEntry;
import ua.com.fielden.platform.reflection.CompanionObjectAutobinder;

import java.util.List;
import java.util.Properties;

import static ua.com.fielden.platform.reflection.CompanionObjectAutobinder.bindCo;

/// Module responsible for everything related to entity companion objects.
///
public class CompanionIocModule extends CommonFactoryIocModule {

    private final List<Class<? extends AbstractEntity<?>>> domainEntityTypes;

    /**
     * @param domainEntityTypes  domain entity types that have an explicit companion (i.e., annotated with {@link CompanionObject})
     */
    public CompanionIocModule(final Properties props,
                              final List<Class<? extends AbstractEntity<?>>> domainEntityTypes) {
        super(props);
        this.domainEntityTypes = domainEntityTypes;
    }

    @Override
    protected void configure() {
        super.configure();

        bindDomainCompanionObjects(domainEntityTypes);
        bindPlatformCompanionObjects();

        bind(IEntityAggregatesOperations.class).to(EntityAggregatesDao.class);
        bind(IEntityAggregates.class).to(CommonEntityAggregatesDao.class);

        bind(UserMenuInvisibilityAssociationBatchActionCo.class).to(UserMenuInvisibilityAssociationBatchActionDao.class);
        install(new EntityCompanionGenerationIocModule());
    }

    protected void bindDomainCompanionObjects(final List<Class<? extends AbstractEntity<?>>> domainEntityTypes) {
        for (final Class<? extends AbstractEntity<?>> entityType : domainEntityTypes) {
            CompanionObjectAutobinder.bindCo(entityType, binder());
        }
    }

    protected void bindPlatformCompanionObjects() {
        PlatformDomainTypes.typesNotDependentOnWebUI.stream()
                // skip entity types that have no companions
                .filter(type -> !AbstractTreeEntry.class.isAssignableFrom(type) && !Action.class.isAssignableFrom(type))
                .forEach(type -> bindCo(type, binder()));
    }

}
