package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

/**
 * DAO implementation for companion object {@link ITgLocation}.
 * 
 * @author Developers
 * 
 */
@EntityType(TgLocation.class)
public class TgLocationDao extends CommonEntityDao<TgLocation> implements ITgLocation {
    @Inject
    public TgLocationDao(final IFilter filter) {
        super(filter);
    }

}