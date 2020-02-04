package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.sample.domain.ITgVehicle;
import ua.com.fielden.platform.sample.domain.TgVehicle;

import com.google.inject.Inject;

@EntityType(TgVehicle.class)
public class TgVehicleDao extends CommonEntityDao<TgVehicle> implements ITgVehicle {

    @Inject
    protected TgVehicleDao(final IFilter filter) {
        super(filter);
    }
}