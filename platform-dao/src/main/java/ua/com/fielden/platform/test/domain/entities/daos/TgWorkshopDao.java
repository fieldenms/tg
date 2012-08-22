package ua.com.fielden.platform.test.domain.entities.daos;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.sample.domain.TgWorkshop;
import ua.com.fielden.platform.sample.domain.controller.ITgWorkshop;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;


@EntityType(TgWorkshop.class)
public class TgWorkshopDao extends CommonEntityDao<TgWorkshop> implements ITgWorkshop {

    @Inject
    protected TgWorkshopDao(final IFilter filter) {
	super(filter);
    }
}
