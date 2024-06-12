package ua.com.fielden.platform.ui.config;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.ui.config.EntityMasterConfig;
import ua.com.fielden.platform.ui.config.EntityMasterConfigCo;

import com.google.inject.Inject;

/**
 * DAO implementation of {@link EntityMasterConfigCo}.
 *
 * @author TG Team
 */
@EntityType(EntityMasterConfig.class)
public class EntityMasterConfigDao extends CommonEntityDao<EntityMasterConfig> implements EntityMasterConfigCo {

    @Override
    @SessionRequired
    public int batchDelete(EntityResultQueryModel<EntityMasterConfig> model) {
        return defaultBatchDelete(model);
    }

}
