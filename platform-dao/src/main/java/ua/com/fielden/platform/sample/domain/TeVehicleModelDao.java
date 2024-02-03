package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

@EntityType(TeVehicleModel.class)
public class TeVehicleModelDao extends CommonEntityDao<TeVehicleModel> implements ITeVehicleModel {

    @Inject
    protected TeVehicleModelDao(final IFilter filter) {
        super(filter);
    }
}
