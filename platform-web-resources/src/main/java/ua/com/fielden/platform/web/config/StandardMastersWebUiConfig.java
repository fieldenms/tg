package ua.com.fielden.platform.web.config;

import static java.lang.String.format;

import java.util.Optional;

import ua.com.fielden.platform.entity.EntityEditAction;
import ua.com.fielden.platform.entity.EntityEditActionProducer;
import ua.com.fielden.platform.entity.EntityExportAction;
import ua.com.fielden.platform.entity.EntityExportActionProducer;
import ua.com.fielden.platform.entity.EntityNewAction;
import ua.com.fielden.platform.entity.EntityNewActionProducer;
import ua.com.fielden.platform.web.interfaces.ILayout.Device;
import ua.com.fielden.platform.web.view.master.EntityMaster;
import ua.com.fielden.platform.web.view.master.api.IMaster;
import ua.com.fielden.platform.web.view.master.api.actions.MasterActions;
import ua.com.fielden.platform.web.view.master.api.impl.SimpleMasterBuilder;
import ua.com.fielden.platform.web.view.master.api.with_master.impl.EntityManipulationMasterBuilder;

import com.google.inject.Injector;

public class StandardMastersWebUiConfig {

    public static EntityMaster<EntityNewAction> createEntityNewMaster(final Injector injector) {
        return new EntityMaster<EntityNewAction>(EntityNewAction.class,
                EntityNewActionProducer.class,
                new EntityManipulationMasterBuilder<EntityNewAction>()
                /*  */.forEntityWithSaveOnActivate(EntityNewAction.class)
                /*  */.withMaster(null) // the master instance is not passing here, this is generic implementation, and master type is calculated from selection criteria context
                /*  */.done(),
                injector);
    }

    public static EntityMaster<EntityEditAction> createEntityEditMaster(final Injector injector) {
        return new EntityMaster<EntityEditAction>(EntityEditAction.class,
                EntityEditActionProducer.class,
                new EntityManipulationMasterBuilder<EntityEditAction>()
                /*  */.forEntityWithSaveOnActivate(EntityEditAction.class)
                /*  */.withMaster(null) // the master instance is not passing here, this is generic implementation, and master type is calculated from currentEntity context
                /*  */.done(),
                injector);
    }

    public static EntityMaster<EntityExportAction> createExportMaster(final Injector injector) {
        final String actionMr = "'margin: 10px', 'width: 110px'";
        final String inner = "'flex', 'margin-right: 20px'";
        final String outer = "'flex'";

        final String masterLayout = ("['vertical', 'padding:20px',"
                //                                     all
                + format("['horizontal', 'justified', [%s]],", outer)
                //                                    pages
                + format("['horizontal', 'justified', 'margin-top:20px', [%s]],", outer)
                //
                + format("['horizontal', 'justified', 'margin-bottom:20px', 'margin-left:25px', [%s], [%s]],", inner, outer)
                //                                  selected
                + format("['horizontal', 'justified', [%s]],", outer)
                //                                                               Cancel  Export
                + format("['margin-top: 20px', 'wrap', 'justify-content: center', [%s],   [%s]]", actionMr, actionMr)
                + "]");
        final IMaster<EntityExportAction> masterConfig = new SimpleMasterBuilder<EntityExportAction>()
                .forEntity(EntityExportAction.class)
                .addProp("all").asCheckbox()
                .also()
                .addProp("pageRange").asCheckbox()
                .also()
                .addProp("fromPage").asSpinner()
                .also()
                .addProp("toPage").asSpinner()
                .also()
                .addProp("selected").asCheckbox()
                .also()
                .addAction(MasterActions.REFRESH)
                /*      */.shortDesc("CANCEL")
                /*      */.longDesc("Cancel action")
                .addAction(MasterActions.SAVE)
                /*      */.shortDesc("EXPORT")
                /*      */.longDesc("Export action")
                .setLayoutFor(Device.DESKTOP, Optional.empty(), masterLayout)
                .done();
        final EntityMaster<EntityExportAction> master = new EntityMaster<EntityExportAction>(
                EntityExportAction.class,
                EntityExportActionProducer.class,
                masterConfig,
                injector);

        return master;
    }

    // TODO once it will be necessary, uncomment this code to implement generic EDIT / NEW actions with 'no parent centre refresh' capability:
//    public static EntityMaster<EntityNewActionWithNoParentCentreRefresh> createEntityNewMasterWithNoParentCentreRefresh(final Injector injector) {
//        return new EntityMaster<EntityNewAction>(EntityNewActionWithNoParentCentreRefresh.class,
//                EntityNewActionWithNoParentCentreRefreshProducer.class,
//                new EntityManipulationMasterBuilder<EntityNewActionWithNoParentCentreRefresh>()
//                /*  */.forEntityWithSaveOnActivate(EntityNewActionWithNoParentCentreRefresh.class)
//                /*  */.withMasterAndWithNoParentCentreRefresh(null) // the master instance is not passing here, this is generic implementation, and master type is calculated from selection criteria context
//                /*  */.done(),
//                injector);
//    }
//
//    public static EntityMaster<EntityEditActionWithNoParentCentreRefresh> createEntityEditMasterWithNoParentCentreRefresh(final Injector injector) {
//        return new EntityMaster<EntityEditActionWithNoParentCentreRefresh>(EntityEditActionWithNoParentCentreRefresh.class,
//                EntityEditActionWithNoParentCentreRefreshProducer.class,
//                new EntityManipulationMasterBuilder<EntityEditActionWithNoParentCentreRefresh>()
//                /*  */.forEntityWithSaveOnActivate(EntityEditActionWithNoParentCentreRefresh.class)
//                /*  */.withMasterAndWithNoParentCentreRefresh(null) // the master instance is not passing here, this is generic implementation, and master type is calculated from currentEntity context
//                /*  */.done(),
//                injector);
//    }
}
