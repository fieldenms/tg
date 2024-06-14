package ua.com.fielden.platform.ioc;

import com.google.inject.Singleton;
import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.dao.CommonEntityAggregatesDao;
import ua.com.fielden.platform.dao.EntityAggregatesDao;
import ua.com.fielden.platform.dao.IEntityAggregatesOperations;
import ua.com.fielden.platform.domain.PlatformDomainTypes;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.DefaultCompanionObjectFinderImpl;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.query.IEntityAggregates;
import ua.com.fielden.platform.menu.Action;
import ua.com.fielden.platform.menu.UserMenuInvisibilityAssociationBatchActionCo;
import ua.com.fielden.platform.menu.UserMenuInvisibilityAssociationBatchActionDao;
import ua.com.fielden.platform.ref_hierarchy.AbstractTreeEntry;
import ua.com.fielden.platform.reflection.CompanionObjectAutobinder;
import ua.com.fielden.platform.security.ISecurityRoleAssociationBatchAction;
import ua.com.fielden.platform.security.IUserAndRoleAssociationBatchAction;
import ua.com.fielden.platform.security.SecurityRoleAssociationBatchActionDao;
import ua.com.fielden.platform.security.UserAndRoleAssociationBatchActionDao;

import java.util.Properties;

import static ua.com.fielden.platform.reflection.CompanionObjectAutobinder.bindCo;

/**
 * Module responsible for everything related to entity companion objects.
 */
public class CompanionModule extends CommonFactoryModule {

    private final IApplicationDomainProvider appDomainProvider;

    public CompanionModule(final Properties props, final IApplicationDomainProvider appDomainProvider) {
        super(props);
        this.appDomainProvider = appDomainProvider;
    }

    @Override
    protected void configure() {
        super.configure();

        bind(ICompanionObjectFinder.class).to(DefaultCompanionObjectFinderImpl.class).in(Singleton.class);
        bindDomainCos(appDomainProvider);
        bindPlatformCos();

        bind(IEntityAggregatesOperations.class).to(EntityAggregatesDao.class);
        bind(IEntityAggregates.class).to(CommonEntityAggregatesDao.class);

        bind(IUserAndRoleAssociationBatchAction.class).to(UserAndRoleAssociationBatchActionDao.class);
        bind(UserMenuInvisibilityAssociationBatchActionCo.class).to(UserMenuInvisibilityAssociationBatchActionDao.class);
        bind(ISecurityRoleAssociationBatchAction.class).to(SecurityRoleAssociationBatchActionDao.class);
    }

    protected void bindDomainCos(final IApplicationDomainProvider appDomainProvider) {
        for (final Class<? extends AbstractEntity<?>> entityType : appDomainProvider.entityTypes()) {
            CompanionObjectAutobinder.bindCo(entityType, binder());
        }
    }

    protected void bindPlatformCos() {
        PlatformDomainTypes.typesNotDependentOnWebUI.stream()
                // skip entity types that have no companions
                .filter(type -> !AbstractTreeEntry.class.isAssignableFrom(type) && !Action.class.isAssignableFrom(type))
                .forEach(type -> bindCo(type, binder()));
    }

}
