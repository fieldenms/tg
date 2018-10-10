package ua.com.fielden.platform.ui.menu.sample;

import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.serialisation.jackson.entities.EmptyEntity;
import ua.com.fielden.platform.ui.menu.MiWithConfigurationSupport;

@EntityType(EmptyEntity.class)
public class MiEmptyEntity extends MiWithConfigurationSupport<EmptyEntity> {
    private static final long serialVersionUID = 1L;

    private static final String caption = "MiEmptyEntity";
    private static final String description = "<html>" + "<h3>EmptyEntity Centre</h3>"
            + "A facility to query EmptyEntity information.</html>";
}