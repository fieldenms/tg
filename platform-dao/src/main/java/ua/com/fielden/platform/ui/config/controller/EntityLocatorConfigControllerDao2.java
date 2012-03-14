package ua.com.fielden.platform.ui.config.controller;

import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.dao2.CommonEntityDao2;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.ui.config.EntityLocatorConfig;
import ua.com.fielden.platform.ui.config.api.IEntityLocatorConfigController2;

import com.google.inject.Inject;

/**
 * DAO implementation of {@link IEntityLocatorConfigController2}.
 *
 * @author TG Team
 *
 */
@EntityType(EntityLocatorConfig.class)
public class EntityLocatorConfigControllerDao2 extends CommonEntityDao2<EntityLocatorConfig> implements IEntityLocatorConfigController2 {

    @Inject
    protected EntityLocatorConfigControllerDao2(final IFilter filter) {
	super(filter);
    }

    @Override
    @SessionRequired
    public void delete(final EntityLocatorConfig entity) {
	defaultDelete(entity);
    }
}