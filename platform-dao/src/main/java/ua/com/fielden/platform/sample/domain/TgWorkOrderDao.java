package ua.com.fielden.platform.sample.domain;

import java.util.Map;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.sample.domain.ITgWorkOrder;
import ua.com.fielden.platform.sample.domain.TgWorkOrder;

import com.google.inject.Inject;

/**
 * DAO for retrieving workorders.
 * 
 * @author TG Team
 * 
 */
@EntityType(TgWorkOrder.class)
public class TgWorkOrderDao extends CommonEntityDao<TgWorkOrder> implements ITgWorkOrder {

    @Inject
    protected TgWorkOrderDao(final IFilter filter) {
        super(filter);
    }

    @Override
    @SessionRequired
    public void delete(final EntityResultQueryModel<TgWorkOrder> entityModel, final Map<String, Object> paramValues) {
        defaultDelete(entityModel, paramValues);
    }
}
