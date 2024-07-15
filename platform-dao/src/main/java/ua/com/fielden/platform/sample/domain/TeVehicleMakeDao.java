package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

/**
 * DAO for {@link TgVehicleMake} retrieval.
 * 
 * @author TG Team
 */

@EntityType(TeVehicleMake.class)
public class TeVehicleMakeDao extends CommonEntityDao<TeVehicleMake> implements ITeVehicleMake {

    @Inject
    protected TeVehicleMakeDao(final IFilter filter) {
        super(filter);
    }
}
