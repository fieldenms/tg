package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.sample.domain.ITgBogie;
import ua.com.fielden.platform.sample.domain.TgBogie;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

@EntityType(TgBogie.class)
public class TgBogieDao extends CommonEntityDao<TgBogie> implements ITgBogie {

    @Inject
    protected TgBogieDao(final IFilter filter) {
        super(filter);
    }
}