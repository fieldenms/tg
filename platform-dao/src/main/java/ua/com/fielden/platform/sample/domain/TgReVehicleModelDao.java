package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

@EntityType(TgReVehicleModel.class)
public class TgReVehicleModelDao extends CommonEntityDao<TgReVehicleModel> implements ITgReVehicleModel {

    @Inject
    protected TgReVehicleModelDao(final IFilter filter) {
        super(filter);
    }
}
