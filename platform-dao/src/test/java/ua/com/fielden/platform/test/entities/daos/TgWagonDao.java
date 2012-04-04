package ua.com.fielden.platform.test.entities.daos;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.sample.domain.TgWagon;
import ua.com.fielden.platform.sample.domain.controller.ITgWagon;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

@EntityType(TgWagon.class)
public class TgWagonDao extends CommonEntityDao<TgWagon> implements ITgWagon {

    @Inject
    protected TgWagonDao(final IFilter filter) {
	super(filter);
    }
}