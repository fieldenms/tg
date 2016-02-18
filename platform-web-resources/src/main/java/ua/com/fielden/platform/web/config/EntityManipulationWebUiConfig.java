package ua.com.fielden.platform.web.config;

import ua.com.fielden.platform.entity.EntityManipulationAction;
import ua.com.fielden.platform.entity.EntityManipulationActionProducer;
import ua.com.fielden.platform.web.view.master.EntityMaster;
import ua.com.fielden.platform.web.view.master.api.IMaster;
import ua.com.fielden.platform.web.view.master.entity_manipulation.EntityManipulationMaster;

import com.google.inject.Injector;

public class EntityManipulationWebUiConfig {

    public static EntityMaster<EntityManipulationAction> createMaster(final Injector injector) {
        final IMaster<EntityManipulationAction> manipulationMaster = new EntityManipulationMaster();
        return new EntityMaster<EntityManipulationAction>(EntityManipulationAction.class, EntityManipulationActionProducer.class, manipulationMaster, injector);
    }
}
