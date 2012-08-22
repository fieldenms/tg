package ua.com.fielden.platform.test.entities.daos;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.sample.domain.TgBogieClass;
import ua.com.fielden.platform.sample.domain.controller.ITgBogieClass;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

@EntityType(TgBogieClass.class)
public class TgBogieClassDao extends CommonEntityDao<TgBogieClass> implements ITgBogieClass {

    @Inject
    protected TgBogieClassDao(final IFilter filter) {
	super(filter);
    }
}