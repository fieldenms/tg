package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.sample.domain.ITgBogieLocation;
import ua.com.fielden.platform.sample.domain.TgBogieLocation;

import com.google.inject.Inject;

@EntityType(TgBogieLocation.class)
public class TgBogieLocationDao extends CommonEntityDao<TgBogieLocation> implements ITgBogieLocation {

    @Inject
    protected TgBogieLocationDao(final IFilter filter) {
        super(filter);
    }
}