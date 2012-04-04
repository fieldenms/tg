package ua.com.fielden.platform.test.domain.entities.daos;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.sample.domain.TgVehicleMake;
import ua.com.fielden.platform.sample.domain.controller.ITgVehicleMake;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;


/**
 * DAO for {@link TgVehicleMake} retrieval.
 *
 * @author TG Team
 */

@EntityType(TgVehicleMake.class)
public class TgVehicleMakeDao extends CommonEntityDao<TgVehicleMake> implements ITgVehicleMake {

    @Inject
    protected TgVehicleMakeDao(final IFilter filter) {
	super(filter);
    }
}
