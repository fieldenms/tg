package ua.com.fielden.platform.example.entities.daos;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.equery.interfaces.IFilter;
import ua.com.fielden.platform.example.entities.IWorkorderDao;
import ua.com.fielden.platform.example.entities.WorkOrder;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

/**
 * DAO for retrieving work orders.
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
}
