package ua.com.fielden.platform.ui.config.controller;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.ui.config.EntityLocatorConfig;
import ua.com.fielden.platform.ui.config.api.IEntityLocatorConfig;

/**
 * DAO implementation of {@link IEntityLocatorConfig}.
 * 
 * @author TG Team
 * 
 */
@EntityType(EntityLocatorConfig.class)
public class EntityLocatorConfigDao extends CommonEntityDao<EntityLocatorConfig> implements IEntityLocatorConfig {

    @Inject
    protected EntityLocatorConfigDao(final IFilter filter) {
        super(filter);
    }

    @Override
    @SessionRequired
    public void delete(final EntityLocatorConfig entity) {
        defaultDelete(entity);
    }
    
    @Override
    @SessionRequired
    public int batchDelete(EntityResultQueryModel<EntityLocatorConfig> model) {
        return defaultBatchDelete(model);
    }
}