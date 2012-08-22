package ua.com.fielden.platform.test.entities.daos;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.sample.domain.TgVehicle;
import ua.com.fielden.platform.sample.domain.controller.ITgVehicle;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

@EntityType(TgVehicle.class)
public class TgVehicleDao extends CommonEntityDao<TgVehicle> implements ITgVehicle {

    @Inject
    protected TgVehicleDao(final IFilter filter) {
	super(filter);
    }
}