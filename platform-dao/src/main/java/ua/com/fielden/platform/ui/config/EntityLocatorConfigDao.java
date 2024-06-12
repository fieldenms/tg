package ua.com.fielden.platform.ui.config;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.ui.config.EntityLocatorConfig;
import ua.com.fielden.platform.ui.config.EntityLocatorConfigCo;

/**
 * DAO implementation of {@link EntityLocatorConfigCo}.
 *
 * @author TG Team
 */
@EntityType(EntityLocatorConfig.class)
public class EntityLocatorConfigDao extends CommonEntityDao<EntityLocatorConfig> implements EntityLocatorConfigCo {

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
