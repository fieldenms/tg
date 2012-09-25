package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.sample.domain.ITgWagonClass;
import ua.com.fielden.platform.sample.domain.TgWagonClass;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

@EntityType(TgWagonClass.class)
public class TgWagonClassDao extends CommonEntityDao<TgWagonClass> implements ITgWagonClass {

    @Inject
    protected TgWagonClassDao(final IFilter filter) {
	super(filter);
    }
}