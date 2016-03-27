package ua.com.fielden.web;

import org.hibernate.SessionFactory;

import com.google.common.base.Ticker;
import com.google.common.cache.Cache;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

import ua.com.fielden.platform.attachment.IAttachment;
import ua.com.fielden.platform.dao.AttachmentDao;
import ua.com.fielden.platform.dao.DomainMetadata;
import ua.com.fielden.platform.dao.ISecurityRoleAssociationDao;
import ua.com.fielden.platform.dao.IUserAndRoleAssociationDao;
import ua.com.fielden.platform.dao.IUserRoleDao;
import ua.com.fielden.platform.entity.query.IdOnlyProxiedEntityTypeCache;
import ua.com.fielden.platform.ioc.CommonFactoryModule;
import ua.com.fielden.platform.security.ISecurityRoleAssociationBatchAction;
import ua.com.fielden.platform.security.IUserAndRoleAssociationBatchAction;
import ua.com.fielden.platform.security.SecurityRoleAssociationBatchActionDao;
import ua.com.fielden.platform.security.UserAndRoleAssociationBatchActionDao;
import ua.com.fielden.platform.security.annotations.PasswordHashingKey;
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
import ua.com.fielden.platform.security.provider.UserDao;
import ua.com.fielden.platform.security.provider.UserRoleTokensUpdaterDao;
import ua.com.fielden.platform.security.provider.UserRolesUpdaterDao;
import ua.com.fielden.platform.security.session.IUserSession;
import ua.com.fielden.platform.security.session.UserSession;
import ua.com.fielden.platform.security.session.UserSessionDao;
import ua.com.fielden.platform.security.user.ISecurityTokenInfo;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.IUserRoleTokensUpdater;
import ua.com.fielden.platform.security.user.IUserRolesUpdater;
import ua.com.fielden.platform.serialisation.api.ISerialisationClassProvider;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.api.ISerialiser0;
import ua.com.fielden.platform.serialisation.api.impl.Serialiser;
import ua.com.fielden.platform.serialisation.api.impl.Serialiser0;
import ua.com.fielden.platform.test.UserProviderForTesting;
import ua.com.fielden.platform.test.ioc.PlatformTestServerModule.TestSessionCacheBuilder;
import ua.com.fielden.platform.test.ioc.TickerForSessionCache;
import ua.com.fielden.platform.test.ioc.UniversalConstantsForTesting;
import ua.com.fielden.platform.utils.IUniversalConstants;
import ua.com.fielden.web.entities.IInspectedEntityDao;
import ua.com.fielden.web.entities.InspectedEntityDao;

/**
 * Guice injector module for Hibernate related injections, which are specific to testing.
 *
 * @author TG Team
 *
 */
public class WebHibernateModule extends CommonFactoryModule {

    private final ISerialisationClassProvider serialisationClassProvider;

    public WebHibernateModule(//
    final SessionFactory sessionFactory,//
            final DomainMetadata domainMetadata,//
            final IdOnlyProxiedEntityTypeCache idOnlyProxiedEntityTypeCache, 
            final ISerialisationClassProvider serialisationClassProvider) {
        super(sessionFactory, domainMetadata, idOnlyProxiedEntityTypeCache);
        this.serialisationClassProvider = serialisationClassProvider;
    }

    @Override
    protected void configure() {
        super.configure();
        bind(IUserProvider.class).to(UserProviderForTesting.class).in(Scopes.SINGLETON);
        bind(ISerialisationClassProvider.class).toInstance(serialisationClassProvider);
        bind(ISerialiser0.class).to(Serialiser0.class);
        bind(ISerialiser.class).to(Serialiser.class);
        // bind DAO
        bind(IInspectedEntityDao.class).to(InspectedEntityDao.class);
        bind(IUserRoleDao.class).to(UserRoleDao.class);
        bind(IUserRoleTokensUpdater.class).to(UserRoleTokensUpdaterDao.class);
        bind(ISecurityTokenInfo.class).to(SecurityTokenInfoDao.class);
        bind(IUserAndRoleAssociationDao.class).to(UserAndRoleAssociationDao.class);
        bind(ISecurityRoleAssociationDao.class).to(SecurityRoleAssociationDao.class);
        bind(IUser.class).to(UserDao.class);
        bind(IUserRolesUpdater.class).to(UserRolesUpdaterDao.class);
        bind(ISecurityTokenController.class).to(SecurityTokenController.class);
        bindConstant().annotatedWith(Names.named("attachments.location")).to(".");
        bind(IAttachment.class).to(AttachmentDao.class);
        bind(IUserAndRoleAssociationBatchAction.class).to(UserAndRoleAssociationBatchActionDao.class);
        bind(ISecurityRoleAssociationBatchAction.class).to(SecurityRoleAssociationBatchActionDao.class);
        
        bind(IUserSession.class).to(UserSessionDao.class);
        bindConstant().annotatedWith(SessionHashingKey.class).to("This is a hasing key, which is used to hash session data in unit tests.");
        bindConstant().annotatedWith(PasswordHashingKey.class).to("This is a hasing key, which is used to hash user passwords in unit tests.");
        bindConstant().annotatedWith(TrustedDeviceSessionDuration.class).to(60 * 24 * 3); // three days
        bindConstant().annotatedWith(UntrustedDeviceSessionDuration.class).to(5); // 5 minutes

        bind(Ticker.class).to(TickerForSessionCache.class).in(Scopes.SINGLETON);
        bind(IUniversalConstants.class).to(UniversalConstantsForTesting.class).in(Scopes.SINGLETON);
        bind(new TypeLiteral<Cache<String, UserSession>>(){}).annotatedWith(SessionCache.class).toProvider(TestSessionCacheBuilder.class).in(Scopes.SINGLETON);

    }
}
