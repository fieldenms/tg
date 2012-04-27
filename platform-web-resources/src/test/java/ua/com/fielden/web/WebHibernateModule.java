package ua.com.fielden.web;

import org.hibernate.SessionFactory;

import ua.com.fielden.platform.attachment.IAttachmentController;
import ua.com.fielden.platform.dao.AttachmentDao;
import ua.com.fielden.platform.dao.DomainPersistenceMetadata;
import ua.com.fielden.platform.dao.ISecurityRoleAssociationDao;
import ua.com.fielden.platform.dao.IUserAndRoleAssociationDao;
import ua.com.fielden.platform.dao.IUserRoleDao;
import ua.com.fielden.platform.ioc.CommonFactoryModule;
import ua.com.fielden.platform.security.dao.SecurityRoleAssociationDao;
import ua.com.fielden.platform.security.dao.UserAndRoleAssociationDao;
import ua.com.fielden.platform.security.dao.UserRoleDao;
import ua.com.fielden.platform.security.provider.ISecurityTokenController;
import ua.com.fielden.platform.security.provider.IUserController;
import ua.com.fielden.platform.security.provider.SecurityTokenController;
import ua.com.fielden.platform.security.provider.UserController;
import ua.com.fielden.platform.security.user.IUserDao;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.impl.ISerialisationClassProvider;
import ua.com.fielden.platform.serialisation.impl.TgKryo;
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
	    final DomainPersistenceMetadata domainPersistenceMetadata,//
	    final ISerialisationClassProvider serialisationClassProvider) {
	super(sessionFactory, domainPersistenceMetadata);
	this.serialisationClassProvider = serialisationClassProvider;
    }

    @Override
    protected void configure() {
	super.configure();
	bind(ISerialisationClassProvider.class).toInstance(serialisationClassProvider);
	bind(ISerialiser.class).to(TgKryo.class);
	// bind DAO
	bind(IInspectedEntityDao.class).to(InspectedEntityDao.class);
	bind(IUserRoleDao.class).to(UserRoleDao.class);
	bind(IUserAndRoleAssociationDao.class).to(UserAndRoleAssociationDao.class);
	bind(ISecurityRoleAssociationDao.class).to(SecurityRoleAssociationDao.class);
	bind(IUserController.class).to(UserController.class); // UserControllerForTestPurposes.class
	bind(IUserDao.class).to(UserController.class);
	bind(ISecurityTokenController.class).to(SecurityTokenController.class);
	bind(IAttachmentController.class).to(AttachmentDao.class);
    }
}
