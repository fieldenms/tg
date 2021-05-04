package ua.com.fielden.platform.entity;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
/**
 * DAO implementation for companion object {@link CalendarEntityCo}.
 *
 * @author TG Team
 *
 */
@EntityType(CalendarEntity.class)
public class CalendarEntityDao extends CommonEntityDao<CalendarEntity> implements CalendarEntityCo {

    @Inject
    public CalendarEntityDao(final IFilter filter) {
        super(filter);
    }

}