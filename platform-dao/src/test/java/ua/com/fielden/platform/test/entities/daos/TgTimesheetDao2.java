package ua.com.fielden.platform.test.entities.daos;

import ua.com.fielden.platform.dao2.CommonEntityDao2;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.sample.domain.TgTimesheet;
import ua.com.fielden.platform.sample.domain.controller.ITgTimesheet2;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

/**
 * DAO for retrieving TgTimesheets.
 *
 * @author TG Team
 *
 */
@EntityType(TgTimesheet.class)
public class TgTimesheetDao2 extends CommonEntityDao2<TgTimesheet> implements ITgTimesheet2 {

    @Inject
    protected TgTimesheetDao2(final IFilter filter) {
	super(filter);
    }

}
