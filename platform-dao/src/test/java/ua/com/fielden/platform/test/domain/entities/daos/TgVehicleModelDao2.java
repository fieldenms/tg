package ua.com.fielden.platform.test.domain.entities.daos;

import ua.com.fielden.platform.dao2.CommonEntityDao2;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.sample.domain.TgVehicleModel;
import ua.com.fielden.platform.sample.domain.controller.ITgVehicleModel2;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

@EntityType(TgVehicleModel.class)
public class TgVehicleModelDao2 extends CommonEntityDao2<TgVehicleModel> implements ITgVehicleModel2 {

    @Inject
    protected TgVehicleModelDao2(final IFilter filter) {
	super(filter);
    }
}
