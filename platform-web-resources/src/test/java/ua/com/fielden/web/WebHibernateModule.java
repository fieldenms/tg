package ua.com.fielden.web;

import org.hibernate.SessionFactory;

import com.google.common.base.Ticker;
import com.google.common.cache.Cache;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

import ua.com.fielden.platform.attachment.AttachmentDao;
import ua.com.fielden.platform.attachment.IAttachment;
import ua.com.fielden.platform.dao.ISecurityRoleAssociation;
import ua.com.fielden.platform.dao.IUserAndRoleAssociation;
import ua.com.fielden.platform.dao.IUserRole;
import ua.com.fielden.platform.entity.functional.master.AcknowledgeWarningsDao;
import ua.com.fielden.platform.entity.functional.master.IAcknowledgeWarnings;
import ua.com.fielden.platform.entity.functional.master.IPropertyWarning;
import ua.com.fielden.platform.entity.functional.master.PropertyWarningDao;
import ua.com.fielden.platform.entity.query.IdOnlyProxiedEntityTypeCache;
import ua.com.fielden.platform.entity.query.metadata.DomainMetadata;
import ua.com.fielden.platform.ioc.CommonFactoryModule;
import ua.com.fielden.platform.security.ISecurityRoleAssociationBatchAction;
import ua.com.fielden.platform.security.IUserAndRoleAssociationBatchAction;
import ua.com.fielden.platform.security.SecurityRoleAssociationBatchActionDao;
import ua.com.fielden.platform.security.UserAndRoleAssociationBatchActionDao;
import ua.com.fielden.platform.security.annotations.SessionCache;
import ua.com.fielden.platform.security.annotations.SessionHashingKey;
import ua.com.fielden.platform.security.annotations.TrustedDeviceSessionDuration;
import ua.com.fielden.platform.security.annotations.UntrustedDeviceSessionDuration;
import ua.com.fielden.platform.security.dao.SecurityRoleAssociationDao;
import ua.com.fielden.platform.security.dao.UserAndRoleAssociationDao;
import ua.com.fielden.platform.security.dao.UserRoleDao;
import ua.com.fielden.platform.security.provider.ISecurityTokenController;
import ua.com.fielden.platform.security.provider.SecurityTokenController;
import ua.com.fielden.platform.security.provider.SecurityTokenInfoDao;
import ua.com.fielden.platform.security.provider.UserRoleTokensUpdaterDao;
import ua.com.fielden.platform.security.provider.UserRolesUpdaterDao;
import ua.com.fielden.platform.security.session.IUserSession;
import ua.com.fielden.platform.security.session.UserSession;
import ua.com.fielden.platform.security.session.UserSessionDao;
import ua.com.fielden.platform.security.user.INewUserNotifier;
import ua.com.fielden.platform.security.user.ISecurityTokenInfo;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.IUserRoleTokensUpdater;
import ua.com.fielden.platform.security.user.IUserRolesUpdater;
import ua.com.fielden.platform.security.user.IUserSecret;
import ua.com.fielden.platform.security.user.UserDao;
import ua.com.fielden.platform.security.user.UserSecretDao;
import ua.com.fielden.platform.serialisation.api.ISerialisationClassProvider;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.api.impl.Serialiser;
import ua.com.fielden.platform.test.UserProviderForTesting;
import ua.com.fielden.platform.test.ioc.DatesForTesting;
import ua.com.fielden.platform.test.ioc.PlatformTestServerModule.TestSessionCacheBuilder;
import ua.com.fielden.platform.test.ioc.TickerForSessionCache;
import ua.com.fielden.platform.test.ioc.UniversalConstantsForTesting;
import ua.com.fielden.platform.ui.config.api.EntityCentreConfigCo;
import ua.com.fielden.platform.ui.config.api.IEntityLocatorConfig;
import ua.com.fielden.platform.ui.config.api.IEntityMasterConfig;
import ua.com.fielden.platform.ui.config.controller.EntityCentreConfigDao;
import ua.com.fielden.platform.ui.config.controller.EntityLocatorConfigDao;
import ua.com.fielden.platform.ui.config.controller.EntityMasterConfigDao;
import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.utils.IUniversalConstants;

/**
 * Guice injector module for Hibernate related injections, which are specific to testing.
 *
 * @author TG Team
 *
 */
public class WebHibernateModule extends CommonFactoryModule {
    private final ISerialisationClassProvider serialisationClassProvider;

    public WebHibernateModule(
        final SessionFactory sessionFactory,
        final DomainMetadata domainMetadata,
        final IdOnlyProxiedEntityTypeCache idOnlyProxiedEntityTypeCache,
        final ISerialisationClassProvider serialisationClassProvider
    ) {
        super(sessionFactory, domainMetadata, idOnlyProxiedEntityTypeCache);
        this.serialisationClassProvider = serialisationClassProvider;
    }

    @Override
    protected void configure() {
        super.configure();
        bind(IUserProvider.class).to(UserProviderForTesting.class).in(Scopes.SINGLETON);
        bind(INewUserNotifier.class).toInstance(s -> {});
        bind(ISerialisationClassProvider.class).toInstance(serialisationClassProvider);
        bind(ISerialiser.class).to(Serialiser.class);

        bind(IUserRole.class).to(UserRoleDao.class);
        bind(IUserRoleTokensUpdater.class).to(UserRoleTokensUpdaterDao.class);
        bind(ISecurityTokenInfo.class).to(SecurityTokenInfoDao.class);
        bind(IUserAndRoleAssociation.class).to(UserAndRoleAssociationDao.class);
        bind(ISecurityRoleAssociation.class).to(SecurityRoleAssociationDao.class);
        bind(IUser.class).to(UserDao.class);
        bind(IUserSecret.class).to(UserSecretDao.class);
        bind(IUserRolesUpdater.class).to(UserRolesUpdaterDao.class);
        bind(ISecurityTokenController.class).to(SecurityTokenController.class);
        bind(IAcknowledgeWarnings.class).to(AcknowledgeWarningsDao.class);
        bind(IPropertyWarning.class).to(PropertyWarningDao.class);
        bindConstant().annotatedWith(Names.named("attachments.location")).to(".");
        bind(IAttachment.class).to(AttachmentDao.class);
        bind(IUserAndRoleAssociationBatchAction.class).to(UserAndRoleAssociationBatchActionDao.class);
        bind(ISecurityRoleAssociationBatchAction.class).to(SecurityRoleAssociationBatchActionDao.class);
        bind(EntityCentreConfigCo.class).to(EntityCentreConfigDao.class);
        bind(IEntityLocatorConfig.class).to(EntityLocatorConfigDao.class);
        bind(IEntityMasterConfig.class).to(EntityMasterConfigDao.class);
        bind(IUserSession.class).to(UserSessionDao.class);
        bindConstant().annotatedWith(SessionHashingKey.class).to("This is a hasing key, which is used to hash session data in unit tests.");
        bindConstant().annotatedWith(TrustedDeviceSessionDuration.class).to(60 * 24 * 3); // three days
        bindConstant().annotatedWith(UntrustedDeviceSessionDuration.class).to(5); // 5 minutes

        bind(Ticker.class).to(TickerForSessionCache.class).in(Scopes.SINGLETON);
        bind(IDates.class).to(DatesForTesting.class).in(Scopes.SINGLETON);
        bind(IUniversalConstants.class).to(UniversalConstantsForTesting.class).in(Scopes.SINGLETON);
        bind(new TypeLiteral<Cache<String, UserSession>>(){}).annotatedWith(SessionCache.class).toProvider(TestSessionCacheBuilder.class).in(Scopes.SINGLETON);
    }

}