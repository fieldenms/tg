package ua.com.fielden.platform.test.entities.daos;

import ua.com.fielden.platform.dao2.CommonEntityDao2;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.sample.domain.TgFuelUsage;
import ua.com.fielden.platform.sample.domain.controller.ITgFuelUsage;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

/**
 * DAO for retrieving TgTimesheets.
 *
 * @author TG Team
 *
 */
@EntityType(TgFuelUsage.class)
public class TgFuelUsageDao extends CommonEntityDao2<TgFuelUsage> implements ITgFuelUsage {

    @Inject
    protected TgFuelUsageDao(final IFilter filter) {
	super(filter);
    }

}
