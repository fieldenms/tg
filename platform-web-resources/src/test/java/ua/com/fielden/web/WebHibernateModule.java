package ua.com.fielden.web;

import org.hibernate.SessionFactory;

import ua.com.fielden.platform.attachment.IAttachmentController;
import ua.com.fielden.platform.dao.AttachmentDao;
import ua.com.fielden.platform.dao.EntityAggregatesDao;
import ua.com.fielden.platform.dao.IEntityAggregatesDao;
import ua.com.fielden.platform.dao.ISecurityRoleAssociationDao;
import ua.com.fielden.platform.dao.IUserAndRoleAssociationDao;
import ua.com.fielden.platform.dao.IUserRoleDao;
import ua.com.fielden.platform.dao.MappingExtractor;
import ua.com.fielden.platform.dao2.MappingsGenerator;
import ua.com.fielden.platform.ioc.CommonFactoryModule;
import ua.com.fielden.platform.security.dao.SecurityRoleAssociationDao;
import ua.com.fielden.platform.security.dao.UserAndRoleAssociationDao;
import ua.com.fielden.platform.security.dao.UserRoleDao;
import ua.com.fielden.platform.security.provider.ISecurityTokenController;
import ua.com.fielden.platform.security.provider.IUserController;
import ua.com.fielden.platform.security.provider.SecurityTokenController;
import ua.com.fielden.platform.security.provider.UserController;
import ua.com.fielden.platform.security.user.IUserDao;
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
public class WebHibernateModule extends CommonFactoryModule {

    public WebHibernateModule(final SessionFactory sessionFactory, final MappingExtractor mappingExtractor, final MappingsGenerator mappingsGenerator) {
	super(sessionFactory, mappingExtractor, mappingsGenerator);
    }

    @Override
    protected void configure() {
	super.configure();
	bind(ISerialiser.class).to(ServerSerialiser.class);
	// bind DAO
	bind(IEntityAggregatesDao.class).to(EntityAggregatesDao.class);
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
