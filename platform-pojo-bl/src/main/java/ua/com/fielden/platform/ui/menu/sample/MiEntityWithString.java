package ua.com.fielden.platform.ui.menu.sample;

import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.serialisation.jackson.entities.EntityWithString;
import ua.com.fielden.platform.ui.menu.MiWithConfigurationSupport;

@EntityType(EntityWithString.class)
public class MiEntityWithString extends MiWithConfigurationSupport<EntityWithString> {
    
}