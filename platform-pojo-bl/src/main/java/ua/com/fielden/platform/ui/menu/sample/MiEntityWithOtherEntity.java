package ua.com.fielden.platform.ui.menu.sample;

import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.serialisation.jackson.entities.EntityWithOtherEntity;
import ua.com.fielden.platform.ui.menu.MiWithConfigurationSupport;

@EntityType(EntityWithOtherEntity.class)
public class MiEntityWithOtherEntity extends MiWithConfigurationSupport<EntityWithOtherEntity> {
    
}