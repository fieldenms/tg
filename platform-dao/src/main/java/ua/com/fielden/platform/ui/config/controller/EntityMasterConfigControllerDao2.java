package ua.com.fielden.platform.ui.config.controller;

import ua.com.fielden.platform.dao2.CommonEntityDao2;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.ui.config.EntityMasterConfig;
import ua.com.fielden.platform.ui.config.api.IEntityMasterConfigController;
import ua.com.fielden.platform.ui.config.api.IEntityMasterConfigController2;

import com.google.inject.Inject;

/**
 *
 * DAO implementation of {@link IEntityMasterConfigController}.
 *
 * @author TG Team
 *
 */
@EntityType(EntityMasterConfig.class)
public class EntityMasterConfigControllerDao2 extends CommonEntityDao2<EntityMasterConfig> implements IEntityMasterConfigController2 {

    @Inject
    protected EntityMasterConfigControllerDao2(final IFilter filter) {
	super(filter);
    }

}
