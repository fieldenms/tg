package ua.com.fielden.platform.web.config;

import com.google.inject.Injector;

import ua.com.fielden.platform.entity.EntityEditAction;
import ua.com.fielden.platform.entity.EntityEditActionProducer;
import ua.com.fielden.platform.entity.EntityNewAction;
import ua.com.fielden.platform.entity.EntityNewActionProducer;
import ua.com.fielden.platform.web.view.master.EntityMaster;
import ua.com.fielden.platform.web.view.master.api.with_master.impl.EntityManipulationMasterBuilder;

public class EntityManipulationWebUiConfig {

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
