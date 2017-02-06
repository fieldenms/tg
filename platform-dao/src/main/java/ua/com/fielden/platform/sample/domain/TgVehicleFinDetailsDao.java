package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.sample.domain.ITgVehicleFinDetails;
import ua.com.fielden.platform.sample.domain.TgVehicleFinDetails;

import com.google.inject.Inject;

@EntityType(TgVehicleFinDetails.class)
public class TgVehicleFinDetailsDao extends CommonEntityDao<TgVehicleFinDetails> implements ITgVehicleFinDetails {

    @Inject
    protected TgVehicleFinDetailsDao(final IFilter filter) {
        super(filter);
    }
}