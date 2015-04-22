package ua.com.fielden.web;

import org.hibernate.SessionFactory;

import ua.com.fielden.platform.attachment.IAttachment;
import ua.com.fielden.platform.dao.AttachmentDao;
import ua.com.fielden.platform.dao.DomainMetadata;
import ua.com.fielden.platform.dao.ISecurityRoleAssociationDao;
import ua.com.fielden.platform.dao.IUserAndRoleAssociationDao;
import ua.com.fielden.platform.dao.IUserRoleDao;
import ua.com.fielden.platform.ioc.CommonFactoryModule;
import ua.com.fielden.platform.security.IUserAndRoleAssociationBatchAction;
import ua.com.fielden.platform.security.UserAndRoleAssociationBatchActionDao;
import ua.com.fielden.platform.security.dao.SecurityRoleAssociationDao;
import ua.com.fielden.platform.security.dao.UserAndRoleAssociationDao;
import ua.com.fielden.platform.security.dao.UserRoleDao;
import ua.com.fielden.platform.security.provider.ISecurityTokenController;
import ua.com.fielden.platform.security.provider.IUserController;
import ua.com.fielden.platform.security.provider.SecurityTokenController;
import ua.com.fielden.platform.security.provider.UserController;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.serialisation.api.ISerialisationClassProvider;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.api.ISerialiser0;
import ua.com.fielden.platform.serialisation.api.impl.Serialiser;
import ua.com.fielden.platform.serialisation.api.impl.Serialiser0;
import ua.com.fielden.platform.test.UserProviderForTesting;
import ua.com.fielden.web.entities.IInspectedEntityDao;
import ua.com.fielden.web.entities.InspectedEntityDao;

import com.google.inject.Scopes;
import com.google.inject.name.Names;

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
            final ISerialisationClassProvider serialisationClassProvider) {
        super(sessionFactory, domainMetadata);
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
        bind(IUserAndRoleAssociationDao.class).to(UserAndRoleAssociationDao.class);
        bind(ISecurityRoleAssociationDao.class).to(SecurityRoleAssociationDao.class);
        bind(IUserController.class).to(UserController.class); // UserControllerForTestPurposes.class
        bind(IUser.class).to(UserController.class);
        bind(ISecurityTokenController.class).to(SecurityTokenController.class);
        bindConstant().annotatedWith(Names.named("attachments.location")).to(".");
        bind(IAttachment.class).to(AttachmentDao.class);
        bind(IUserAndRoleAssociationBatchAction.class).to(UserAndRoleAssociationBatchActionDao.class);
    }
}
