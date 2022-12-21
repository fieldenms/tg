package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

@EntityType(TgBogie.class)
public class TgBogieDao extends CommonEntityDao<TgBogie> implements ITgBogie {

    @Inject
    protected TgBogieDao(final IFilter filter) {
        super(filter);
    }

    @Override
    public TgBogie new_() {
        return super.new_().setActive(true);
    }

}