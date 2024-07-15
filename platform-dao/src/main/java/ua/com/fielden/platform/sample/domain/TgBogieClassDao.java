package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

@EntityType(TgBogieClass.class)
public class TgBogieClassDao extends CommonEntityDao<TgBogieClass> implements ITgBogieClass {

    @Inject
    protected TgBogieClassDao(final IFilter filter) {
        super(filter);
    }

    @Override
    public TgBogieClass new_() {
        return super.new_().setActive(true);
    }

}