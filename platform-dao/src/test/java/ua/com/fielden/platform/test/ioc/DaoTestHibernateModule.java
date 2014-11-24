package ua.com.fielden.platform.test.ioc;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.SessionFactory;

import ua.com.fielden.platform.dao.DomainMetadata;
import ua.com.fielden.platform.dao.EntityWithMoneyDao;
import ua.com.fielden.platform.dao.IDaoFactory;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dao.ISecurityRoleAssociationDao;
import ua.com.fielden.platform.dao.IUserAndRoleAssociationDao;
import ua.com.fielden.platform.dao.IUserRoleDao;
import ua.com.fielden.platform.entity.matcher.IValueMatcherFactory;
import ua.com.fielden.platform.entity.matcher.ValueMatcherFactory;
import ua.com.fielden.platform.ioc.CommonFactoryModule;
import ua.com.fielden.platform.keygen.IKeyNumber;
import ua.com.fielden.platform.keygen.KeyNumberDao;
import ua.com.fielden.platform.migration.controller.IMigrationErrorDao;
import ua.com.fielden.platform.migration.controller.IMigrationHistoryDao;
import ua.com.fielden.platform.migration.controller.IMigrationRunDao;
import ua.com.fielden.platform.migration.dao.MigrationErrorDao;
import ua.com.fielden.platform.migration.dao.MigrationHistoryDao;
import ua.com.fielden.platform.migration.dao.MigrationRunDao;
import ua.com.fielden.platform.persistence.types.EntityWithMoney;
import ua.com.fielden.platform.sample.domain.ITgMeterReading;
import ua.com.fielden.platform.sample.domain.ITgTimesheet;
import ua.com.fielden.platform.sample.domain.ITgVehicle;
import ua.com.fielden.platform.sample.domain.ITgVehicleMake;
import ua.com.fielden.platform.sample.domain.ITgVehicleModel;
import ua.com.fielden.platform.sample.domain.ITgWorkorder;
import ua.com.fielden.platform.sample.domain.TgMeterReadingDao;
import ua.com.fielden.platform.sample.domain.TgTimesheetDao;
import ua.com.fielden.platform.sample.domain.TgVehicleDao;
import ua.com.fielden.platform.sample.domain.TgVehicleMakeDao;
import ua.com.fielden.platform.sample.domain.TgVehicleModelDao;
import ua.com.fielden.platform.sample.domain.TgWorkorderDao;
import ua.com.fielden.platform.security.dao.SecurityRoleAssociationDao;
import ua.com.fielden.platform.security.dao.UserAndRoleAssociationDao;
import ua.com.fielden.platform.security.dao.UserRoleDao;
import ua.com.fielden.platform.security.provider.IUserController;
import ua.com.fielden.platform.security.provider.UserController;
import ua.com.fielden.platform.security.user.IUserDao;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.serialisation.api.ISerialisationClassProvider;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.api.ISerialiser0;
import ua.com.fielden.platform.serialisation.api.impl.Serialiser;
import ua.com.fielden.platform.serialisation.api.impl.Serialiser0;
import ua.com.fielden.platform.test.UserProviderForTesting;
import ua.com.fielden.platform.test.domain.entities.daos.BogieDao;
import ua.com.fielden.platform.test.domain.entities.daos.IBogieDao;
import ua.com.fielden.platform.test.domain.entities.daos.IWagonDao;
import ua.com.fielden.platform.test.domain.entities.daos.IWagonSlotDao;
import ua.com.fielden.platform.test.domain.entities.daos.IWorkshopDao;
import ua.com.fielden.platform.test.domain.entities.daos.WagonDao;
import ua.com.fielden.platform.test.domain.entities.daos.WagonSlotDao;
import ua.com.fielden.platform.test.domain.entities.daos.WorkshopDao;
import ua.com.fielden.platform.ui.config.EntityCentreAnalysisConfigDao;
import ua.com.fielden.platform.ui.config.IEntityCentreAnalysisConfig;
import ua.com.fielden.platform.ui.config.IMainMenu;
import ua.com.fielden.platform.ui.config.MainMenuDao;
import ua.com.fielden.platform.ui.config.api.IEntityCentreConfigController;
import ua.com.fielden.platform.ui.config.api.IEntityLocatorConfigController;
import ua.com.fielden.platform.ui.config.api.IEntityMasterConfigController;
import ua.com.fielden.platform.ui.config.api.IMainMenuItemController;
import ua.com.fielden.platform.ui.config.api.IMainMenuItemInvisibilityController;
import ua.com.fielden.platform.ui.config.api.IMainMenuStructureBuilder;
import ua.com.fielden.platform.ui.config.controller.EntityCentreConfigControllerDao;
import ua.com.fielden.platform.ui.config.controller.EntityLocatorConfigControllerDao;
import ua.com.fielden.platform.ui.config.controller.EntityMasterConfigControllerDao;
import ua.com.fielden.platform.ui.config.controller.MainMenuItemControllerDao;
import ua.com.fielden.platform.ui.config.controller.MainMenuItemInvisibilityControllerDao;
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

    public DaoTestHibernateModule(final SessionFactory sessionFactory, final DomainMetadata domainMetadata) {
        super(sessionFactory, domainMetadata);
    }

    @Override
    protected void configure() {
        super.configure();
        // bind DAO
        //bind(IFilter.class).to(DataFilter.class);
        bind(IKeyNumber.class).to(KeyNumberDao.class);
        bind(IBogieDao.class).to(BogieDao.class);
        //	bind(IWheelsetDao.class).to(WheelsetDao.class);
        //	bind(IRotableDao.class).to(RotableDao.class);
        bind(IWorkshopDao.class).to(WorkshopDao.class);
        //	bind(IWagonClassDao.class).to(WagonClassDao.class);
        //	bind(IBogieClassDao.class).to(BogieClassDao.class);
        //	bind(IWheelsetClassDao.class).to(WheelsetClassDao.class);
        bind(IWagonDao.class).to(WagonDao.class);
        bind(IWagonSlotDao.class).to(WagonSlotDao.class);
        bind(ITgWorkorder.class).to(TgWorkorderDao.class);
        //	bind(IWorkorderableDao.class).to(WorkorderableDao.class);
        //	bind(IAdviceDao.class).to(AdviceDao.class);
        //	bind(IRotableClassDao.class).to(RotableClassDao.class);
        bind(IUserRoleDao.class).to(UserRoleDao.class);
        bind(IUserAndRoleAssociationDao.class).to(UserAndRoleAssociationDao.class);
        bind(ISecurityRoleAssociationDao.class).to(SecurityRoleAssociationDao.class);

        bind(IUserDao.class).to(UserController.class);
        bind(IUserController.class).to(UserController.class);
        // bind IUserProvider
        bind(IUserProvider.class).to(UserProviderForTesting.class).in(Scopes.SINGLETON);

        bind(IEntityCentreConfigController.class).to(EntityCentreConfigControllerDao.class);
        bind(IEntityCentreAnalysisConfig.class).to(EntityCentreAnalysisConfigDao.class);
        bind(IEntityMasterConfigController.class).to(EntityMasterConfigControllerDao.class);
        bind(IEntityLocatorConfigController.class).to(EntityLocatorConfigControllerDao.class);
        bind(IMainMenuItemController.class).to(MainMenuItemControllerDao.class);
        bind(IMainMenu.class).to(MainMenuDao.class);
        bind(IMainMenuStructureBuilder.class).to(PersistedMainMenuStructureBuilder.class);

        bind(IMainMenuItemInvisibilityController.class).to(MainMenuItemInvisibilityControllerDao.class);

        bind(ITgTimesheet.class).to(TgTimesheetDao.class);
        bind(ITgVehicleModel.class).to(TgVehicleModelDao.class);
        bind(ITgVehicleMake.class).to(TgVehicleMakeDao.class);
        bind(ITgVehicle.class).to(TgVehicleDao.class);
        bind(ITgMeterReading.class).to(TgMeterReadingDao.class);
        bind(IMigrationErrorDao.class).to(MigrationErrorDao.class);
        bind(IMigrationRunDao.class).to(MigrationRunDao.class);
        bind(IMigrationHistoryDao.class).to(MigrationHistoryDao.class);

        bind(IDaoFactory.class).toInstance(getDaoFactory());
        bind(IValueMatcherFactory.class).to(ValueMatcherFactory.class).in(Scopes.SINGLETON);

        bind(new TypeLiteral<IEntityDao<EntityWithMoney>>() {
        }).to(EntityWithMoneyDao.class);

        bind(ISerialisationClassProvider.class).toInstance(new ISerialisationClassProvider() {

            @Override
            public List<Class<?>> classes() {
                return new ArrayList<>();
            }
        });
        bind(ISerialiser0.class).to(Serialiser0.class).in(Scopes.SINGLETON);
        bind(ISerialiser.class).to(Serialiser.class).in(Scopes.SINGLETON);
    }
}
