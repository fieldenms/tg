package ua.com.fielden.platform.sample.domain;

import java.util.Map;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;

/**
 * DAO for retrieving workorders.
 *
 * @author TG Team
 */
@EntityType(TeWorkOrder.class)
public class TeWorkOrderDao extends CommonEntityDao<TeWorkOrder> implements ITeWorkOrder {

    @Override
    @SessionRequired
    public void delete(final EntityResultQueryModel<TeWorkOrder> entityModel, final Map<String, Object> paramValues) {
        defaultDelete(entityModel, paramValues);
    }

}
