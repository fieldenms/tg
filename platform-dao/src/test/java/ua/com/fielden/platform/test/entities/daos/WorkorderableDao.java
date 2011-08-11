package ua.com.fielden.platform.test.entities.daos;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.equery.interfaces.IFilter;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.test.entities.Workorderable;

import com.google.inject.Inject;

/**
 * DAO for retrieving workorders.
 *
 * @author TG Team
 *
 */
@EntityType(Workorderable.class)
public class WorkorderableDao extends CommonEntityDao<Workorderable> implements IWorkorderableDao {

    @Inject
    protected WorkorderableDao(final IFilter filter) {
	super(filter);
    }

}
