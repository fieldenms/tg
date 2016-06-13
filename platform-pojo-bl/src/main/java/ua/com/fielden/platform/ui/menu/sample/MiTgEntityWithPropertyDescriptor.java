package ua.com.fielden.platform.ui.menu.sample;

import ua.com.fielden.platform.sample.domain.TgEntityWithPropertyDescriptor;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.ui.menu.MiWithConfigurationSupport;

@EntityType(TgEntityWithPropertyDescriptor.class)
public class MiTgEntityWithPropertyDescriptor extends MiWithConfigurationSupport<TgEntityWithPropertyDescriptor> {
    private static final long serialVersionUID = 1L;

    private static final String caption = "Tg Entity With Property Descriptor";
    private static final String description = "<html>" + "<h3>Tg Entity With Property Descriptor Centre</h3>"
            + "A facility to query Tg Entity With Property Descriptor information.</html>";
}