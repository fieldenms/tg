package ua.com.fielden.platform.test.ioc;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Ticker;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;

import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.dao.EntityWithMoneyDao;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.functional.centre.CentreContextHolderDao;
import ua.com.fielden.platform.entity.functional.centre.ICentreContextHolder;
import ua.com.fielden.platform.entity.functional.centre.ISavingInfoHolder;
import ua.com.fielden.platform.entity.functional.centre.SavingInfoHolderDao;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.ioc.BasicWebServerModule;
import ua.com.fielden.platform.migration.controller.IMigrationError;
import ua.com.fielden.platform.migration.controller.IMigrationHistory;
import ua.com.fielden.platform.migration.controller.IMigrationRun;
import ua.com.fielden.platform.migration.dao.MigrationErrorDao;
import ua.com.fielden.platform.migration.dao.MigrationHistoryDao;
import ua.com.fielden.platform.migration.dao.MigrationRunDao;
import ua.com.fielden.platform.persistence.types.EntityWithMoney;
import ua.com.fielden.platform.sample.domain.*;
import ua.com.fielden.platform.sample.domain.compound.ITgCompoundEntity;
import ua.com.fielden.platform.sample.domain.compound.ITgCompoundEntityChild;
import ua.com.fielden.platform.sample.domain.compound.ITgCompoundEntityDetail;
import ua.com.fielden.platform.sample.domain.compound.TgCompoundEntityChildDao;
import ua.com.fielden.platform.sample.domain.compound.TgCompoundEntityDao;
import ua.com.fielden.platform.sample.domain.compound.TgCompoundEntityDetailDao;
import ua.com.fielden.platform.security.annotations.SessionCache;
import ua.com.fielden.platform.security.annotations.SessionHashingKey;
import ua.com.fielden.platform.security.annotations.TrustedDeviceSessionDuration;
import ua.com.fielden.platform.security.annotations.UntrustedDeviceSessionDuration;
import ua.com.fielden.platform.security.provider.SecurityTokenProvider;
import ua.com.fielden.platform.security.session.UserSession;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.impl.ThreadLocalUserProvider;
import ua.com.fielden.platform.serialisation.api.ISerialisationClassProvider;
import ua.com.fielden.platform.test.entities.ComplexKeyEntityDao;
import ua.com.fielden.platform.test.entities.CompositeEntityDao;
import ua.com.fielden.platform.test.entities.CompositeEntityKeyDao;
import ua.com.fielden.platform.test.entities.IComplexKeyEntity;
import ua.com.fielden.platform.test.entities.ICompositeEntity;
import ua.com.fielden.platform.test.entities.ICompositeEntityKey;
import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.utils.IUniversalConstants;

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
        bindConstant().annotatedWith(TrustedDeviceSessionDuration.class).to(60 * 24 * 3); // three days
        bindConstant().annotatedWith(UntrustedDeviceSessionDuration.class).to(5); // 5 minutes

        bind(Ticker.class).to(TickerForSessionCache.class).in(Scopes.SINGLETON);
        bind(IDates.class).to(DatesForTesting.class).in(Scopes.SINGLETON);
        bind(IUniversalConstants.class).to(UniversalConstantsForTesting.class).in(Scopes.SINGLETON);
        bind(new TypeLiteral<Cache<String, UserSession>>(){}).annotatedWith(SessionCache.class).toProvider(TestSessionCacheBuilder.class).in(Scopes.SINGLETON);

        bind(IUserProvider.class).to(ThreadLocalUserProvider.class).in(Scopes.SINGLETON);
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
        bind(ITgCategoryAttachment.class).to(TgCategoryAttachmentDao.class);
        bind(ITgVehicle.class).to(TgVehicleDao.class);
        bind(ITgWebApiEntity.class).to(TgWebApiEntityDao.class);
        bind(ITgWebApiEntitySyntheticSingle.class).to(TgWebApiEntitySyntheticSingleDao.class);
        bind(ITgWebApiEntitySyntheticMulti.class).to(TgWebApiEntitySyntheticMultiDao.class);
        bind(ITgCompoundEntity.class).to(TgCompoundEntityDao.class);
        bind(ITgCompoundEntityDetail.class).to(TgCompoundEntityDetailDao.class);
        bind(ITgCompoundEntityChild.class).to(TgCompoundEntityChildDao.class);
        bind(ITgVehicleFinDetails.class).to(TgVehicleFinDetailsDao.class);
        bind(ITgPersonName.class).to(TgPersonNameDao.class);
        bind(ITgAuthor.class).to(TgAuthorDao.class);
        bind(ITgFuelUsage.class).to(TgFuelUsageDao.class);
        bind(ITgWorkOrder.class).to(TgWorkOrderDao.class);
        bind(ITgModelCount.class).to(TgModelCountDao.class);
        bind(ITgModelYearCount.class).to(TgModelYearCountDao.class);
        bind(ITgFuelType.class).to(TgFuelTypeDao.class);
        bind(ITgVehicleModel.class).to(TgVehicleModelDao.class);
        bind(ITgVehicleMake.class).to(TgVehicleMakeDao.class);
        bind(ITgMeterReading.class).to(TgMeterReadingDao.class);
        bind(IMigrationError.class).to(MigrationErrorDao.class);
        bind(IMigrationRun.class).to(MigrationRunDao.class);
        bind(IMigrationHistory.class).to(MigrationHistoryDao.class);
        bind(ICentreContextHolder.class).to(CentreContextHolderDao.class);
        bind(ISavingInfoHolder.class).to(SavingInfoHolderDao.class);
        bind(ITgPerson.class).to(TgPersonDao.class);
        bind(ITgAuthoriser.class).to(TgAuthoriserDao.class);
        bind(ITgOriginator.class).to(TgOriginatorDao.class);
        bind(ITgDateTestEntity.class).to(TgDateTestEntityDao.class);
        bind(TgEntityWithManyPropTypesCo.class).to(TgEntityWithManyPropTypesDao.class);
        bind(IEntityOne.class).to(EntityOneDao.class);
        bind(IEntityTwo.class).to(EntityTwoDao.class);
        bind(IUnionEntity.class).to(UnionEntityDao.class);

        bind(ITgMakeCount.class).to(TgMakeCountDao.class);
        bind(ITgAverageFuelUsage.class).to(TgAverageFuelUsageDao.class);
        bind(ITgVehicleFuelUsage.class).to(TgVehicleFuelUsageDao.class);
        bind(ITgEntityWithComplexSummaries.class).to(TgEntityWithComplexSummariesDao.class);

        bind(ITgAuthorship.class).to(TgAuthorshipDao.class);
        bind(ITgAuthorRoyalty.class).to(TgAuthorRoyaltyDao.class);
        bind(ITgEntityWithLoopedCalcProps.class).to(TgEntityWithLoopedCalcPropsDao.class);
        bind(ITgPublishedYearly.class).to(TgPublishedYearlyDao.class);


        bind(ICompositeEntity.class).to(CompositeEntityDao.class);
        bind(ICompositeEntityKey.class).to(CompositeEntityKeyDao.class);
        bind(IComplexKeyEntity.class).to(ComplexKeyEntityDao.class);

        bind(new TypeLiteral<IEntityDao<EntityWithMoney>>() {
        }).to(EntityWithMoneyDao.class);

        bind(ITgCollectionalSerialisationParent.class).to(TgCollectionalSerialisationParentDao.class);
        bind(ITgCollectionalSerialisationChild.class).to(TgCollectionalSerialisationChildDao.class);
        bind(ITgCentreDiffSerialisation.class).to(TgCentreDiffSerialisationDao.class);
        bind(ITgCentreDiffSerialisationPersistentChild.class).to(TgCentreDiffSerialisationPersistentChildDao.class);
        bind(ITgCentreDiffSerialisationNonPersistentChild.class).to(TgCentreDiffSerialisationNonPersistentChildDao.class);
        bind(ITgCentreDiffSerialisationNonPersistentCompositeChild.class).to(TgCentreDiffSerialisationNonPersistentCompositeChildDao.class);
    }

    public static class TestSessionCacheBuilder implements Provider<Cache<String, UserSession>> {

        private final Cache<String, UserSession> cache;

        @Inject
        public TestSessionCacheBuilder(final Ticker ticker) {
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
