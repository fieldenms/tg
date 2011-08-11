package ua.com.fielden.platform.test.domain.entities.daos;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.equery.interfaces.IFilter;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.test.domain.entities.Workshop;

import com.google.inject.Inject;

/**
 * DAO for retrieving workshop related data: workshop itself, contained rotables, existing active workorders.
 *
 * @author TG Team
 *
 */
@EntityType(Workshop.class)
public class WorkshopDao extends CommonEntityDao<Workshop> implements IWorkshopDao {

    @Inject
    protected WorkshopDao(final IFilter filter) {
	super(filter);
    }
}
