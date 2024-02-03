package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

@EntityType(TgReMaxVehicleReading.class)
public class TgReMaxVehicleReadingDao extends CommonEntityDao<TgReMaxVehicleReading> implements TgReMaxVehicleReadingCo {

    @Inject
    protected TgReMaxVehicleReadingDao(final IFilter filter) {
        super(filter);
    }
}
