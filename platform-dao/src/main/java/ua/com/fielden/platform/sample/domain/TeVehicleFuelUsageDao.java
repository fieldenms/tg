package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

/**
 * @author TG Team
 * 
 */
@EntityType(TeVehicleFuelUsage.class)
public class TeVehicleFuelUsageDao extends CommonEntityDao<TeVehicleFuelUsage> implements ITeVehicleFuelUsage {

    @Inject
    protected TeVehicleFuelUsageDao(final IFilter filter) {
        super(filter);
    }

}
