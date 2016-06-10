package ua.com.fielden.platform.ui.menu.sample;

import org.apache.log4j.Logger;

import ua.com.fielden.platform.sample.domain.TgFetchProviderTestEntity;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.ui.menu.MiWithConfigurationSupport;

@EntityType(TgFetchProviderTestEntity.class)
public class MiTgFetchProviderTestEntity extends MiWithConfigurationSupport<TgFetchProviderTestEntity> {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(MiTgFetchProviderTestEntity.class);

    private static final String caption = "TgFetchProviderTestEntity";
    private static final String description = "<html>" + "<h3>TgFetchProviderTestEntity Centre</h3>"
            + //
            "A facility to query TgFetchProviderTestEntity information.</html>";
}