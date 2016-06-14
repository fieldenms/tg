package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.sample.domain.ITgMakeCount;
import ua.com.fielden.platform.sample.domain.TgMakeCount;

import com.google.inject.Inject;

@EntityType(TgMakeCount.class)
public class TgMakeCountDao extends CommonEntityDao<TgMakeCount> implements ITgMakeCount {

    @Inject
    protected TgMakeCountDao(final IFilter filter) {
        super(filter);
    }
}
