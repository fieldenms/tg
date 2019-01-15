package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.sample.domain.ITgVehicleModel;
import ua.com.fielden.platform.sample.domain.TgVehicleModel;

import com.google.inject.Inject;

@EntityType(TgVehicleModelWithCalc.class)
public class TgVehicleModelWithCalcDao extends CommonEntityDao<TgVehicleModelWithCalc> implements ITgVehicleModelWithCalc {

    @Inject
    protected TgVehicleModelWithCalcDao(final IFilter filter) {
        super(filter);
    }
}
