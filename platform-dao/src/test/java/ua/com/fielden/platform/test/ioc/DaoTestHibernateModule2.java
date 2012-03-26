package ua.com.fielden.platform.test.ioc;

import org.hibernate.SessionFactory;

import ua.com.fielden.platform.dao.EntityWithMoneyDao2;
import ua.com.fielden.platform.dao.filtering.DataFilter2;
import ua.com.fielden.platform.dao2.IDaoFactory2;
import ua.com.fielden.platform.dao2.IEntityDao2;
import ua.com.fielden.platform.dao2.ISecurityRoleAssociationDao2;
import ua.com.fielden.platform.dao2.IUserAndRoleAssociationDao2;
import ua.com.fielden.platform.dao2.IUserRoleDao2;
import ua.com.fielden.platform.dao2.MappingsGenerator;
import ua.com.fielden.platform.entity.matcher.IValueMatcherFactory2;
import ua.com.fielden.platform.entity.matcher.ValueMatcherFactory2;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.ioc.CommonFactoryModule2;
import ua.com.fielden.platform.keygen.IKeyNumberGenerator;
import ua.com.fielden.platform.keygen.KeyNumberDao2;
import ua.com.fielden.platform.migration.controller.IMigrationErrorDao2;
import ua.com.fielden.platform.migration.controller.IMigrationHistoryDao2;
import ua.com.fielden.platform.migration.controller.IMigrationRunDao2;
import ua.com.fielden.platform.migration.dao.MigrationErrorDao2;
import ua.com.fielden.platform.migration.dao.MigrationHistoryDao2;
import ua.com.fielden.platform.migration.dao.MigrationRunDao2;
import ua.com.fielden.platform.persistence.types.EntityWithMoney;
import ua.com.fielden.platform.sample.domain.controller.ITgMeterReading2;
import ua.com.fielden.platform.sample.domain.controller.ITgTimesheet;
import ua.com.fielden.platform.sample.domain.controller.ITgVehicle;
import ua.com.fielden.platform.sample.domain.controller.ITgVehicleMake2;
import ua.com.fielden.platform.sample.domain.controller.ITgVehicleModel2;
import ua.com.fielden.platform.security.dao.SecurityRoleAssociationDao2;
import ua.com.fielden.platform.security.dao.UserAndRoleAssociationDao2;
import ua.com.fielden.platform.security.dao.UserRoleDao2;
import ua.com.fielden.platform.security.provider.IUserController2;
import ua.com.fielden.platform.security.provider.UserController2;
import ua.com.fielden.platform.security.user.IUserDao2;
import ua.com.fielden.platform.test.domain.entities.daos.BogieDao2;
import ua.com.fielden.platform.test.domain.entities.daos.IBogieDao2;
import ua.com.fielden.platform.test.domain.entities.daos.ITgWorkorder;
import ua.com.fielden.platform.test.domain.entities.daos.IWagonDao2;
import ua.com.fielden.platform.test.domain.entities.daos.IWagonSlotDao2;
import ua.com.fielden.platform.test.domain.entities.daos.IWorkshopDao2;
import ua.com.fielden.platform.test.domain.entities.daos.TgMeterReadingDao2;
import ua.com.fielden.platform.test.domain.entities.daos.TgVehicleMakeDao2;
import ua.com.fielden.platform.test.domain.entities.daos.TgVehicleModelDao2;
import ua.com.fielden.platform.test.domain.entities.daos.TgWorkorderDao;
import ua.com.fielden.platform.test.domain.entities.daos.WagonDao2;
import ua.com.fielden.platform.test.domain.entities.daos.WagonSlotDao2;
import ua.com.fielden.platform.test.domain.entities.daos.WorkshopDao2;
import ua.com.fielden.platform.test.entities.daos.TgTimesheetDao;
import ua.com.fielden.platform.test.entities.daos.TgVehicleDao;
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

import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;


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
	bind(IKeyNumberGenerator.class).to(KeyNumberDao2.class);
	bind(IBogieDao2.class).to(BogieDao2.class);
//	bind(IWheelsetDao.class).to(WheelsetDao.class);
//	bind(IRotableDao.class).to(RotableDao.class);
	bind(IWorkshopDao2.class).to(WorkshopDao2.class);
//	bind(IWagonClassDao.class).to(WagonClassDao.class);
//	bind(IBogieClassDao.class).to(BogieClassDao.class);
//	bind(IWheelsetClassDao.class).to(WheelsetClassDao.class);
	bind(IWagonDao2.class).to(WagonDao2.class);
	bind(IWagonSlotDao2.class).to(WagonSlotDao2.class);
	bind(ITgWorkorder.class).to(TgWorkorderDao.class);
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

	bind(ITgTimesheet.class).to(TgTimesheetDao.class);
	bind(ITgVehicleModel2.class).to(TgVehicleModelDao2.class);
	bind(ITgVehicleMake2.class).to(TgVehicleMakeDao2.class);
	bind(ITgVehicle.class).to(TgVehicleDao.class);
	bind(ITgMeterReading2.class).to(TgMeterReadingDao2.class);
	bind(IMigrationErrorDao2.class).to(MigrationErrorDao2.class);
	bind(IMigrationRunDao2.class).to(MigrationRunDao2.class);
	bind(IMigrationHistoryDao2.class).to(MigrationHistoryDao2.class);

	bind(IDaoFactory2.class).toInstance(getDaoFactory());
	bind(IValueMatcherFactory2.class).to(ValueMatcherFactory2.class).in(Scopes.SINGLETON);

	bind(new TypeLiteral<IEntityDao2<EntityWithMoney>>() {
	}).to(EntityWithMoneyDao2.class);
    }
}
