package ua.com.fielden.platform.test.ioc;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.SessionFactory;

import com.google.common.base.Ticker;
import com.google.common.cache.Cache;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;

import ua.com.fielden.platform.dao.DomainMetadata;
import ua.com.fielden.platform.dao.EntityWithMoneyDao;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dao.ISecurityRoleAssociationDao;
import ua.com.fielden.platform.dao.IUserAndRoleAssociation;
import ua.com.fielden.platform.dao.IUserRoleDao;
import ua.com.fielden.platform.entity.matcher.IValueMatcherFactory;
import ua.com.fielden.platform.entity.matcher.ValueMatcherFactory;
import ua.com.fielden.platform.entity.query.IdOnlyProxiedEntityTypeCache;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.ioc.CommonFactoryModule;
import ua.com.fielden.platform.keygen.IKeyNumber;
import ua.com.fielden.platform.keygen.KeyNumberDao;
import ua.com.fielden.platform.migration.controller.IMigrationError;
import ua.com.fielden.platform.migration.controller.IMigrationHistory;
import ua.com.fielden.platform.migration.controller.IMigrationRun;
import ua.com.fielden.platform.migration.dao.MigrationErrorDao;
import ua.com.fielden.platform.migration.dao.MigrationHistoryDao;
import ua.com.fielden.platform.migration.dao.MigrationRunDao;
import ua.com.fielden.platform.persistence.types.EntityWithMoney;
import ua.com.fielden.platform.sample.domain.ITgMeterReading;
import ua.com.fielden.platform.sample.domain.ITgTimesheet;
import ua.com.fielden.platform.sample.domain.ITgVehicle;
import ua.com.fielden.platform.sample.domain.ITgVehicleMake;
import ua.com.fielden.platform.sample.domain.ITgVehicleModel;
import ua.com.fielden.platform.sample.domain.ITgWorkOrder;
import ua.com.fielden.platform.sample.domain.TgMeterReadingDao;
import ua.com.fielden.platform.sample.domain.TgTimesheetDao;
import ua.com.fielden.platform.sample.domain.TgVehicleDao;
import ua.com.fielden.platform.sample.domain.TgVehicleMakeDao;
import ua.com.fielden.platform.sample.domain.TgVehicleModelDao;
import ua.com.fielden.platform.sample.domain.TgWorkOrderDao;
import ua.com.fielden.platform.security.IAuthorisationModel;
import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.annotations.SessionCache;
import ua.com.fielden.platform.security.annotations.SessionHashingKey;
import ua.com.fielden.platform.security.annotations.TrustedDeviceSessionDuration;
import ua.com.fielden.platform.security.annotations.UntrustedDeviceSessionDuration;
import ua.com.fielden.platform.security.dao.SecurityRoleAssociationDao;
import ua.com.fielden.platform.security.dao.UserAndRoleAssociationDao;
import ua.com.fielden.platform.security.dao.UserRoleDao;
import ua.com.fielden.platform.security.session.IUserSession;
import ua.com.fielden.platform.security.session.UserSession;
import ua.com.fielden.platform.security.session.UserSessionDao;
import ua.com.fielden.platform.security.user.INewUserNotifier;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.security.user.UserDao;
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
import ua.com.fielden.platform.test.ioc.PlatformTestServerModule.TestSessionCacheBuilder;
import ua.com.fielden.platform.ui.config.EntityCentreAnalysisConfigDao;
import ua.com.fielden.platform.ui.config.IEntityCentreAnalysisConfig;
import ua.com.fielden.platform.ui.config.IMainMenu;
import ua.com.fielden.platform.ui.config.MainMenuDao;
import ua.com.fielden.platform.ui.config.api.IEntityCentreConfig;
import ua.com.fielden.platform.ui.config.api.IEntityLocatorConfig;
import ua.com.fielden.platform.ui.config.api.IEntityMasterConfig;
import ua.com.fielden.platform.ui.config.api.IMainMenuItemController;
import ua.com.fielden.platform.ui.config.api.IMainMenuItemInvisibility;
import ua.com.fielden.platform.ui.config.api.IMainMenuStructureBuilder;
import ua.com.fielden.platform.ui.config.controller.EntityCentreConfigDao;
import ua.com.fielden.platform.ui.config.controller.EntityLocatorConfigDao;
import ua.com.fielden.platform.ui.config.controller.EntityMasterConfigDao;
import ua.com.fielden.platform.ui.config.controller.MainMenuItemControllerDao;
import ua.com.fielden.platform.ui.config.controller.MainMenuItemInvisibilityDao;
import ua.com.fielden.platform.ui.config.controller.mixin.PersistedMainMenuStructureBuilder;
import ua.com.fielden.platform.utils.IUniversalConstants;

/**
 * Guice injector module for Hibernate related injections for testing purposes.
 *
 * @author TG Team
 *
 */
public class DaoTestHibernateModule extends CommonFactoryModule {

    public DaoTestHibernateModule(final SessionFactory sessionFactory, final DomainMetadata domainMetadata, final IdOnlyProxiedEntityTypeCache idOnlyProxiedEntityTypeCache) {
        super(sessionFactory, domainMetadata, idOnlyProxiedEntityTypeCache);
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
        bind(ITgWorkOrder.class).to(TgWorkOrderDao.class);
        //	bind(IWorkorderableDao.class).to(WorkorderableDao.class);
        //	bind(IAdviceDao.class).to(AdviceDao.class);
        //	bind(IRotableClassDao.class).to(RotableClassDao.class);
        bind(IUserRoleDao.class).to(UserRoleDao.class);
        bind(IUserAndRoleAssociation.class).to(UserAndRoleAssociationDao.class);
        bind(ISecurityRoleAssociationDao.class).to(SecurityRoleAssociationDao.class);

        bind(IUser.class).to(UserDao.class);
        // bind IUserProvider
        bind(IUserProvider.class).to(UserProviderForTesting.class).in(Scopes.SINGLETON);
        bind(INewUserNotifier.class).toInstance(new INewUserNotifier() {
            @Override
            public void notify(User user) {
            }
            
        });
        
        bind(IEntityCentreConfig.class).to(EntityCentreConfigDao.class);
        bind(IEntityCentreAnalysisConfig.class).to(EntityCentreAnalysisConfigDao.class);
        bind(IEntityMasterConfig.class).to(EntityMasterConfigDao.class);
        bind(IEntityLocatorConfig.class).to(EntityLocatorConfigDao.class);
        bind(IMainMenuItemController.class).to(MainMenuItemControllerDao.class);
        bind(IMainMenu.class).to(MainMenuDao.class);
        bind(IMainMenuStructureBuilder.class).to(PersistedMainMenuStructureBuilder.class);

        bind(IMainMenuItemInvisibility.class).to(MainMenuItemInvisibilityDao.class);

        bind(ITgTimesheet.class).to(TgTimesheetDao.class);
        bind(ITgVehicleModel.class).to(TgVehicleModelDao.class);
        bind(ITgVehicleMake.class).to(TgVehicleMakeDao.class);
        bind(ITgVehicle.class).to(TgVehicleDao.class);
        bind(ITgMeterReading.class).to(TgMeterReadingDao.class);
        bind(IMigrationError.class).to(MigrationErrorDao.class);
        bind(IMigrationRun.class).to(MigrationRunDao.class);
        bind(IMigrationHistory.class).to(MigrationHistoryDao.class);

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
        
        bind(IUserSession.class).to(UserSessionDao.class);
        bindConstant().annotatedWith(SessionHashingKey.class).to("This is a hasing key, which is used to hash session data in unit tests.");
        bindConstant().annotatedWith(TrustedDeviceSessionDuration.class).to(60 * 24 * 3); // three days
        bindConstant().annotatedWith(UntrustedDeviceSessionDuration.class).to(5); // 5 minutes

        bind(Ticker.class).to(TickerForSessionCache.class).in(Scopes.SINGLETON);
        bind(IUniversalConstants.class).to(UniversalConstantsForTesting.class).in(Scopes.SINGLETON);
        bind(new TypeLiteral<Cache<String, UserSession>>(){}).annotatedWith(SessionCache.class).toProvider(TestSessionCacheBuilder.class).in(Scopes.SINGLETON);

        bind(IAuthorisationModel.class).toInstance(new IAuthorisationModel() {
            @Override
            public Result authorise(final Class<? extends ISecurityToken> token) {
                return Result.successful("always permitted");
            }

            @Override
            public void start() {
                
            }

            @Override
            public void stop() {
                
            }

            @Override
            public boolean isStarted() {
                return false;
            }
            
        });
    }
    
    
}
