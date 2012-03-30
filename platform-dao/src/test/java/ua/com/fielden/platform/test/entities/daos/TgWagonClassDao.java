package ua.com.fielden.platform.test.entities.daos;

import ua.com.fielden.platform.dao2.CommonEntityDao2;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.sample.domain.TgWagonClass;
import ua.com.fielden.platform.sample.domain.controller.ITgWagonClass;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

@EntityType(TgWagonClass.class)
public class TgWagonClassDao extends CommonEntityDao2<TgWagonClass> implements ITgWagonClass {

    @Inject
    protected TgWagonClassDao(final IFilter filter) {
	super(filter);
    }
}