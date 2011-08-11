package ua.com.fielden.platform.test.domain.entities.daos;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.equery.interfaces.IFilter;
import ua.com.fielden.platform.equery.interfaces.IQueryOrderedModel;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.test.domain.entities.WorkOrder;

import com.google.inject.Inject;

/**
 * DAO for retrieving workorders.
 *
 * @author TG Team
 *
 */
@EntityType(WorkOrder.class)
public class WorkorderDao extends CommonEntityDao<WorkOrder> implements IWorkorderDao {

    @Inject
    protected WorkorderDao(final IFilter filter) {
	super(filter);
    }

    @Override
    @SessionRequired
    public void delete(final IQueryOrderedModel<WorkOrder> entityModel) {
	defaultDelete(entityModel);
    }
}
