package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

/**
 * @author TG Team
 * 
 */
@EntityType(TgReVehicleWithHighPrice.class)
public class TgReVehicleWithHighPriceDao extends CommonEntityDao<TgReVehicleWithHighPrice> implements TgReVehicleWithHighPriceCo {

    @Inject
    protected TgReVehicleWithHighPriceDao(final IFilter filter) {
        super(filter);
    }

}
