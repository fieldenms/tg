package ua.com.fielden.platform.ui.config.controller;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.ui.config.EntityMasterConfig;
import ua.com.fielden.platform.ui.config.api.IEntityMasterConfigController;

import com.google.inject.Inject;

/**
 * 
 * DAO implementation of {@link IEntityMasterConfigController}.
 * 
 * @author TG Team
 * 
 */
@EntityType(EntityMasterConfig.class)
public class EntityMasterConfigControllerDao extends CommonEntityDao<EntityMasterConfig> implements IEntityMasterConfigController {

    @Inject
    protected EntityMasterConfigControllerDao(final IFilter filter) {
        super(filter);
    }

}
