package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.sample.domain.ITgTimesheet;
import ua.com.fielden.platform.sample.domain.TgTimesheet;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

/**
 * DAO for retrieving TgTimesheets.
 * 
 * @author TG Team
 * 
 */
@EntityType(TgTimesheet.class)
public class TgTimesheetDao extends CommonEntityDao<TgTimesheet> implements ITgTimesheet {

    @Inject
    protected TgTimesheetDao(final IFilter filter) {
        super(filter);
    }

}
