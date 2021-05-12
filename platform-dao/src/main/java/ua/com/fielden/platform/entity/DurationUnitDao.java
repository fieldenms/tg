package ua.com.fielden.platform.entity;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

/**
 * DAO implementation for companion object {@link DurationUnitCo}.
 *
 * @author TG Team
 *
 */
@EntityType(DurationUnit.class)
public class DurationUnitDao extends CommonEntityDao<DurationUnit> implements DurationUnitCo {

    @Inject
    public DurationUnitDao(final IFilter filter) {
        super(filter);
    }

}