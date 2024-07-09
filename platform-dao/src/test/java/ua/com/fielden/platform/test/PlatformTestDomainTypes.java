package ua.com.fielden.platform.test;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.domain.PlatformDomainTypes;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.validation.test_entities.EntityWithDynamicRequiredness;
import ua.com.fielden.platform.persistence.composite.EntityWithDynamicCompositeKey;
import ua.com.fielden.platform.persistence.composite.EntityWithSingleMemberDynamicCompositeKey;
import ua.com.fielden.platform.persistence.types.*;
import ua.com.fielden.platform.sample.domain.EntityOne;
import ua.com.fielden.platform.sample.domain.EntityTwo;
import ua.com.fielden.platform.sample.domain.EntityWithUnionEntityWithSkipExistsValidation;
import ua.com.fielden.platform.sample.domain.TeAverageFuelUsage;
import ua.com.fielden.platform.sample.domain.TeFuelUsageByType;
import ua.com.fielden.platform.sample.domain.TeNamedValuesVector;
import ua.com.fielden.platform.sample.domain.TeProductPrice;
import ua.com.fielden.platform.sample.domain.TeVehicle;
import ua.com.fielden.platform.sample.domain.TeVehicleFinDetails;
import ua.com.fielden.platform.sample.domain.TeVehicleFuelUsage;
import ua.com.fielden.platform.sample.domain.TeVehicleMake;
import ua.com.fielden.platform.sample.domain.TeVehicleModel;
import ua.com.fielden.platform.sample.domain.TeWorkOrder;
import ua.com.fielden.platform.sample.domain.TgAuthor;
import ua.com.fielden.platform.sample.domain.TgAuthorRoyalty;
import ua.com.fielden.platform.sample.domain.TgAuthoriser;
import ua.com.fielden.platform.sample.domain.TgAuthorship;
import ua.com.fielden.platform.sample.domain.TgAverageFuelUsage;
import ua.com.fielden.platform.sample.domain.TgBogie;
import ua.com.fielden.platform.sample.domain.TgBogieClass;
import ua.com.fielden.platform.sample.domain.TgBogieLocation;
import ua.com.fielden.platform.sample.domain.TgCategory;
import ua.com.fielden.platform.sample.domain.TgCategoryAttachment;
import ua.com.fielden.platform.sample.domain.TgCentreDiffSerialisation;
import ua.com.fielden.platform.sample.domain.TgCentreDiffSerialisationNonPersistentChild;
import ua.com.fielden.platform.sample.domain.TgCentreDiffSerialisationNonPersistentCompositeChild;
import ua.com.fielden.platform.sample.domain.TgCentreDiffSerialisationPersistentChild;
import ua.com.fielden.platform.sample.domain.TgCollectionalSerialisationChild;
import ua.com.fielden.platform.sample.domain.TgCollectionalSerialisationParent;
import ua.com.fielden.platform.sample.domain.TgDateTestEntity;
import ua.com.fielden.platform.sample.domain.TgEntityWithComplexSummaries;
import ua.com.fielden.platform.sample.domain.TgEntityWithComplexSummariesThatActuallyDeclareThoseSummaries;
import ua.com.fielden.platform.sample.domain.TgFuelType;
import ua.com.fielden.platform.sample.domain.TgFuelUsage;
import ua.com.fielden.platform.sample.domain.TgMakeCount;
import ua.com.fielden.platform.sample.domain.TgMeterReading;
import ua.com.fielden.platform.sample.domain.TgModelCount;
import ua.com.fielden.platform.sample.domain.TgModelYearCount;
import ua.com.fielden.platform.sample.domain.TgOrgUnit1;
import ua.com.fielden.platform.sample.domain.TgOrgUnit2;
import ua.com.fielden.platform.sample.domain.TgOrgUnit3;
import ua.com.fielden.platform.sample.domain.TgOrgUnit4;
import ua.com.fielden.platform.sample.domain.TgOrgUnit5;
import ua.com.fielden.platform.sample.domain.TgOrgUnit5WithSummaries;
import ua.com.fielden.platform.sample.domain.TgOriginator;
import ua.com.fielden.platform.sample.domain.TgOriginatorDetails;
import ua.com.fielden.platform.sample.domain.TgPerson;
import ua.com.fielden.platform.sample.domain.TgPersonName;
import ua.com.fielden.platform.sample.domain.TgPublishedYearly;
import ua.com.fielden.platform.sample.domain.TgReBogieWithHighLoad;
import ua.com.fielden.platform.sample.domain.TgReMaxVehicleReading;
import ua.com.fielden.platform.sample.domain.TgReVehicleModel;
import ua.com.fielden.platform.sample.domain.TgReVehicleWithHighPrice;
import ua.com.fielden.platform.sample.domain.TgSubSystem;
import ua.com.fielden.platform.sample.domain.TgSynBogie;
import ua.com.fielden.platform.sample.domain.TgSystem;
import ua.com.fielden.platform.sample.domain.TgTimesheet;
import ua.com.fielden.platform.sample.domain.TgUnion;
import ua.com.fielden.platform.sample.domain.TgUnionCommonType;
import ua.com.fielden.platform.sample.domain.TgUnionType1;
import ua.com.fielden.platform.sample.domain.TgUnionType2;
import ua.com.fielden.platform.sample.domain.TgVehicle;
import ua.com.fielden.platform.sample.domain.TgVehicleFinDetails;
import ua.com.fielden.platform.sample.domain.TgVehicleFuelUsage;
import ua.com.fielden.platform.sample.domain.TgVehicleMake;
import ua.com.fielden.platform.sample.domain.TgVehicleModel;
import ua.com.fielden.platform.sample.domain.TgVehicleTechDetails;
import ua.com.fielden.platform.sample.domain.TgWagon;
import ua.com.fielden.platform.sample.domain.TgWagonClass;
import ua.com.fielden.platform.sample.domain.TgWagonClassCompatibility;
import ua.com.fielden.platform.sample.domain.TgWagonSlot;
import ua.com.fielden.platform.sample.domain.TgWebApiEntity;
import ua.com.fielden.platform.sample.domain.TgWebApiEntitySyntheticMulti;
import ua.com.fielden.platform.sample.domain.TgWebApiEntitySyntheticSingle;
import ua.com.fielden.platform.sample.domain.TgWorkOrder;
import ua.com.fielden.platform.sample.domain.TgWorkshop;
import ua.com.fielden.platform.sample.domain.UnionEntity;
import ua.com.fielden.platform.sample.domain.UnionEntityWithSkipExistsValidation;
import ua.com.fielden.platform.sample.domain.compound.TgCompoundEntity;
import ua.com.fielden.platform.sample.domain.compound.TgCompoundEntityChild;
import ua.com.fielden.platform.sample.domain.compound.TgCompoundEntityDetail;
import ua.com.fielden.platform.test.entities.ComplexKeyEntity;
import ua.com.fielden.platform.test.entities.CompositeEntity;
import ua.com.fielden.platform.test.entities.CompositeEntityKey;
import ua.com.fielden.platform.test.entities.TgEntityWithManyPropTypes;

/**
 * A class to enlist platform test domain entities. Should be replaced with runtime generation via reflection.
 *
 * @author TG Team
 *
 */
public class PlatformTestDomainTypes implements IApplicationDomainProvider {
    public static final List<Class<? extends AbstractEntity<?>>> entityTypes = new ArrayList<>();

    static void add(final Class<? extends AbstractEntity<?>> domainType) {
        entityTypes.add(domainType);
    }

    static {
        entityTypes.addAll(PlatformDomainTypes.typesNotDependentOnWebUI);
        // and test domain entities
        add(TgPerson.class);
        add(TgPersonName.class);
        add(TgAuthor.class);
        add(TgAuthorship.class);
        add(TgAuthorRoyalty.class);
        add(TgBogie.class);
        add(TgReBogieWithHighLoad.class);
        add(TgSynBogie.class);
        add(TgBogieLocation.class);
        add(UnionEntityWithSkipExistsValidation.class);
        add(EntityWithUnionEntityWithSkipExistsValidation.class);
        add(TgBogieClass.class);
        add(TgWagon.class);
        add(TgWagonSlot.class);
        add(TgWagonClass.class);
        add(TgWagonClassCompatibility.class);
        add(TgWorkshop.class);
        add(TgTimesheet.class);
        add(TgVehicle.class);
        add(TgReVehicleWithHighPrice.class);
        add(TeNamedValuesVector.class);
        add(TeProductPrice.class);
        add(TeVehicle.class);
        add(TgVehicleFinDetails.class);
        add(TgVehicleTechDetails.class);
        add(TeVehicleFinDetails.class);
        add(TgWebApiEntity.class);
        add(TgWebApiEntitySyntheticSingle.class);
        add(TgWebApiEntitySyntheticMulti.class);
        add(TgCompoundEntity.class);
        add(TgCompoundEntityDetail.class);
        add(TgCompoundEntityChild.class);
        add(TgVehicleModel.class);
        add(TeVehicleModel.class);
        add(TgReVehicleModel.class);
        add(TgVehicleMake.class);
        add(TeVehicleMake.class);
        add(TgMakeCount.class);
        add(TgOrgUnit1.class);
        add(TgOrgUnit2.class);
        add(TgOrgUnit3.class);
        add(TgOrgUnit4.class);
        add(TgOrgUnit5.class);
        add(TgOrgUnit5WithSummaries.class);
        add(TgWorkOrder.class);
        add(TeWorkOrder.class);
        add(TgFuelUsage.class);
        add(TeVehicleFuelUsage.class);
        add(TgMeterReading.class);
        add(TgVehicleFuelUsage.class);
        add(TgFuelType.class);
        add(TgModelCount.class);
        add(TgModelYearCount.class);
        add(TgPublishedYearly.class);
        add(CompositeEntity.class);
        add(CompositeEntityKey.class);
        add(ComplexKeyEntity.class);
        add(EntityWithMoney.class);
        add(EntityWithTaxMoney.class);
        add(EntityWithExTaxAndTaxMoney.class);
        add(EntityWithSimpleTaxMoney.class);
        add(EntityWithSimpleMoney.class);
        add(EntityWithDynamicCompositeKey.class);
        add(EntityWithSingleMemberDynamicCompositeKey.class);
        add(EntityWithAutoAssignableProperties.class);
        add(EntityWithRichText.class);
        add(EntityBasedOnAbstractPersistentEntity.class);
        add(EntityBasedOnAbstractPersistentEntity2.class);
        add(TgAverageFuelUsage.class);
        add(TgReMaxVehicleReading.class);
        add(TeAverageFuelUsage.class);
        add(TeFuelUsageByType.class);
        add(TgSystem.class);
        add(TgSubSystem.class);
        add(TgCategory.class);
        add(TgAuthoriser.class);
        add(TgOriginator.class);
        add(TgOriginatorDetails.class);
        add(TgEntityWithComplexSummaries.class);
        add(TgEntityWithComplexSummariesThatActuallyDeclareThoseSummaries.class);
        add(TgCollectionalSerialisationParent.class);
        add(TgCollectionalSerialisationChild.class);
        add(TgCentreDiffSerialisation.class);
        add(TgCentreDiffSerialisationPersistentChild.class);
        add(TgCentreDiffSerialisationNonPersistentChild.class);
        add(TgCentreDiffSerialisationNonPersistentCompositeChild.class);
        add(TgCategoryAttachment.class);
        add(TgDateTestEntity.class);
        add(EntityWithDynamicRequiredness.class);
        add(TgEntityWithManyPropTypes.class);
        add(EntityOne.class);
        add(EntityTwo.class);
        add(UnionEntity.class);
        add(TgUnion.class);
        add(TgUnionType1.class);
        add(TgUnionType2.class);
        add(TgUnionCommonType.class);
    }

    @Override
    public List<Class<? extends AbstractEntity<?>>> entityTypes() {
        return entityTypes;
    }
}
