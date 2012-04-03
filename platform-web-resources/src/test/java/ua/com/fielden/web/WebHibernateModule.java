package ua.com.fielden.web;

import org.hibernate.SessionFactory;

import ua.com.fielden.platform.attachment.IAttachmentController2;
import ua.com.fielden.platform.dao2.AttachmentDao2;
import ua.com.fielden.platform.dao2.DomainPersistenceMetadata;
import ua.com.fielden.platform.dao2.ISecurityRoleAssociationDao2;
import ua.com.fielden.platform.dao2.IUserAndRoleAssociationDao2;
import ua.com.fielden.platform.dao2.IUserRoleDao2;
import ua.com.fielden.platform.ioc.CommonFactoryModule2;
import ua.com.fielden.platform.security.dao.SecurityRoleAssociationDao2;
import ua.com.fielden.platform.security.dao.UserAndRoleAssociationDao2;
import ua.com.fielden.platform.security.dao.UserRoleDao2;
import ua.com.fielden.platform.security.provider.ISecurityTokenController;
import ua.com.fielden.platform.security.provider.IUserController2;
import ua.com.fielden.platform.security.provider.SecurityTokenController2;
import ua.com.fielden.platform.security.provider.UserController2;
import ua.com.fielden.platform.security.user.IUserDao2;
import ua.com.fielden.platform.serialisation.ServerSerialiser;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.web.entities.IInspectedEntityDao;
import ua.com.fielden.web.entities.InspectedEntityDao;

/**
 * Guice injector module for Hibernate related injections, which are specific to testing.
 *
 * @author TG Team
 *
 */
public class WebHibernateModule extends CommonFactoryModule2 {

    public WebHibernateModule(final SessionFactory sessionFactory, final DomainPersistenceMetadata domainPersistenceMetadata) {
	super(sessionFactory, domainPersistenceMetadata);
    }

    @Override
    protected void configure() {
	super.configure();
	bind(ISerialiser.class).to(ServerSerialiser.class);
	// bind DAO
	bind(IInspectedEntityDao.class).to(InspectedEntityDao.class);
	bind(IUserRoleDao2.class).to(UserRoleDao2.class);
	bind(IUserAndRoleAssociationDao2.class).to(UserAndRoleAssociationDao2.class);
	bind(ISecurityRoleAssociationDao2.class).to(SecurityRoleAssociationDao2.class);
	bind(IUserController2.class).to(UserController2.class); // UserControllerForTestPurposes.class
	bind(IUserDao2.class).to(UserController2.class);
	bind(ISecurityTokenController.class).to(SecurityTokenController2.class);
	bind(IAttachmentController2.class).to(AttachmentDao2.class);
    }
}
