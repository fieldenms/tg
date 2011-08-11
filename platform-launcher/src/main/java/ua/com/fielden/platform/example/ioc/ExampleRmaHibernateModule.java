package ua.com.fielden.platform.example.ioc;

import org.hibernate.SessionFactory;

import ua.com.fielden.platform.dao.EntityAggregatesDao;
import ua.com.fielden.platform.dao.IDaoFactory;
import ua.com.fielden.platform.dao.IEntityAggregatesDao;
import ua.com.fielden.platform.dao.ISecurityRoleAssociationDao;
import ua.com.fielden.platform.dao.IUserAndRoleAssociationDao;
import ua.com.fielden.platform.dao.IUserRoleDao;
import ua.com.fielden.platform.dao.MappingExtractor;
import ua.com.fielden.platform.dao.SnappyDao;
import ua.com.fielden.platform.entity.matcher.IValueMatcherFactory;
import ua.com.fielden.platform.entity.matcher.ValueMatcherFactory;
import ua.com.fielden.platform.example.entities.IAdviceController;
import ua.com.fielden.platform.example.entities.IAdviceDao;
import ua.com.fielden.platform.example.entities.IBogieClassDao;
import ua.com.fielden.platform.example.entities.IBogieDao;
import ua.com.fielden.platform.example.entities.ICompletionCertificateController;
import ua.com.fielden.platform.example.entities.ICompletionCertificateDao;
import ua.com.fielden.platform.example.entities.IFWorkOrderDao;
import ua.com.fielden.platform.example.entities.IPersonDao;
import ua.com.fielden.platform.example.entities.IRotableClassDao;
import ua.com.fielden.platform.example.entities.IRotableDao;
import ua.com.fielden.platform.example.entities.IRotableWorkspaceController;
import ua.com.fielden.platform.example.entities.IWagonClassDao;
import ua.com.fielden.platform.example.entities.IWagonDao;
import ua.com.fielden.platform.example.entities.IWheelsetClassDao;
import ua.com.fielden.platform.example.entities.IWheelsetDao;
import ua.com.fielden.platform.example.entities.IWorkorderDao;
import ua.com.fielden.platform.example.entities.IWorkshopDao;
import ua.com.fielden.platform.example.entities.daos.AdviceController;
import ua.com.fielden.platform.example.entities.daos.AdviceDao;
import ua.com.fielden.platform.example.entities.daos.BogieClassDao;
import ua.com.fielden.platform.example.entities.daos.BogieDao;
import ua.com.fielden.platform.example.entities.daos.CompletionCertificateController;
import ua.com.fielden.platform.example.entities.daos.CompletionCertificateDao;
import ua.com.fielden.platform.example.entities.daos.FWorkOrderDao;
import ua.com.fielden.platform.example.entities.daos.PersonDao;
import ua.com.fielden.platform.example.entities.daos.RotableClassDao;
import ua.com.fielden.platform.example.entities.daos.RotableDao;
import ua.com.fielden.platform.example.entities.daos.RotableWorkspaceController;
import ua.com.fielden.platform.example.entities.daos.UserController;
import ua.com.fielden.platform.example.entities.daos.WagonClassDao;
import ua.com.fielden.platform.example.entities.daos.WagonDao;
import ua.com.fielden.platform.example.entities.daos.WheelsetClassDao;
import ua.com.fielden.platform.example.entities.daos.WheelsetDao;
import ua.com.fielden.platform.example.entities.daos.WorkorderDao;
import ua.com.fielden.platform.example.entities.daos.WorkshopDao;
import ua.com.fielden.platform.ioc.CommonFactoryModule;
import ua.com.fielden.platform.keygen.IKeyNumberGenerator;
import ua.com.fielden.platform.keygen.KeyNumberDao;
import ua.com.fielden.platform.security.dao.SecurityRoleAssociationDao;
import ua.com.fielden.platform.security.dao.UserAndRoleAssociationDao;
import ua.com.fielden.platform.security.dao.UserRoleDao;
import ua.com.fielden.platform.security.provider.ISecurityTokenController;
import ua.com.fielden.platform.security.provider.IUserController;
import ua.com.fielden.platform.security.provider.SecurityTokenController;
import ua.com.fielden.platform.serialisation.ClientSerialiser;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.snappy.ISnappyDao;
import ua.com.fielden.platform.swing.review.configuration.LocalCenterConfigurationController;
import ua.com.fielden.platform.swing.review.configuration.LocalLocatorConfigurationController;
import ua.com.fielden.platform.swing.review.configuration.LocalMasterConfigurationController;
import ua.com.fielden.platform.ui.config.api.interaction.ICenterConfigurationController;
import ua.com.fielden.platform.ui.config.api.interaction.ILocatorConfigurationController;
import ua.com.fielden.platform.ui.config.api.interaction.IMasterConfigurationController;

import com.google.inject.Scopes;
import com.google.inject.name.Names;

/**
 * Guice injector module for Hibernate related injections, which are specific to PNL (including RMA). This class is present here only for platform testing purposes. It should not
 * be used outside of platform tests
 *
 * @author TG Team
 *
 */
public class ExampleRmaHibernateModule extends CommonFactoryModule {

    public ExampleRmaHibernateModule(final SessionFactory sessionFactory, final MappingExtractor mappingExtractor) {
	super(sessionFactory, mappingExtractor, null);
    }

    @Override
    protected void configure() {
	super.configure();
	//bind constants
	bindConstant().annotatedWith(Names.named("reports.path")).to("resources/criterias/");

	// bind DAO and business logic controllers
	bind(IKeyNumberGenerator.class).to(KeyNumberDao.class).in(Scopes.SINGLETON);
	bind(IPersonDao.class).to(PersonDao.class).in(Scopes.SINGLETON);
	bind(IBogieDao.class).to(BogieDao.class).in(Scopes.SINGLETON);
	bind(IWheelsetDao.class).to(WheelsetDao.class).in(Scopes.SINGLETON);
	bind(IRotableDao.class).to(RotableDao.class).in(Scopes.SINGLETON);
	bind(IWorkshopDao.class).to(WorkshopDao.class).in(Scopes.SINGLETON);
	bind(IWagonClassDao.class).to(WagonClassDao.class).in(Scopes.SINGLETON);
	bind(IBogieClassDao.class).to(BogieClassDao.class).in(Scopes.SINGLETON);
	bind(IWheelsetClassDao.class).to(WheelsetClassDao.class).in(Scopes.SINGLETON);
	bind(IWagonDao.class).to(WagonDao.class).in(Scopes.SINGLETON);
	bind(IWorkorderDao.class).to(WorkorderDao.class).in(Scopes.SINGLETON);
	bind(IAdviceDao.class).to(AdviceDao.class).in(Scopes.SINGLETON);
	bind(ICompletionCertificateDao.class).to(CompletionCertificateDao.class).in(Scopes.SINGLETON);
	bind(IRotableClassDao.class).to(RotableClassDao.class).in(Scopes.SINGLETON);
	bind(ICompletionCertificateController.class).to(CompletionCertificateController.class).in(Scopes.SINGLETON);
	bind(IAdviceController.class).to(AdviceController.class).in(Scopes.SINGLETON);
	bind(IRotableWorkspaceController.class).to(RotableWorkspaceController.class).in(Scopes.SINGLETON);
	bind(IEntityAggregatesDao.class).to(EntityAggregatesDao.class).in(Scopes.SINGLETON);
	bind(IFWorkOrderDao.class).to(FWorkOrderDao.class).in(Scopes.SINGLETON);

	// user nad security management
	bind(IUserRoleDao.class).to(UserRoleDao.class).in(Scopes.SINGLETON);
	bind(IUserAndRoleAssociationDao.class).to(UserAndRoleAssociationDao.class).in(Scopes.SINGLETON);
	bind(ISecurityRoleAssociationDao.class).to(SecurityRoleAssociationDao.class).in(Scopes.SINGLETON);

	bind(IUserController.class).to(UserController.class).in(Scopes.SINGLETON);
	bind(ISecurityTokenController.class).to(SecurityTokenController.class).in(Scopes.SINGLETON);

	// bind DaoFactory to support dynamic query criteria
	bind(IDaoFactory.class).toInstance(getDaoFactory());

	// bind snappy access objects :
	bind(ISnappyDao.class).to(SnappyDao.class);

	// bind value matcher factory to support autocompleters
	bind(IValueMatcherFactory.class).toInstance(new ValueMatcherFactory(getDaoFactory(), getEntityFactory()));

	//bind serialiser
	bind(ISerialiser.class).to(ClientSerialiser.class);

	//bind configuration controllers
	bind(ICenterConfigurationController.class).to(LocalCenterConfigurationController.class);
	bind(ILocatorConfigurationController.class).to(LocalLocatorConfigurationController.class);
	bind(IMasterConfigurationController.class).to(LocalMasterConfigurationController.class);
    }
}
