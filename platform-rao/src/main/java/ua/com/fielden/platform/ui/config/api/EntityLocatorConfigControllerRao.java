package ua.com.fielden.platform.ui.config.api;

import ua.com.fielden.platform.rao.CommonEntityRao;
import ua.com.fielden.platform.rao.RestClientUtil;
import ua.com.fielden.platform.rao.WebResourceType;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.ui.config.EntityLocatorConfig;

import com.google.inject.Inject;

/**
 * RAO implementation of {@link IEntityLocatorConfig}.
 * 
 * @author TG Team
 * 
 */
@EntityType(EntityLocatorConfig.class)
public class EntityLocatorConfigControllerRao extends CommonEntityRao<EntityLocatorConfig> implements IEntityLocatorConfig {

    @Inject
    public EntityLocatorConfigControllerRao(final RestClientUtil restUtil) {
        super(restUtil);
    }

    protected WebResourceType getDefaultWebResourceType() {
        return WebResourceType.SYSTEM;
    }

}
