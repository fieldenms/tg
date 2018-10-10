package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
/** 
 * DAO implementation for companion object {@link ITgPolygonMap}.
 * 
 * @author Developers
 *
 */
@EntityType(TgPolygonMap.class)
public class TgPolygonMapDao extends CommonEntityDao<TgPolygonMap> implements ITgPolygonMap {

    @Inject
    public TgPolygonMapDao(final IFilter filter) {
        super(filter);
    }

}