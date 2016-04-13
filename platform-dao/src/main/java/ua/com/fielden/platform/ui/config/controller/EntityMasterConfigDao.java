package ua.com.fielden.platform.ui.config.controller;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.ui.config.EntityMasterConfig;
import ua.com.fielden.platform.ui.config.api.IEntityMasterConfig;

import com.google.inject.Inject;

/**
 * 
 * DAO implementation of {@link IEntityMasterConfig}.
 * 
 * @author TG Team
 * 
 */
@EntityType(EntityMasterConfig.class)
public class EntityMasterConfigDao extends CommonEntityDao<EntityMasterConfig> implements IEntityMasterConfig {

    @Inject
    protected EntityMasterConfigDao(final IFilter filter) {
        super(filter);
    }
    
    @Override
    @SessionRequired
    public int batchDelete(EntityResultQueryModel<EntityMasterConfig> model) {
        return defaultBatchDelete(model);
    }

}
