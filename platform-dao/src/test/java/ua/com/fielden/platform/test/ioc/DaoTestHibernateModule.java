package ua.com.fielden.platform.test.ioc;

import org.hibernate.SessionFactory;

import ua.com.fielden.platform.dao.EntityAggregatesDao;
import ua.com.fielden.platform.dao.EntityWithMoneyDao;
import ua.com.fielden.platform.dao.IDaoFactory;
import ua.com.fielden.platform.dao.IEntityAggregatesDao;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dao.ISecurityRoleAssociationDao;
import ua.com.fielden.platform.dao.IUserAndRoleAssociationDao;
import ua.com.fielden.platform.dao.IUserRoleDao;
import ua.com.fielden.platform.dao.MappingExtractor;
import ua.com.fielden.platform.dao.filtering.DataFilter;
import ua.com.fielden.platform.dao2.MappingsGenerator;
import ua.com.fielden.platform.entity.matcher.development.IValueMatcherFactory;
import ua.com.fielden.platform.entity.matcher.development.ValueMatcherFactory;
import ua.com.fielden.platform.equery.interfaces.IFilter;
import ua.com.fielden.platform.ioc.CommonFactoryModule;
import ua.com.fielden.platform.keygen.IKeyNumberGenerator;
import ua.com.fielden.platform.keygen.KeyNumberDao;
import ua.com.fielden.platform.migration.controller.IMigrationErrorDao;
import ua.com.fielden.platform.migration.controller.IMigrationHistoryDao;
import ua.com.fielden.platform.migration.controller.IMigrationRunDao;
import ua.com.fielden.platform.migration.dao.MigrationErrorDao;
import ua.com.fielden.platform.migration.dao.MigrationHistoryDao;
import ua.com.fielden.platform.migration.dao.MigrationRunDao;
import ua.com.fielden.platform.persistence.types.EntityWithMoney;
import ua.com.fielden.platform.sample.domain.controller.ITgMeterReading;
import ua.com.fielden.platform.sample.domain.controller.ITgVehicleMake;
import ua.com.fielden.platform.sample.domain.controller.ITgVehicleModel;
import ua.com.fielden.platform.security.dao.SecurityRoleAssociationDao;
import ua.com.fielden.platform.security.dao.UserAndRoleAssociationDao;
import ua.com.fielden.platform.security.dao.UserRoleDao;
import ua.com.fielden.platform.security.provider.IUserController;
import ua.com.fielden.platform.security.provider.UserController;
import ua.com.fielden.platform.security.user.IUserDao;
import ua.com.fielden.platform.test.domain.entities.daos.AdviceDao;
import ua.com.fielden.platform.test.domain.entities.daos.BogieClassDao;
import ua.com.fielden.platform.test.domain.entities.daos.BogieDao;
import ua.com.fielden.platform.test.domain.entities.daos.IAdviceDao;
import ua.com.fielden.platform.test.domain.entities.daos.IBogieClassDao;
import ua.com.fielden.platform.test.domain.entities.daos.IBogieDao;
import ua.com.fielden.platform.test.domain.entities.daos.IRotableClassDao;
import ua.com.fielden.platform.test.domain.entities.daos.IRotableDao;
import ua.com.fielden.platform.test.domain.entities.daos.IWagonClassDao;
import ua.com.fielden.platform.test.domain.entities.daos.IWagonDao;
import ua.com.fielden.platform.test.domain.entities.daos.IWagonSlotDao;
import ua.com.fielden.platform.test.domain.entities.daos.IWheelsetClassDao;
import ua.com.fielden.platform.test.domain.entities.daos.IWheelsetDao;
import ua.com.fielden.platform.test.domain.entities.daos.IWorkorderDao;
import ua.com.fielden.platform.test.domain.entities.daos.IWorkshopDao;
import ua.com.fielden.platform.test.domain.entities.daos.RotableClassDao;
import ua.com.fielden.platform.test.domain.entities.daos.RotableDao;
import ua.com.fielden.platform.test.domain.entities.daos.TgMeterReadingDao;
import ua.com.fielden.platform.test.domain.entities.daos.TgVehicleMakeDao;
import ua.com.fielden.platform.test.domain.entities.daos.TgVehicleModelDao;
import ua.com.fielden.platform.test.domain.entities.daos.WagonClassDao;
import ua.com.fielden.platform.test.domain.entities.daos.WagonDao;
import ua.com.fielden.platform.test.domain.entities.daos.WagonSlotDao;
import ua.com.fielden.platform.test.domain.entities.daos.WheelsetClassDao;
import ua.com.fielden.platform.test.domain.entities.daos.WheelsetDao;
import ua.com.fielden.platform.test.domain.entities.daos.WorkorderDao;
import ua.com.fielden.platform.test.domain.entities.daos.WorkshopDao;
import ua.com.fielden.platform.test.entities.daos.IWorkorderableDao;
import ua.com.fielden.platform.test.entities.daos.WorkorderableDao;
import ua.com.fielden.platform.ui.config.api.IEntityCentreConfigController;
import ua.com.fielden.platform.ui.config.api.IEntityLocatorConfigController;
import ua.com.fielden.platform.ui.config.api.IEntityMasterConfigController;
import ua.com.fielden.platform.ui.config.api.IMainMenuItemController;
import ua.com.fielden.platform.ui.config.api.IMainMenuStructureBuilder;
import ua.com.fielden.platform.ui.config.controller.EntityCentreConfigControllerDao;
import ua.com.fielden.platform.ui.config.controller.EntityLocatorConfigControllerDao;
import ua.com.fielden.platform.ui.config.controller.EntityMasterConfigControllerDao;
import ua.com.fielden.platform.ui.config.controller.MainMenuItemControllerDao;
import ua.com.fielden.platform.ui.config.controller.mixin.PersistedMainMenuStructureBuilder;

import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;


/**
 * Guice injector module for Hibernate related injections for testing purposes.
 *
 * @author TG Team
 *
 */
public class DaoTestHibernateModule extends CommonFactoryModule {

    public DaoTestHibernateModule(final SessionFactory sessionFactory, final MappingExtractor mappingExtractor, final MappingsGenerator mappingsGenerator) {
	super(sessionFactory, mappingExtractor, mappingsGenerator);
    }

    @Override
    protected void configure() {
	super.configure();
	// bind DAO
	bind(IFilter.class).to(DataFilter.class);
	bind(IEntityAggregatesDao.class).to(EntityAggregatesDao.class);
	bind(IKeyNumberGenerator.class).to(KeyNumberDao.class);
	bind(IBogieDao.class).to(BogieDao.class);
	bind(IWheelsetDao.class).to(WheelsetDao.class);
	bind(IRotableDao.class).to(RotableDao.class);
	bind(IWorkshopDao.class).to(WorkshopDao.class);
	bind(IWagonClassDao.class).to(WagonClassDao.class);
	bind(IBogieClassDao.class).to(BogieClassDao.class);
	bind(IWheelsetClassDao.class).to(WheelsetClassDao.class);
	bind(IWagonDao.class).to(WagonDao.class);
	bind(IWagonSlotDao.class).to(WagonSlotDao.class);
	bind(IWorkorderDao.class).to(WorkorderDao.class);
	bind(IWorkorderableDao.class).to(WorkorderableDao.class);
	bind(IAdviceDao.class).to(AdviceDao.class);
	bind(IRotableClassDao.class).to(RotableClassDao.class);
	bind(IUserRoleDao.class).to(UserRoleDao.class);
	bind(IUserAndRoleAssociationDao.class).to(UserAndRoleAssociationDao.class);
	bind(ISecurityRoleAssociationDao.class).to(SecurityRoleAssociationDao.class);

	bind(IUserDao.class).to(UserController.class);
	bind(IUserController.class).to(UserController.class);
	bind(IEntityCentreConfigController.class).to(EntityCentreConfigControllerDao.class);
	bind(IEntityMasterConfigController.class).to(EntityMasterConfigControllerDao.class);
	bind(IEntityLocatorConfigController.class).to(EntityLocatorConfigControllerDao.class);
	bind(IMainMenuItemController.class).to(MainMenuItemControllerDao.class);
	bind(IMainMenuStructureBuilder.class).to(PersistedMainMenuStructureBuilder.class);

	bind(ITgVehicleModel.class).to(TgVehicleModelDao.class);
	bind(ITgVehicleMake.class).to(TgVehicleMakeDao.class);
	bind(ITgMeterReading.class).to(TgMeterReadingDao.class);
	bind(IMigrationErrorDao.class).to(MigrationErrorDao.class);
	bind(IMigrationRunDao.class).to(MigrationRunDao.class);
	bind(IMigrationHistoryDao.class).to(MigrationHistoryDao.class);

	bind(IDaoFactory.class).toInstance(getDaoFactory());
	bind(IValueMatcherFactory.class).to(ValueMatcherFactory.class).in(Scopes.SINGLETON);

	bind(new TypeLiteral<IEntityDao<EntityWithMoney>>() {
	}).to(EntityWithMoneyDao.class);
    }
}
