package ua.com.fielden.platform.ui.menu.sample;

import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.sample.domain.TgPersistentEntityWithProperties;
import ua.com.fielden.platform.ui.menu.MiWithConfigurationSupport;

@EntityType(TgPersistentEntityWithProperties.class)
public class MiEntityCentreNotGenerated extends MiWithConfigurationSupport<TgPersistentEntityWithProperties> {
    private static final long serialVersionUID = 1L;

    private static final String caption = "MiEntityCentreNotGenerated";
    private static final String description = "<html>" + "<h3>Tg Persistent Entity With Properties Centre (not generated)</h3>"
            + "A facility to query Tg Persistent Entity With Properties information.</html>";
}