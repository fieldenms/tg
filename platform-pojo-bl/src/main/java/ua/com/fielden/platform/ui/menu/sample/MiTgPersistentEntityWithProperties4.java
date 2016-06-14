package ua.com.fielden.platform.ui.menu.sample;

import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.sample.domain.TgPersistentEntityWithProperties;
import ua.com.fielden.platform.ui.menu.MiWithConfigurationSupport;

@EntityType(TgPersistentEntityWithProperties.class)
public class MiTgPersistentEntityWithProperties4 extends MiWithConfigurationSupport<TgPersistentEntityWithProperties> {
    private static final long serialVersionUID = 1L;

    private static final String caption = "Tg Persistent Entity With Properties 4";
    private static final String description = "<html>" + "<h3>Tg Persistent Entity With Properties Centre</h3>"
            + "A facility to query Tg Persistent Entity With Properties information.</html>";
}