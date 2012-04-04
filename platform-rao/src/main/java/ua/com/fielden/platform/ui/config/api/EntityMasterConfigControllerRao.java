package ua.com.fielden.platform.ui.config.api;

import ua.com.fielden.platform.rao.CommonEntityRao;
import ua.com.fielden.platform.rao.RestClientUtil;
import ua.com.fielden.platform.rao.WebResourceType;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.ui.config.EntityMasterConfig;

import com.google.inject.Inject;

/**
 *
 * RAO implementation of {@link IEntityMasterConfigController}.
 *
 * @author TG Team
 *
 */
@EntityType(EntityMasterConfig.class)
public class EntityMasterConfigControllerRao extends CommonEntityRao<EntityMasterConfig> implements IEntityMasterConfigController {

    @Inject
    public EntityMasterConfigControllerRao(final RestClientUtil restUtil) {
	super(restUtil);
    }

    protected WebResourceType getDefaultWebResourceType() {
	return WebResourceType.SYSTEM;
    }

}
