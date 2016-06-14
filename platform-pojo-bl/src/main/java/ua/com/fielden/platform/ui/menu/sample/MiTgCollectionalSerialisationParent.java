package ua.com.fielden.platform.ui.menu.sample;

import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.sample.domain.TgCollectionalSerialisationParent;
import ua.com.fielden.platform.ui.menu.MiWithConfigurationSupport;

@EntityType(TgCollectionalSerialisationParent.class)
public class MiTgCollectionalSerialisationParent extends MiWithConfigurationSupport<TgCollectionalSerialisationParent> {
    private static final long serialVersionUID = 1L;

    private static final String caption = "TgCollectionalSerialisationParent";
    private static final String description = "<html>" + "<h3>TgCollectionalSerialisationParent Centre</h3>"
            + //
            "A facility to query TgCollectionalSerialisationParent information.</html>";
}