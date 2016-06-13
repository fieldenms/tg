package ua.com.fielden.platform.ui.menu.sample;

import ua.com.fielden.platform.sample.domain.TgPersistentEntityWithProperties;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.ui.menu.MiWithConfigurationSupport;

@EntityType(TgPersistentEntityWithProperties.class)
public class MiDetailsCentre extends MiWithConfigurationSupport<TgPersistentEntityWithProperties> {
    private static final long serialVersionUID = 1L;

    private static final String caption = "Tg Persistent Entity With Properties (Details Centre)";
    private static final String description = "<html>" + "<h3>Tg Persistent Entity With Properties (Details Centre)</h3>"
            + "A facility to query Tg Persistent Entity With Properties information.</html>";
}