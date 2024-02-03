package ua.com.fielden.platform.ui.menu.sample;

import static org.apache.logging.log4j.LogManager.getLogger;

import org.apache.logging.log4j.Logger;

import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.sample.domain.TgFetchProviderTestEntity;
import ua.com.fielden.platform.ui.menu.MiWithConfigurationSupport;

@EntityType(TgFetchProviderTestEntity.class)
public class MiTgFetchProviderTestEntity extends MiWithConfigurationSupport<TgFetchProviderTestEntity> {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = getLogger(MiTgFetchProviderTestEntity.class);

    private static final String caption = "TgFetchProviderTestEntity";
    private static final String description = "<html>" + "<h3>TgFetchProviderTestEntity Centre</h3>"
            + //
            "A facility to query TgFetchProviderTestEntity information.</html>";
}