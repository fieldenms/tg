package ua.com.fielden.platform.ui.config.api;

import ua.com.fielden.platform.rao.CommonEntityRao;
import ua.com.fielden.platform.rao.RestClientUtil;
import ua.com.fielden.platform.rao.WebResourceType;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.ui.config.EntityCentreConfig;

import com.google.inject.Inject;

/**
 * RAO implementation of {@link IEntityCentreConfigController}.
 *
 * @author TG Team
 *
 */
@EntityType(EntityCentreConfig.class)
public class EntityCentreConfigControllerRao extends CommonEntityRao<EntityCentreConfig> implements IEntityCentreConfigController {

    @Inject
    public EntityCentreConfigControllerRao(final RestClientUtil restUtil) {
	super(restUtil);
    }

    protected WebResourceType getDefaultWebResourceType() {
	return WebResourceType.SYSTEM;
    }
}
