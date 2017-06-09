package ua.com.fielden.platform.web.test.config;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.domain.PlatformDomainTypes;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.sample.domain.EntityOne;
import ua.com.fielden.platform.sample.domain.EntityTwo;
import ua.com.fielden.platform.sample.domain.ExportAction;
import ua.com.fielden.platform.sample.domain.TgBogie;
import ua.com.fielden.platform.sample.domain.TgBogieLocation;
import ua.com.fielden.platform.sample.domain.TgCentreInvokerWithCentreContext;
import ua.com.fielden.platform.sample.domain.TgCollectionalSerialisationChild;
import ua.com.fielden.platform.sample.domain.TgCollectionalSerialisationParent;
import ua.com.fielden.platform.sample.domain.TgCoordinate;
import ua.com.fielden.platform.sample.domain.TgCreatePersistentStatusAction;
import ua.com.fielden.platform.sample.domain.TgDeletionTestEntity;
import ua.com.fielden.platform.sample.domain.TgDummyAction;
import ua.com.fielden.platform.sample.domain.TgEntityForColourMaster;
import ua.com.fielden.platform.sample.domain.TgEntityWithPropertyDependency;
import ua.com.fielden.platform.sample.domain.TgEntityWithPropertyDescriptor;
import ua.com.fielden.platform.sample.domain.TgEntityWithTimeZoneDates;
import ua.com.fielden.platform.sample.domain.TgExportFunctionalEntity;
import ua.com.fielden.platform.sample.domain.TgFetchProviderTestEntity;
import ua.com.fielden.platform.sample.domain.TgFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.sample.domain.TgGeneratedEntity;
import ua.com.fielden.platform.sample.domain.TgIRStatusActivationFunctionalEntity;
import ua.com.fielden.platform.sample.domain.TgISStatusActivationFunctionalEntity;
import ua.com.fielden.platform.sample.domain.TgMachine;
import ua.com.fielden.platform.sample.domain.TgMachineRealtimeMonitorMap;
import ua.com.fielden.platform.sample.domain.TgMessage;
import ua.com.fielden.platform.sample.domain.TgMessageMap;
import ua.com.fielden.platform.sample.domain.TgONStatusActivationFunctionalEntity;
import ua.com.fielden.platform.sample.domain.TgOrgUnit;
import ua.com.fielden.platform.sample.domain.TgPersistentCompositeEntity;
import ua.com.fielden.platform.sample.domain.TgPersistentEntityWithProperties;
import ua.com.fielden.platform.sample.domain.TgPersistentStatus;
import ua.com.fielden.platform.sample.domain.TgPerson;
import ua.com.fielden.platform.sample.domain.TgPolygon;
import ua.com.fielden.platform.sample.domain.TgPolygonMap;
import ua.com.fielden.platform.sample.domain.TgSRStatusActivationFunctionalEntity;
import ua.com.fielden.platform.sample.domain.TgStatusActivationFunctionalEntity;
import ua.com.fielden.platform.sample.domain.TgStop;
import ua.com.fielden.platform.sample.domain.TgStopMap;
import ua.com.fielden.platform.sample.domain.TgWagon;
import ua.com.fielden.platform.sample.domain.TgWagonClass;
import ua.com.fielden.platform.sample.domain.TgWagonSlot;
import ua.com.fielden.platform.sample.domain.TgWorkshop;
import ua.com.fielden.platform.sample.domain.UnionEntity;
import ua.com.fielden.platform.sample.domain.stream_processors.DumpCsvTxtProcessor;
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
        add(DumpCsvTxtProcessor.class);
        add(NewEntityAction.class);
        add(ExportAction.class);
        add(TgDeletionTestEntity.class);
        add(TgEntityWithTimeZoneDates.class);
        add(TgGeneratedEntity.class);
        add(OtherEntity.class);

        add(TgMessage.class);
        add(TgMessageMap.class);

        add(TgOrgUnit.class);
        add(TgMachine.class);
        add(TgMachineRealtimeMonitorMap.class);

        add(TgStop.class);
        add(TgStopMap.class);

        add(TgPolygon.class);
        add(TgCoordinate.class);
        add(TgPolygonMap.class);

        add(UnionEntity.class);
        add(EntityOne.class);
        add(EntityTwo.class);

        add(TgBogieLocation.class);
        add(TgWorkshop.class);
        add(TgWagonSlot.class);
        add(TgWagon.class);
        add(TgBogie.class);
        add(TgWagonClass.class);
    }

    @Override
    public List<Class<? extends AbstractEntity<?>>> entityTypes() {
        return Collections.unmodifiableList(entityTypes.stream().collect(Collectors.toList()));
    }

    public List<Class<? extends AbstractEntity<?>>> domainTypes() {
        return Collections.unmodifiableList(domainTypes.stream().collect(Collectors.toList()));
    }
}
