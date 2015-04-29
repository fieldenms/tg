package ua.com.fielden.platform.test.ioc;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.dao.EntityWithMoneyDao;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.ioc.BasicWebServerModule;
import ua.com.fielden.platform.migration.controller.IMigrationErrorDao;
import ua.com.fielden.platform.migration.controller.IMigrationHistoryDao;
import ua.com.fielden.platform.migration.controller.IMigrationRunDao;
import ua.com.fielden.platform.migration.dao.MigrationErrorDao;
import ua.com.fielden.platform.migration.dao.MigrationHistoryDao;
import ua.com.fielden.platform.migration.dao.MigrationRunDao;
import ua.com.fielden.platform.persistence.types.EntityWithMoney;
import ua.com.fielden.platform.sample.domain.ITgAuthor;
import ua.com.fielden.platform.sample.domain.ITgAuthoriser;
import ua.com.fielden.platform.sample.domain.ITgAverageFuelUsage;
import ua.com.fielden.platform.sample.domain.ITgBogie;
import ua.com.fielden.platform.sample.domain.ITgBogieClass;
import ua.com.fielden.platform.sample.domain.ITgBogieLocation;
import ua.com.fielden.platform.sample.domain.ITgCategory;
import ua.com.fielden.platform.sample.domain.ITgFuelType;
import ua.com.fielden.platform.sample.domain.ITgFuelUsage;
import ua.com.fielden.platform.sample.domain.ITgMakeCount;
import ua.com.fielden.platform.sample.domain.ITgMeterReading;
import ua.com.fielden.platform.sample.domain.ITgOrgUnit1;
import ua.com.fielden.platform.sample.domain.ITgOrgUnit2;
import ua.com.fielden.platform.sample.domain.ITgOrgUnit3;
import ua.com.fielden.platform.sample.domain.ITgOrgUnit4;
import ua.com.fielden.platform.sample.domain.ITgOrgUnit5;
import ua.com.fielden.platform.sample.domain.ITgOriginator;
import ua.com.fielden.platform.sample.domain.ITgPerson;
import ua.com.fielden.platform.sample.domain.ITgPersonName;
import ua.com.fielden.platform.sample.domain.ITgSubSystem;
import ua.com.fielden.platform.sample.domain.ITgSystem;
import ua.com.fielden.platform.sample.domain.ITgTimesheet;
import ua.com.fielden.platform.sample.domain.ITgVehicle;
import ua.com.fielden.platform.sample.domain.ITgVehicleFinDetails;
import ua.com.fielden.platform.sample.domain.ITgVehicleMake;
import ua.com.fielden.platform.sample.domain.ITgVehicleModel;
import ua.com.fielden.platform.sample.domain.ITgWagon;
import ua.com.fielden.platform.sample.domain.ITgWagonClass;
import ua.com.fielden.platform.sample.domain.ITgWagonClassCompatibility;
import ua.com.fielden.platform.sample.domain.ITgWagonSlot;
import ua.com.fielden.platform.sample.domain.ITgWorkshop;
import ua.com.fielden.platform.sample.domain.TgAuthorDao;
import ua.com.fielden.platform.sample.domain.TgAuthoriserDao;
import ua.com.fielden.platform.sample.domain.TgAverageFuelUsageDao;
import ua.com.fielden.platform.sample.domain.TgBogieClassDao;
import ua.com.fielden.platform.sample.domain.TgBogieDao;
import ua.com.fielden.platform.sample.domain.TgBogieLocationDao;
import ua.com.fielden.platform.sample.domain.TgCategoryDao;
import ua.com.fielden.platform.sample.domain.TgFuelTypeDao;
import ua.com.fielden.platform.sample.domain.TgFuelUsageDao;
import ua.com.fielden.platform.sample.domain.TgMakeCountDao;
import ua.com.fielden.platform.sample.domain.TgMeterReadingDao;
import ua.com.fielden.platform.sample.domain.TgOrgUnit1Dao;
import ua.com.fielden.platform.sample.domain.TgOrgUnit2Dao;
import ua.com.fielden.platform.sample.domain.TgOrgUnit3Dao;
import ua.com.fielden.platform.sample.domain.TgOrgUnit4Dao;
import ua.com.fielden.platform.sample.domain.TgOrgUnit5Dao;
import ua.com.fielden.platform.sample.domain.TgOriginatorDao;
import ua.com.fielden.platform.sample.domain.TgPersonDao;
import ua.com.fielden.platform.sample.domain.TgPersonNameDao;
import ua.com.fielden.platform.sample.domain.TgSubSystemDao;
import ua.com.fielden.platform.sample.domain.TgSystemDao;
import ua.com.fielden.platform.sample.domain.TgTimesheetDao;
import ua.com.fielden.platform.sample.domain.TgVehicleDao;
import ua.com.fielden.platform.sample.domain.TgVehicleFinDetailsDao;
import ua.com.fielden.platform.sample.domain.TgVehicleMakeDao;
import ua.com.fielden.platform.sample.domain.TgVehicleModelDao;
import ua.com.fielden.platform.sample.domain.TgWagonClassCompatibilityDao;
import ua.com.fielden.platform.sample.domain.TgWagonClassDao;
import ua.com.fielden.platform.sample.domain.TgWagonDao;
import ua.com.fielden.platform.sample.domain.TgWagonSlotDao;
import ua.com.fielden.platform.sample.domain.TgWorkshopDao;
import ua.com.fielden.platform.security.annotations.PasswordHashingKey;
import ua.com.fielden.platform.security.annotations.SessionCache;
import ua.com.fielden.platform.security.annotations.SessionHashingKey;
import ua.com.fielden.platform.security.annotations.TrustedDeviceSessionDuration;
import ua.com.fielden.platform.security.annotations.UntrustedDeviceSessionDuration;
import ua.com.fielden.platform.security.provider.SecurityTokenProvider;
import ua.com.fielden.platform.security.session.UserSession;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.impl.ThreadLocalUserProvider;
import ua.com.fielden.platform.serialisation.api.ISerialisationClassProvider;
import ua.com.fielden.platform.utils.IUniversalConstants;

import com.google.common.base.Ticker;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;

/**
 * Serve IoC module for platform related testing.
 *
 * @author TG Team
 *
 */
public class PlatformTestServerModule extends BasicWebServerModule {

    public PlatformTestServerModule(final Map<Class, Class> defaultHibernateTypes, //
            final IApplicationDomainProvider applicationDomainProvider,//
            final Class<? extends ISerialisationClassProvider> serialisationClassProviderType, //
            final Class<? extends IFilter> automaticDataFilterType, //
            final SecurityTokenProvider tokenProvider,//
            final Properties props) throws Exception {
        super(defaultHibernateTypes, applicationDomainProvider, serialisationClassProviderType, automaticDataFilterType, tokenProvider, props);
    }

    public PlatformTestServerModule(final Map<Class, Class> defaultHibernateTypes, //
            final IApplicationDomainProvider applicationDomainProvider,//
            final Class<? extends ISerialisationClassProvider> serialisationClassProviderType, //
            final Class<? extends IFilter> automaticDataFilterType, //
            final Properties props) throws Exception {
        super(defaultHibernateTypes, applicationDomainProvider, serialisationClassProviderType, automaticDataFilterType, null, props);
    }

    @Override
    protected void configure() {
        super.configure();

        bindConstant().annotatedWith(SessionHashingKey.class).to("This is a hasing key, which is used to hash session data in unit tests.");
        bindConstant().annotatedWith(PasswordHashingKey.class).to("This is a hasing key, which is used to hash user passwords in unit tests.");
        bindConstant().annotatedWith(TrustedDeviceSessionDuration.class).to(60 * 24 * 3); // three days
        bindConstant().annotatedWith(UntrustedDeviceSessionDuration.class).to(5); // 5 minutes

        bind(Ticker.class).to(TickerForSessionCache.class).in(Scopes.SINGLETON);
        bind(IUniversalConstants.class).to(UniversalConstantsForTesting.class).in(Scopes.SINGLETON);
        bind(new TypeLiteral<Cache<String, UserSession>>(){}).annotatedWith(SessionCache.class).toProvider(SessionCacheBuilder.class).in(Scopes.SINGLETON);

        bind(IUserProvider.class).to(ThreadLocalUserProvider.class).in(Scopes.SINGLETON);
        // bind(IUserProvider.class).to(UserProviderForTesting.class).in(Scopes.SINGLETON);

        // bind DAO
        //	bind(IWheelsetDao.class).to(WheelsetDao.class);
        //	bind(IWorkshopDao2.class).to(WorkshopDao2.class);
        //	bind(IWheelsetClassDao.class).to(WheelsetClassDao.class);
        //	bind(IWorkorderDao.class).to(WorkorderDao.class);
        //	bind(IWorkorderableDao.class).to(WorkorderableDao.class);
        //	bind(IAdviceDao.class).to(AdviceDao.class);

        bind(ITgOrgUnit1.class).to(TgOrgUnit1Dao.class);
        bind(ITgOrgUnit2.class).to(TgOrgUnit2Dao.class);
        bind(ITgOrgUnit3.class).to(TgOrgUnit3Dao.class);
        bind(ITgOrgUnit4.class).to(TgOrgUnit4Dao.class);
        bind(ITgOrgUnit5.class).to(TgOrgUnit5Dao.class);

        bind(ITgBogieLocation.class).to(TgBogieLocationDao.class);
        bind(ITgBogie.class).to(TgBogieDao.class);
        bind(ITgBogieClass.class).to(TgBogieClassDao.class);
        bind(ITgWagon.class).to(TgWagonDao.class);
        bind(ITgWagonSlot.class).to(TgWagonSlotDao.class);
        bind(ITgWagonClass.class).to(TgWagonClassDao.class);
        bind(ITgWagonClassCompatibility.class).to(TgWagonClassCompatibilityDao.class);
        bind(ITgWorkshop.class).to(TgWorkshopDao.class);
        bind(ITgTimesheet.class).to(TgTimesheetDao.class);
        bind(ITgSystem.class).to(TgSystemDao.class);
        bind(ITgSubSystem.class).to(TgSubSystemDao.class);
        bind(ITgCategory.class).to(TgCategoryDao.class);
        bind(ITgVehicle.class).to(TgVehicleDao.class);
        bind(ITgVehicleFinDetails.class).to(TgVehicleFinDetailsDao.class);
        bind(ITgPersonName.class).to(TgPersonNameDao.class);
        bind(ITgAuthor.class).to(TgAuthorDao.class);
        bind(ITgFuelUsage.class).to(TgFuelUsageDao.class);
        bind(ITgFuelType.class).to(TgFuelTypeDao.class);
        bind(ITgVehicleModel.class).to(TgVehicleModelDao.class);
        bind(ITgVehicleMake.class).to(TgVehicleMakeDao.class);
        bind(ITgMeterReading.class).to(TgMeterReadingDao.class);
        bind(IMigrationErrorDao.class).to(MigrationErrorDao.class);
        bind(IMigrationRunDao.class).to(MigrationRunDao.class);
        bind(IMigrationHistoryDao.class).to(MigrationHistoryDao.class);
        bind(ITgPerson.class).to(TgPersonDao.class);
        bind(ITgAuthoriser.class).to(TgAuthoriserDao.class);
        bind(ITgOriginator.class).to(TgOriginatorDao.class);

        bind(ITgMakeCount.class).to(TgMakeCountDao.class);
        bind(ITgAverageFuelUsage.class).to(TgAverageFuelUsageDao.class);

        bind(new TypeLiteral<IEntityDao<EntityWithMoney>>() {
        }).to(EntityWithMoneyDao.class);
    }

    private static class SessionCacheBuilder implements Provider<Cache<String, UserSession>> {

        private final Cache<String, UserSession> cache;

        @Inject
        public SessionCacheBuilder(final Ticker ticker) {
            cache = CacheBuilder.newBuilder()
                    // all authenticators should be evicted from the cache in 2 minutes time after that have been
                    // put into the cache
                    .expireAfterWrite(2, TimeUnit.MINUTES)
                    // the ticker controls the eviction time
                    // the injected instance is initialised with IUniversalConstants.now() as its start time
                    .ticker(ticker)
                    .build();
        }

        @Override
        public Cache<String, UserSession> get() {
            return cache;
        }

    }
}
