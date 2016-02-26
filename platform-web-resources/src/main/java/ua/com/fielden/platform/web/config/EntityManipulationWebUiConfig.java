package ua.com.fielden.platform.web.config;

import ua.com.fielden.platform.entity.EntityEditAction;
import ua.com.fielden.platform.entity.EntityEditActionProducer;
import ua.com.fielden.platform.entity.EntityNewAction;
import ua.com.fielden.platform.entity.EntityNewActionProducer;
import ua.com.fielden.platform.web.view.master.EntityMaster;
import ua.com.fielden.platform.web.view.master.api.IMaster;
import ua.com.fielden.platform.web.view.master.entity_manipulation.EntityManipulationMaster;

import com.google.inject.Injector;

public class EntityManipulationWebUiConfig {

    public static EntityMaster<EntityNewAction> createEntityNewMaster(final Injector injector) {
        final IMaster<EntityNewAction> manipulationMaster = new EntityManipulationMaster<EntityNewAction>(EntityNewAction.class);
        return new EntityMaster<EntityNewAction>(EntityNewAction.class, EntityNewActionProducer.class, manipulationMaster, injector);
    }

    public static EntityMaster<EntityEditAction> createEntityEditMaster(final Injector injector) {
        final IMaster<EntityEditAction> manipulationMaster = new EntityManipulationMaster<EntityEditAction>(EntityEditAction.class);
        return new EntityMaster<EntityEditAction>(EntityEditAction.class, EntityEditActionProducer.class, manipulationMaster, injector);
    }
}
