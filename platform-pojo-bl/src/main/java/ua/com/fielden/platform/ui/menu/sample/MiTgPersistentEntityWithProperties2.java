package ua.com.fielden.platform.ui.menu.sample;

import ua.com.fielden.platform.sample.domain.TgPersistentEntityWithProperties;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.ui.menu.MiWithConfigurationSupport;

@EntityType(TgPersistentEntityWithProperties.class)
public class MiTgPersistentEntityWithProperties2 extends MiWithConfigurationSupport<TgPersistentEntityWithProperties> {
    private static final long serialVersionUID = 1L;

    private static final String caption = "Tg Persistent Entity With Properties 2";
    private static final String description = "<html>" + "<h3>Tg Persistent Entity With Properties Centre</h3>"
            + "A facility to query Tg Persistent Entity With Properties information.</html>";
}