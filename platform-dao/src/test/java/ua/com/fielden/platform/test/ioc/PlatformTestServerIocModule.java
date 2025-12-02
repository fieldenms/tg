package ua.com.fielden.platform.test.ioc;

import com.google.common.base.Ticker;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import jakarta.inject.Singleton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.dao.EntityWithMoneyDao;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.activatable.test_entities.*;
import ua.com.fielden.platform.entity.functional.centre.CentreContextHolderDao;
import ua.com.fielden.platform.entity.functional.centre.ICentreContextHolder;
import ua.com.fielden.platform.entity.functional.centre.ISavingInfoHolder;
import ua.com.fielden.platform.entity.functional.centre.SavingInfoHolderDao;
import ua.com.fielden.platform.entity.validation.exists.test_entities.*;
import ua.com.fielden.platform.entity.validation.test_entities.EntityWithDynamicRequirednessCo;
import ua.com.fielden.platform.entity.validation.test_entities.EntityWithDynamicRequirednessDao;
import ua.com.fielden.platform.ioc.BasicWebServerIocModule;
import ua.com.fielden.platform.persistence.types.EntityWithMoney;
import ua.com.fielden.platform.sample.domain.*;
import ua.com.fielden.platform.sample.domain.compound.*;
import ua.com.fielden.platform.security.annotations.SessionCache;
import ua.com.fielden.platform.security.annotations.SessionHashingKey;
import ua.com.fielden.platform.security.annotations.TrustedDeviceSessionDuration;
import ua.com.fielden.platform.security.annotations.UntrustedDeviceSessionDuration;
import ua.com.fielden.platform.security.provider.SecurityTestIocModule;
import ua.com.fielden.platform.security.session.UserSession;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.impl.ThreadLocalUserProvider;
import ua.com.fielden.platform.test.entities.*;
import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.utils.IUniversalConstants;
import ua.com.fielden.platform.web.annotations.AppUri;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;

/// Serve IoC module for platform related testing.
///
public class PlatformTestServerIocModule extends BasicWebServerIocModule {

    private static final Logger LOGGER = LogManager.getLogger(PlatformTestServerIocModule.class);

    private final Properties props;

    public PlatformTestServerIocModule(
            final IApplicationDomainProvider applicationDomainProvider,
            final List<Class<? extends AbstractEntity<?>>> domainEntityTypes,
            final Properties props)
    {
        super(applicationDomainProvider, domainEntityTypes, props);
        this.props = props;
    }

    @Override
    protected void configure() {
        super.configure();

        bindConstant().annotatedWith(SessionHashingKey.class).to("This is a hashing key, which is used to hash session data in unit tests.");
        bindConstant().annotatedWith(TrustedDeviceSessionDuration.class).to(60 * 24 * 3); // three days
        bindConstant().annotatedWith(UntrustedDeviceSessionDuration.class).to(5); // 5 minutes

        bind(Ticker.class).to(TickerForSessionCache.class);
        bind(IDates.class).to(DatesForTesting.class);
        bind(IUniversalConstants.class).to(UniversalConstantsForTesting.class);

        bind(IUserProvider.class).to(ThreadLocalUserProvider.class);

        install(new SecurityTestIocModule());

        bindConstant().annotatedWith(AppUri.class).to(format("https://%s:%s%s", getProps().get("web.domain"), getProps().get("port"), getProps().get("web.path")));
    }

    @Override
    protected void bindDomainCompanionObjects(final List<Class<? extends AbstractEntity<?>>> domainEntityTypes) {
        bind(ITgOrgUnit1.class).to(TgOrgUnit1Dao.class);
        bind(ITgOrgUnit2.class).to(TgOrgUnit2Dao.class);
        bind(ITgOrgUnit3.class).to(TgOrgUnit3Dao.class);
        bind(ITgOrgUnit4.class).to(TgOrgUnit4Dao.class);
        bind(ITgOrgUnit5.class).to(TgOrgUnit5Dao.class);
        bind(TgOrgUnit5WithSummariesCo.class).to(TgOrgUnit5WithSummariesDao.class);

        bind(ITgBogieLocation.class).to(TgBogieLocationDao.class);
        bind(UnionEntityWithSkipExistsValidationCo.class).to(UnionEntityWithSkipExistsValidationDao.class);
        bind(EntityWithUnionEntityWithSkipExistsValidationCo.class).to(EntityWithUnionEntityWithSkipExistsValidationDao.class);
        bind(ITgBogie.class).to(TgBogieDao.class);
        bind(TgReBogieWithHighLoadCo.class).to(TgReBogieWithHighLoadDao.class);
        bind(TgSynBogieCo.class).to(TgSynBogieDao.class);
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
        bind(AuditedEntityCo.class).to(AuditedEntityDao.class);
        bind(TgReVehicleWithHighPriceCo.class).to(TgReVehicleWithHighPriceDao.class);
        bind(TeNamedValuesVectorCo.class).to(TeNamedValuesVectorDao.class);
        bind(TeProductPriceCo.class).to(TeProductPriceDao.class);
        bind(ITeVehicle.class).to(TeVehicleDao.class);
        bind(ITgWebApiEntity.class).to(TgWebApiEntityDao.class);
        bind(ITgWebApiEntitySyntheticSingle.class).to(TgWebApiEntitySyntheticSingleDao.class);
        bind(ITgWebApiEntitySyntheticMulti.class).to(TgWebApiEntitySyntheticMultiDao.class);
        bind(ITgCompoundEntity.class).to(TgCompoundEntityDao.class);
        bind(ITgCompoundEntityDetail.class).to(TgCompoundEntityDetailDao.class);
        bind(ITgCompoundEntityChild.class).to(TgCompoundEntityChildDao.class);
        bind(ITgVehicleFinDetails.class).to(TgVehicleFinDetailsDao.class);
        bind(TgVehicleTechDetailsCo.class).to(TgVehicleTechDetailsDao.class);
        bind(ITeVehicleFinDetails.class).to(TeVehicleFinDetailsDao.class);
        bind(ITgPersonName.class).to(TgPersonNameDao.class);
        bind(TgPatternCo.class).to(TgPatternDao.class);
        bind(ITgAuthor.class).to(TgAuthorDao.class);
        bind(ITgFuelUsage.class).to(TgFuelUsageDao.class);
        bind(ITeVehicleFuelUsage.class).to(TeVehicleFuelUsageDao.class);
        bind(TeFuelUsageByTypeCo.class).to(TeFuelUsageByTypeDao.class);
        bind(ITgWorkOrder.class).to(TgWorkOrderDao.class);
        bind(ITeWorkOrder.class).to(TeWorkOrderDao.class);
        bind(ITgModelCount.class).to(TgModelCountDao.class);
        bind(ITgModelYearCount.class).to(TgModelYearCountDao.class);
        bind(ITgFuelType.class).to(TgFuelTypeDao.class);
        bind(ITgVehicleModel.class).to(TgVehicleModelDao.class);
        bind(ITeVehicleModel.class).to(TeVehicleModelDao.class);
        bind(ITgVehicleMake.class).to(TgVehicleMakeDao.class);
        bind(ITeVehicleMake.class).to(TeVehicleMakeDao.class);
        bind(ITgMeterReading.class).to(TgMeterReadingDao.class);
        bind(ICentreContextHolder.class).to(CentreContextHolderDao.class);
        bind(ISavingInfoHolder.class).to(SavingInfoHolderDao.class);
        bind(ITgPerson.class).to(TgPersonDao.class);
        bind(ITgAuthoriser.class).to(TgAuthoriserDao.class);
        bind(ITgOriginator.class).to(TgOriginatorDao.class);
        bind(TgOriginatorDetailsCo.class).to(TgOriginatorDetailsDao.class);
        bind(ITgDateTestEntity.class).to(TgDateTestEntityDao.class);
        bind(TgEntityWithManyPropTypesCo.class).to(TgEntityWithManyPropTypesDao.class);
        bind(IEntityOne.class).to(EntityOneDao.class);
        bind(IEntityTwo.class).to(EntityTwoDao.class);
        bind(EntityThreeCo.class).to(EntityThreeDao.class);
        bind(IUnionEntity.class).to(UnionEntityDao.class);

        bind(ITgMakeCount.class).to(TgMakeCountDao.class);
        bind(ITgAverageFuelUsage.class).to(TgAverageFuelUsageDao.class);
        bind(TgReMaxVehicleReadingCo.class).to(TgReMaxVehicleReadingDao.class);
        bind(ITeAverageFuelUsage.class).to(TeAverageFuelUsageDao.class);
        bind(ITgVehicleFuelUsage.class).to(TgVehicleFuelUsageDao.class);
        bind(ITgEntityWithComplexSummaries.class).to(TgEntityWithComplexSummariesDao.class);
        bind(TgEntityWithComplexSummariesThatActuallyDeclareThoseSummariesCo.class).to(TgEntityWithComplexSummariesThatActuallyDeclareThoseSummariesDao.class);
        bind(ITgEntityWithTimeZoneDates.class).to(TgEntityWithTimeZoneDatesDao.class);

        bind(ITgAuthorship.class).to(TgAuthorshipDao.class);
        bind(ITgAuthorRoyalty.class).to(TgAuthorRoyaltyDao.class);
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
        bind(EntityWithDynamicRequirednessCo.class).to(EntityWithDynamicRequirednessDao.class);

        bind(TgUnionCo.class).to(TgUnionDao.class);
        bind(TgUnionType1Co.class).to(TgUnionType1Dao.class);
        bind(TgUnionType2Co.class).to(TgUnionType2Dao.class);
        bind(TgUnionCommonTypeCo.class).to(TgUnionCommonTypeDao.class);
        bind(ITgPersistentEntityWithProperties.class).to(TgPersistentEntityWithPropertiesDao.class);
        bind(ITgReVehicleModel.class).to(TgReVehicleModelDao.class);
        bind(TgNoteCo.class).to(TgNoteDao.class);

        bind(TgInventoryPartCo.class).to(TgInventoryPartDao.class);
        bind(TgInventoryCo.class).to(TgInventoryDao.class);
        bind(TgInventoryBinCo.class).to(TgInventoryBinDao.class);
        bind(TgInventoryIssueCo.class).to(TgInventoryIssueDao.class);

        bind(UnionEntityDetailsCo.class).to(UnionEntityDetailsDao.class);

        bind(UnionCo.class).to(UnionDao.class);
        bind(Member1Co.class).to(Member1Dao.class);
        bind(MemberDetailsCo.class).to(MemberDetailsDao.class);
        bind(Member2Co.class).to(Member2Dao.class);
        bind(Member3Co.class).to(Member3Dao.class);
        bind(Member4Co.class).to(Member4Dao.class);
        bind(Member5Co.class).to(Member5Dao.class);
        bind(ActivatableUnionOwnerCo.class).to(ActivatableUnionOwnerDao.class);
        bind(UnionOwnerCo.class).to(UnionOwnerDao.class);

        bind(TestExists_UnionCo.class).to(TestExists_UnionDao.class);
        bind(TestExists_Member1Co.class).to(TestExists_Member1Dao.class);
        bind(TestExists_Member2Co.class).to(TestExists_Member2Dao.class);
        bind(TestExists_Member3Co.class).to(TestExists_Member3Dao.class);
        bind(TestExists_Member4Co.class).to(TestExists_Member4Dao.class);
        bind(TestExists_ActivatableUnionOwnerCo.class).to(TestExists_ActivatableUnionOwnerDao.class);
        bind(TestExists_UnionOwnerCo.class).to(TestExists_UnionOwnerDao.class);

        // FIXME the following approach should have been the correct one for binding companion objects,
        //       however, not all test domain entities actually have companions, hence manual binding...
        //       this should really be corrected at some stage
        // dynamically bind DAO implementations for all companion objects
        //for (final Class<? extends AbstractEntity<?>> entityType : applicationDomainProvider.entityTypes()) {
        //    CompanionObjectAutobinder.bindCo(entityType, binder());
        //}

    }

    @Provides
    @Singleton
    @SessionCache Cache<String, UserSession> provideSessionCache(final Ticker ticker) {
        return CacheBuilder.newBuilder()
                // all authenticators should be evicted from the cache in 2 minutes time after that have been
                // put into the cache
                .expireAfterWrite(2, TimeUnit.MINUTES)
                // the ticker controls the eviction time
                // the injected instance is initialised with IUniversalConstants.now() as its start time
                .ticker(ticker)
                .build();
    }

}
