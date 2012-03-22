package ua.com.fielden.platform.test.entities.daos;

import ua.com.fielden.platform.dao2.CommonEntityDao2;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.sample.domain.TgVehicle;
import ua.com.fielden.platform.sample.domain.controller.ITgVehicle;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

/**
 * DAO for retrieving TgTimesheets.
 *
 * @author TG Team
 *
 */
@EntityType(TgVehicle.class)
public class TgVehicleDao extends CommonEntityDao2<TgVehicle> implements ITgVehicle {

    @Inject
    protected TgVehicleDao(final IFilter filter) {
	super(filter);
    }

}
