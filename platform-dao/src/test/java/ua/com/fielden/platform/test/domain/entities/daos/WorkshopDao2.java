package ua.com.fielden.platform.test.domain.entities.daos;

import ua.com.fielden.platform.dao2.CommonEntityDao2;
import ua.com.fielden.platform.entity.query.IFilter;
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
public class WorkshopDao2 extends CommonEntityDao2<Workshop> implements IWorkshopDao2 {

    @Inject
    protected WorkshopDao2(final IFilter filter) {
	super(filter);
    }
}
