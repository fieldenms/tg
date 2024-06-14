package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.sample.domain.ITgWagon;
import ua.com.fielden.platform.sample.domain.TgWagon;

import com.google.inject.Inject;

@EntityType(TgWagon.class)
public class TgWagonDao extends CommonEntityDao<TgWagon> implements ITgWagon {

    @Inject
    protected TgWagonDao(final IFilter filter) {
        super(filter);
    }
}