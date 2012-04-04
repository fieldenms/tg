package ua.com.fielden.platform.ui.config.controller;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.ui.config.EntityLocatorConfig;
import ua.com.fielden.platform.ui.config.api.IEntityLocatorConfigController;

import com.google.inject.Inject;

/**
 * DAO implementation of {@link IEntityLocatorConfigController}.
 *
 * @author TG Team
 *
 */
@EntityType(EntityLocatorConfig.class)
public class EntityLocatorConfigControllerDao extends CommonEntityDao<EntityLocatorConfig> implements IEntityLocatorConfigController {

    @Inject
    protected EntityLocatorConfigControllerDao(final IFilter filter) {
	super(filter);
    }

    @Override
    @SessionRequired
    public void delete(final EntityLocatorConfig entity) {
	defaultDelete(entity);
    }
}