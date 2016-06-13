package ua.com.fielden.platform.ui.menu.sample;

import ua.com.fielden.platform.sample.domain.TgEntityWithPropertyDependency;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.ui.menu.MiWithConfigurationSupport;

@EntityType(TgEntityWithPropertyDependency.class)
public class MiTgEntityWithPropertyDependency extends MiWithConfigurationSupport<TgEntityWithPropertyDependency> {
    private static final long serialVersionUID = 1L;

    private static final String caption = "Tg Entity With Property Dependency";
    private static final String description = "<html>" + "<h3>Tg Entity With Property Dependency Centre</h3>"
            + "A facility to query Tg Entity With Property Dependency information.</html>";
}