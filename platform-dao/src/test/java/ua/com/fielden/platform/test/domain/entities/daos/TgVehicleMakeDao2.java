package ua.com.fielden.platform.test.domain.entities.daos;

import ua.com.fielden.platform.dao2.CommonEntityDao2;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.sample.domain.TgVehicleMake;
import ua.com.fielden.platform.sample.domain.controller.ITgVehicleMake2;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;


/**
 * DAO for {@link TgVehicleMake} retrieval.
 *
 * @author TG Team
 */

@EntityType(TgVehicleMake.class)
public class TgVehicleMakeDao2 extends CommonEntityDao2<TgVehicleMake> implements ITgVehicleMake2 {

    @Inject
    protected TgVehicleMakeDao2(final IFilter filter) {
	super(filter);
    }
}
