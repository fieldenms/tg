package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.sample.domain.ITgVehicleModel;
import ua.com.fielden.platform.sample.domain.TgVehicleModel;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

@EntityType(TgVehicleModel.class)
public class TgVehicleModelDao extends CommonEntityDao<TgVehicleModel> implements ITgVehicleModel {

    @Inject
    protected TgVehicleModelDao(final IFilter filter) {
	super(filter);
    }
}
