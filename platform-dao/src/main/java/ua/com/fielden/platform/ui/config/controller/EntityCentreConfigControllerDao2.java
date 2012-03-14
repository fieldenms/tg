package ua.com.fielden.platform.ui.config.controller;

import java.util.Map;

import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.dao2.CommonEntityDao2;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.ui.config.EntityCentreConfig;
import ua.com.fielden.platform.ui.config.api.IEntityCentreConfigController2;

import com.google.inject.Inject;

/**
 * DAO implementation of {@link IEntityCentreConfigController2}.
 *
 * @author TG Team
 *
 */
@EntityType(EntityCentreConfig.class)
public class EntityCentreConfigControllerDao2 extends CommonEntityDao2<EntityCentreConfig> implements IEntityCentreConfigController2 {

    @Inject
    protected EntityCentreConfigControllerDao2(final IFilter filter) {
	super(filter);
    }

    @Override
    @SessionRequired
    public void delete(final EntityCentreConfig entity) {
	defaultDelete(entity);
    }

    @Override
    public void delete(final EntityResultQueryModel<EntityCentreConfig> model, final Map<String, Object> paramValues) {
        defaultDelete(model, paramValues);
    }
}
