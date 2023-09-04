package ua.com.fielden.platform.web.test.config;

import static java.util.Collections.unmodifiableSet;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import fielden.test_app.close_leave.OpenTgCloseLeaveExampleMasterAction;
import fielden.test_app.close_leave.TgCloseLeaveExample;
import fielden.test_app.close_leave.TgCloseLeaveExampleDetail;
import fielden.test_app.close_leave.TgCloseLeaveExampleDetailUnpersisted;
import fielden.test_app.close_leave.TgCloseLeaveExampleMaster_OpenDetailUnpersisted_MenuItem;
import fielden.test_app.close_leave.TgCloseLeaveExampleMaster_OpenDetail_MenuItem;
import fielden.test_app.close_leave.TgCloseLeaveExampleMaster_OpenMain_MenuItem;
import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.domain.PlatformDomainTypes;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.sample.domain.EntityOne;
import ua.com.fielden.platform.sample.domain.EntityThree;
import ua.com.fielden.platform.sample.domain.EntityTwo;
import ua.com.fielden.platform.sample.domain.EntityWithUnionEntityWithSkipExistsValidation;
import ua.com.fielden.platform.sample.domain.ExportAction;
import ua.com.fielden.platform.sample.domain.MoreDataForDeleteEntity;
import ua.com.fielden.platform.sample.domain.TeVehicle;
import ua.com.fielden.platform.sample.domain.TeVehicleFinDetails;
import ua.com.fielden.platform.sample.domain.TeVehicleFuelUsage;
import ua.com.fielden.platform.sample.domain.TeVehicleMake;
import ua.com.fielden.platform.sample.domain.TeVehicleModel;
import ua.com.fielden.platform.sample.domain.TgBogie;
import ua.com.fielden.platform.sample.domain.TgBogieClass;
import ua.com.fielden.platform.sample.domain.TgBogieLocation;
import ua.com.fielden.platform.sample.domain.TgCentreInvokerWithCentreContext;
import ua.com.fielden.platform.sample.domain.TgCollectionalSerialisationChild;
import ua.com.fielden.platform.sample.domain.TgCollectionalSerialisationParent;
import ua.com.fielden.platform.sample.domain.TgCreatePersistentStatusAction;
import ua.com.fielden.platform.sample.domain.TgDeletionTestEntity;
import ua.com.fielden.platform.sample.domain.TgDummyAction;
import ua.com.fielden.platform.sample.domain.TgEntityBigDecimalKey;
import ua.com.fielden.platform.sample.domain.TgEntityBooleanKey;
import ua.com.fielden.platform.sample.domain.TgEntityCompositeBooleanKey;
import ua.com.fielden.platform.sample.domain.TgEntityCompositeKey;
import ua.com.fielden.platform.sample.domain.TgEntityDateKey;
import ua.com.fielden.platform.sample.domain.TgEntityEntityKey;
import ua.com.fielden.platform.sample.domain.TgEntityForColourMaster;
import ua.com.fielden.platform.sample.domain.TgEntityIntegerKey;
import ua.com.fielden.platform.sample.domain.TgEntityLongKey;
import ua.com.fielden.platform.sample.domain.TgEntityStringKey;
import ua.com.fielden.platform.sample.domain.TgEntityTwoEntityKeys;
import ua.com.fielden.platform.sample.domain.TgEntityWithPropertyDependency;
import ua.com.fielden.platform.sample.domain.TgEntityWithPropertyDescriptor;
import ua.com.fielden.platform.sample.domain.TgEntityWithPropertyDescriptorExt;
import ua.com.fielden.platform.sample.domain.TgEntityWithTimeZoneDates;
import ua.com.fielden.platform.sample.domain.TgExportFunctionalEntity;
import ua.com.fielden.platform.sample.domain.TgFetchProviderTestEntity;
import ua.com.fielden.platform.sample.domain.TgFuelType;
import ua.com.fielden.platform.sample.domain.TgFuelUsage;
import ua.com.fielden.platform.sample.domain.TgFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.sample.domain.TgGeneratedEntity;
import ua.com.fielden.platform.sample.domain.TgGeneratedEntityForTrippleDecAnalysis;
import ua.com.fielden.platform.sample.domain.TgGeneratedEntityForTrippleDecAnalysisInsertionPoint;
import ua.com.fielden.platform.sample.domain.TgIRStatusActivationFunctionalEntity;
import ua.com.fielden.platform.sample.domain.TgISStatusActivationFunctionalEntity;
import ua.com.fielden.platform.sample.domain.TgONStatusActivationFunctionalEntity;
import ua.com.fielden.platform.sample.domain.TgOpenTrippleDecDetails;
import ua.com.fielden.platform.sample.domain.TgOrgUnit1;
import ua.com.fielden.platform.sample.domain.TgOrgUnit2;
import ua.com.fielden.platform.sample.domain.TgOrgUnit3;
import ua.com.fielden.platform.sample.domain.TgOrgUnit4;
import ua.com.fielden.platform.sample.domain.TgOrgUnit5;
import ua.com.fielden.platform.sample.domain.TgPersistentCompositeEntity;
import ua.com.fielden.platform.sample.domain.TgPersistentEntityWithProperties;
import ua.com.fielden.platform.sample.domain.TgPersistentEntityWithPropertiesAttachment;
import ua.com.fielden.platform.sample.domain.TgPersistentStatus;
import ua.com.fielden.platform.sample.domain.TgPerson;
import ua.com.fielden.platform.sample.domain.TgSRStatusActivationFunctionalEntity;
import ua.com.fielden.platform.sample.domain.TgSelectedEntitiesExampleAction;
import ua.com.fielden.platform.sample.domain.TgStatusActivationFunctionalEntity;
import ua.com.fielden.platform.sample.domain.TgSynBogie;
import ua.com.fielden.platform.sample.domain.TgUnion;
import ua.com.fielden.platform.sample.domain.TgUnionCommonType;
import ua.com.fielden.platform.sample.domain.TgUnionHolder;
import ua.com.fielden.platform.sample.domain.TgUnionType1;
import ua.com.fielden.platform.sample.domain.TgUnionType2;
import ua.com.fielden.platform.sample.domain.TgVehicle;
import ua.com.fielden.platform.sample.domain.TgVehicleFinDetails;
import ua.com.fielden.platform.sample.domain.TgVehicleFuelUsage;
import ua.com.fielden.platform.sample.domain.TgVehicleMake;
import ua.com.fielden.platform.sample.domain.TgVehicleModel;
import ua.com.fielden.platform.sample.domain.TgWagon;
import ua.com.fielden.platform.sample.domain.TgWagonClass;
import ua.com.fielden.platform.sample.domain.TgWagonClassCompatibility;
import ua.com.fielden.platform.sample.domain.TgWagonSlot;
import ua.com.fielden.platform.sample.domain.TgWebApiEntity;
import ua.com.fielden.platform.sample.domain.TgWebApiEntitySyntheticMulti;
import ua.com.fielden.platform.sample.domain.TgWebApiEntitySyntheticSingle;
import ua.com.fielden.platform.sample.domain.TgWorkshop;
import ua.com.fielden.platform.sample.domain.UnionEntity;
import ua.com.fielden.platform.sample.domain.UnionEntityWithSkipExistsValidation;
import ua.com.fielden.platform.sample.domain.UnionEntityWithoutSecondDescTitle;
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
        add(TgPerson.class);
        add(TgPersistentEntityWithProperties.class);
        add(TgPersistentEntityWithPropertiesAttachment.class);
        add(TgExportFunctionalEntity.class);
        add(TgPersistentCompositeEntity.class);
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
    }

    @Override
    public List<Class<? extends AbstractEntity<?>>> entityTypes() {
        return Collections.unmodifiableList(entityTypes.stream().collect(Collectors.toList()));
    }

    public static List<Class<? extends AbstractEntity<?>>> domainTypes() {
        return Collections.unmodifiableList(domainTypes.stream().collect(Collectors.toList()));
    }

    public static Set<Class<? extends AbstractEntity<?>>> entityTypesSet() {
        return unmodifiableSet(entityTypes);
    }

}