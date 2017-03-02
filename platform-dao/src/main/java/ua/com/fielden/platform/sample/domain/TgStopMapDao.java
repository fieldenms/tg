package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
/** 
 * DAO implementation for companion object {@link ITgStopMap}.
 * 
 * @author Developers
 *
 */
@EntityType(TgStopMap.class)
public class TgStopMapDao extends CommonEntityDao<TgStopMap> implements ITgStopMap {

    @Inject
    public TgStopMapDao(final IFilter filter) {
        super(filter);
    }

}