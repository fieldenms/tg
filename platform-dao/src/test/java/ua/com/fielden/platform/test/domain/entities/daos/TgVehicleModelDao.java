package ua.com.fielden.platform.test.domain.entities.daos;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.equery.interfaces.IFilter;
import ua.com.fielden.platform.sample.domain.TgVehicleMake;
import ua.com.fielden.platform.sample.domain.TgVehicleModel;
import ua.com.fielden.platform.sample.domain.controller.ITgVehicleModel;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;


/**
 * DAO for {@link TgVehicleMake} retrieval.
 *
 * @author TG Team
 */

@EntityType(TgVehicleModel.class)
public class TgVehicleModelDao extends CommonEntityDao<TgVehicleModel> implements ITgVehicleModel {

    @Inject
    protected TgVehicleModelDao(final IFilter filter) {
	super(filter);
    }
}
