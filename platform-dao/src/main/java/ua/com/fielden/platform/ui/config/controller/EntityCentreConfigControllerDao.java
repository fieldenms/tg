package ua.com.fielden.platform.ui.config.controller;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.equery.interfaces.IFilter;
import ua.com.fielden.platform.equery.interfaces.IQueryOrderedModel;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.ui.config.EntityCentreConfig;
import ua.com.fielden.platform.ui.config.api.IEntityCentreConfigController;

import com.google.inject.Inject;

/**
 * DAO implementation of {@link IEntityCentreConfigController}.
 *
 * @author TG Team
 *
 */
@EntityType(EntityCentreConfig.class)
public class EntityCentreConfigControllerDao extends CommonEntityDao<EntityCentreConfig> implements IEntityCentreConfigController {

    @Inject
    protected EntityCentreConfigControllerDao(final IFilter filter) {
	super(filter);
    }

    @Override
    @SessionRequired
    public void delete(final EntityCentreConfig entity) {
	defaultDelete(entity);
    }

    @Override
    public void delete(final IQueryOrderedModel<EntityCentreConfig> model) {
        defaultDelete(model);
    }

}
