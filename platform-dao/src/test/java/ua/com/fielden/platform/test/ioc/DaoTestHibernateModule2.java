package ua.com.fielden.platform.test.ioc;

import org.hibernate.SessionFactory;

import ua.com.fielden.platform.dao.MappingsGenerator;
import ua.com.fielden.platform.dao.filtering.DataFilter2;
import ua.com.fielden.platform.dao2.EntityAggregatesDao2;
import ua.com.fielden.platform.dao2.IDaoFactory2;
import ua.com.fielden.platform.dao2.IEntityAggregatesDao2;
import ua.com.fielden.platform.dao2.ISecurityRoleAssociationDao2;
import ua.com.fielden.platform.dao2.IUserAndRoleAssociationDao2;
import ua.com.fielden.platform.dao2.IUserRoleDao2;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.ioc.CommonFactoryModule2;
import ua.com.fielden.platform.sample.domain.controller.ITgVehicleModel2;
import ua.com.fielden.platform.security.dao.SecurityRoleAssociationDao2;
import ua.com.fielden.platform.security.dao.UserAndRoleAssociationDao2;
import ua.com.fielden.platform.security.dao.UserRoleDao2;
import ua.com.fielden.platform.security.provider.IUserController2;
import ua.com.fielden.platform.security.provider.UserController2;
import ua.com.fielden.platform.security.user.IUserDao2;
import ua.com.fielden.platform.test.domain.entities.daos.IWorkshopDao2;
import ua.com.fielden.platform.test.domain.entities.daos.TgVehicleModelDao2;
import ua.com.fielden.platform.test.domain.entities.daos.WorkshopDao2;
import ua.com.fielden.platform.ui.config.api.IEntityCentreConfigController2;
import ua.com.fielden.platform.ui.config.api.IEntityLocatorConfigController2;
import ua.com.fielden.platform.ui.config.api.IEntityMasterConfigController2;
import ua.com.fielden.platform.ui.config.api.IMainMenuItemController2;
import ua.com.fielden.platform.ui.config.api.IMainMenuStructureBuilder2;
import ua.com.fielden.platform.ui.config.controller.EntityCentreConfigControllerDao2;
import ua.com.fielden.platform.ui.config.controller.EntityLocatorConfigControllerDao2;
import ua.com.fielden.platform.ui.config.controller.EntityMasterConfigControllerDao2;
import ua.com.fielden.platform.ui.config.controller.MainMenuItemControllerDao2;
import ua.com.fielden.platform.ui.config.controller.mixin.PersistedMainMenuStructureBuilder2;


/**
 * Guice injector module for Hibernate related injections for testing purposes.
 *
 * @author TG Team
 *
 */
public class DaoTestHibernateModule2 extends CommonFactoryModule2 {

    public DaoTestHibernateModule2(final SessionFactory sessionFactory, final MappingsGenerator mappingsGenerator) {
	super(sessionFactory, mappingsGenerator);
    }

    @Override
    protected void configure() {
	super.configure();
	// bind DAO
	bind(IFilter.class).to(DataFilter2.class);
	bind(IEntityAggregatesDao2.class).to(EntityAggregatesDao2.class);
//	bind(IKeyNumberGenerator.class).to(KeyNumberDao.class);
//	bind(IPersonDao.class).to(PersonDao.class);
//	bind(IBogieDao.class).to(BogieDao.class);
//	bind(IWheelsetDao.class).to(WheelsetDao.class);
//	bind(IRotableDao.class).to(RotableDao.class);
	bind(IWorkshopDao2.class).to(WorkshopDao2.class);
//	bind(IWagonClassDao.class).to(WagonClassDao.class);
//	bind(IBogieClassDao.class).to(BogieClassDao.class);
//	bind(IWheelsetClassDao.class).to(WheelsetClassDao.class);
//	bind(IWagonDao.class).to(WagonDao.class);
//	bind(IWagonSlotDao.class).to(WagonSlotDao.class);
//	bind(IWorkorderDao.class).to(WorkorderDao.class);
//	bind(IWorkorderableDao.class).to(WorkorderableDao.class);
//	bind(IAdviceDao.class).to(AdviceDao.class);
//	bind(IRotableClassDao.class).to(RotableClassDao.class);
	bind(IUserRoleDao2.class).to(UserRoleDao2.class);
	bind(IUserAndRoleAssociationDao2.class).to(UserAndRoleAssociationDao2.class);
	bind(ISecurityRoleAssociationDao2.class).to(SecurityRoleAssociationDao2.class);

	bind(IUserDao2.class).to(UserController2.class);
	bind(IUserController2.class).to(UserController2.class);
	bind(IEntityCentreConfigController2.class).to(EntityCentreConfigControllerDao2.class);
	bind(IEntityMasterConfigController2.class).to(EntityMasterConfigControllerDao2.class);
	bind(IEntityLocatorConfigController2.class).to(EntityLocatorConfigControllerDao2.class);
	bind(IMainMenuItemController2.class).to(MainMenuItemControllerDao2.class);
	bind(IMainMenuStructureBuilder2.class).to(PersistedMainMenuStructureBuilder2.class);

//	bind(ITgTimesheet.class).to(TgTimesheetDao.class);
	bind(ITgVehicleModel2.class).to(TgVehicleModelDao2.class);
//	bind(ITgVehicleMake.class).to(TgVehicleMakeDao.class);
//	bind(ITgMeterReading.class).to(TgMeterReadingDao.class);
//	bind(IMigrationErrorDao.class).to(MigrationErrorDao.class);
//	bind(IMigrationRunDao.class).to(MigrationRunDao.class);
//	bind(IMigrationHistoryDao.class).to(MigrationHistoryDao.class);

	bind(IDaoFactory2.class).toInstance(getDaoFactory());
//	bind(IValueMatcherFactory.class).to(ValueMatcherFactory.class).in(Scopes.SINGLETON);

//	bind(new TypeLiteral<IEntityDao<EntityWithMoney>>() {
//	}).to(EntityWithMoneyDao.class);
    }
}
