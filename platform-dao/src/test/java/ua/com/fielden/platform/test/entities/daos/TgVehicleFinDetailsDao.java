package ua.com.fielden.platform.test.entities.daos;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.sample.domain.TgVehicleFinDetails;
import ua.com.fielden.platform.sample.domain.controller.ITgVehicleFinDetails;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

@EntityType(TgVehicleFinDetails.class)
public class TgVehicleFinDetailsDao extends CommonEntityDao<TgVehicleFinDetails> implements ITgVehicleFinDetails {

    @Inject
    protected TgVehicleFinDetailsDao(final IFilter filter) {
	super(filter);
    }
}