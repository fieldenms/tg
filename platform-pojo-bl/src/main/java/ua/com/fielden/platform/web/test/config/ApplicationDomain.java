package ua.com.fielden.platform.web.test.config;

import fielden.test_app.close_leave.*;
import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.domain.PlatformDomainTypes;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.sample.domain.*;
import ua.com.fielden.platform.sample.domain.composite.TgMinorComponent;
import ua.com.fielden.platform.sample.domain.composite.TgRollingStockMajorComponent;
import ua.com.fielden.platform.sample.domain.composite.TgRollingStockMinorComponent;
import ua.com.fielden.platform.sample.domain.compound.TgCompoundEntity;
import ua.com.fielden.platform.sample.domain.compound.TgCompoundEntityChild;
import ua.com.fielden.platform.sample.domain.compound.TgCompoundEntityDetail;
import ua.com.fielden.platform.sample.domain.compound.TgCompoundEntityLocator;
import ua.com.fielden.platform.sample.domain.compound.master.menu.actions.TgCompoundEntityMaster_OpenMain_MenuItem;
import ua.com.fielden.platform.sample.domain.compound.master.menu.actions.TgCompoundEntityMaster_OpenTgCompoundEntityChild_MenuItem;
import ua.com.fielden.platform.sample.domain.compound.master.menu.actions.TgCompoundEntityMaster_OpenTgCompoundEntityDetail_MenuItem;
import ua.com.fielden.platform.sample.domain.compound.ui_actions.OpenTgCompoundEntityMasterAction;
import ua.com.fielden.platform.sample.domain.stream_processors.DumpCsvTxtProcessor;
import ua.com.fielden.platform.sample.domain.ui_actions.MakeCompletedAction;
import ua.com.fielden.platform.serialisation.jackson.entities.OtherEntity;
import ua.com.fielden.platform.web.test.server.master_action.NewEntityAction;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Collections.unmodifiableSet;

/**
 * A temporary class to enlist domain entities for Web UI Testing Server.
 *
 * @author TG Team
 *
 */
public class ApplicationDomain implements IApplicationDomainProvider {
    private static final Set<Class<? extends AbstractEntity<?>>> entityTypes = new LinkedHashSet<>();
    private static final Set<Class<? extends AbstractEntity<?>>> domainTypes = new LinkedHashSet<>();

    private static void add(final Class<? extends AbstractEntity<?>> domainType) {
        entityTypes.add(domainType);
        domainTypes.add(domainType);
    }

    static {
        entityTypes.addAll(PlatformDomainTypes.types);
        add(TgNote.class);
        add(TgPerson.class);
        add(TgPersistentEntityWithProperties.class);
        add(TgEntityWithRichTextProp.class);
        add(TgEntityWithRichTextRef.class);
        add(TgPersistentEntityWithPropertiesAttachment.class);
        add(TgExportFunctionalEntity.class);
        add(TgPersistentCompositeEntity.class);
        add(TgRollingStockMinorComponent.class);
        add(TgRollingStockMajorComponent.class);
        add(TgMinorComponent.class);
        add(TgFunctionalEntityWithCentreContext.class);
        add(TgStatusActivationFunctionalEntity.class);
        add(TgISStatusActivationFunctionalEntity.class);
        add(TgIRStatusActivationFunctionalEntity.class);
        add(TgONStatusActivationFunctionalEntity.class);
        add(TgSRStatusActivationFunctionalEntity.class);
        add(TgPersistentStatus.class);
        add(TgFetchProviderTestEntity.class);
        add(TgCollectionalSerialisationParent.class);
        add(TgCollectionalSerialisationChild.class);
        add(TgCentreInvokerWithCentreContext.class);
        add(TgEntityForColourMaster.class);
        add(TgCreatePersistentStatusAction.class);
        add(TgDummyAction.class);
        add(TgNoopAction.class);
        add(TgEntityWithPropertyDependency.class);
        add(TgEntityWithPropertyDescriptor.class);
        add(TgEntityWithPropertyDescriptorExt.class);
        add(DumpCsvTxtProcessor.class);
        add(NewEntityAction.class);
        add(ExportAction.class);
        add(TgDeletionTestEntity.class);
        add(TgEntityWithTimeZoneDates.class);
        add(TgGeneratedEntity.class);
        add(TgGeneratedEntityForTrippleDecAnalysis.class);
        add(TgGeneratedEntityForTrippleDecAnalysisInsertionPoint.class);
        add(TgOpenTrippleDecDetails.class);
        add(OtherEntity.class);
        add(MoreDataForDeleteEntity.class);

        add(TgVehicle.class);
        add(TgVehicleFinDetails.class);
        add(TgVehicleModel.class);
        add(TgVehicleMake.class);
        add(TgVehicleFuelUsage.class);
        add(TgOrgUnit1.class);
        add(TgOrgUnit2.class);
        add(TgOrgUnit3.class);
        add(TgOrgUnit4.class);
        add(TgOrgUnit5.class);
        add(TeNamedValuesVector.class);
        add(TeProductPrice.class);
        add(TeVehicle.class);
        add(TeVehicleFinDetails.class);
        add(TeVehicleFuelUsage.class);
        add(TeVehicleMake.class);
        add(TeVehicleModel.class);
        add(TgFuelType.class);
        add(UnionEntity.class);
        add(UnionEntityWithoutSecondDescTitle.class);
        add(EntityOne.class);
        add(EntityTwo.class);
        add(EntityThree.class);

        add(TgBogieLocation.class);
        add(UnionEntityWithSkipExistsValidation.class);
        add(EntityWithUnionEntityWithSkipExistsValidation.class);
        add(TgWorkshop.class);
        add(TgWagonSlot.class);
        add(TgWagon.class);
        add(TgBogie.class);
        add(TgWagonClass.class);
        add(TgWagonClassCompatibility.class);
        add(TgBogieClass.class);
        add(TgFuelUsage.class);
        add(TgSelectedEntitiesExampleAction.class);
        add(TgCloseLeaveExample.class);
        add(OpenTgCloseLeaveExampleMasterAction.class);
        add(TgCloseLeaveExampleMaster_OpenMain_MenuItem.class);
        add(TgCloseLeaveExampleMaster_OpenDetail_MenuItem.class);
        add(TgCloseLeaveExampleDetail.class);
        add(TgCloseLeaveExampleDetailUnpersisted.class);
        add(TgCloseLeaveExampleMaster_OpenDetailUnpersisted_MenuItem.class);
        add(TgCompoundEntity.class);
        add(TgCompoundEntityLocator.class);
        add(TgCompoundEntityDetail.class);
        add(TgCompoundEntityChild.class);
        add(OpenTgCompoundEntityMasterAction.class);
        add(TgCompoundEntityMaster_OpenMain_MenuItem.class);
        add(TgCompoundEntityMaster_OpenTgCompoundEntityDetail_MenuItem.class);
        add(TgCompoundEntityMaster_OpenTgCompoundEntityChild_MenuItem.class);
        add(TgFuelType.class);

        add(TgEntityStringKey.class);
        add(TgEntityBooleanKey.class);
        add(TgEntityDateKey.class);
        add(TgEntityIntegerKey.class);
        add(TgEntityLongKey.class);
        add(TgEntityEntityKey.class);
        add(TgEntityCompositeKey.class);
        add(TgEntityCompositeBooleanKey.class);
        add(TgEntityTwoEntityKeys.class);
        add(TgEntityBigDecimalKey.class);

        add(TgWebApiEntity.class);
        add(TgWebApiEntitySyntheticSingle.class);
        add(TgWebApiEntitySyntheticMulti.class);

        add(TgUnionHolder.class);
        add(TgUnion.class);
        add(TgUnionType1.class);
        add(TgUnionType2.class);
        add(TgUnionCommonType.class);

        add(TgSynBogie.class);
        add(MakeCompletedAction.class);

        add(TgMeterReading.class);
        add(TgReMaxVehicleReading.class);
        add(TgPersonName.class);
        add(TgAuthor.class);
        add(TgAuthorship.class);
        add(TgAuthorRoyalty.class);
        add(TgReBogieWithHighLoad.class);
        add(TgTimesheet.class);
        add(TgReVehicleWithHighPrice.class);
        add(TgVehicleTechDetails.class);
        add(TgReVehicleModel.class);
        add(TgMakeCount.class);
        add(TgOrgUnit5WithSummaries.class);
        add(TgWorkOrder.class);
        add(TeWorkOrder.class);
        add(TgModelCount.class);
        add(TgModelYearCount.class);
        add(TgPublishedYearly.class);
        add(TgAverageFuelUsage.class);
        add(TeAverageFuelUsage.class);
        add(TeFuelUsageByType.class);
        add(TgSystem.class);
        add(TgSubSystem.class);
        add(TgCategory.class);
        add(TgAuthoriser.class);
        add(TgOriginator.class);
        add(TgEntityWithComplexSummaries.class);
        add(TgEntityWithComplexSummariesThatActuallyDeclareThoseSummaries.class);
        add(TgCentreDiffSerialisation.class);
        add(TgCentreDiffSerialisationPersistentChild.class);
        add(TgCentreDiffSerialisationNonPersistentChild.class);
        add(TgCentreDiffSerialisationNonPersistentCompositeChild.class);
        add(TgCategoryAttachment.class);
        add(TgDateTestEntity.class);
    }

    @Override
    public List<Class<? extends AbstractEntity<?>>> entityTypes() {
        return Collections.unmodifiableList(entityTypes.stream().toList());
    }

    public static List<Class<? extends AbstractEntity<?>>> domainTypes() {
        return Collections.unmodifiableList(domainTypes.stream().toList());
    }

    public static Set<Class<? extends AbstractEntity<?>>> entityTypesSet() {
        return unmodifiableSet(entityTypes);
    }

}
