package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.sample.domain.ITgFuelType;
import ua.com.fielden.platform.sample.domain.TgFuelType;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

@EntityType(TgFuelType.class)
public class TgFuelTypeDao extends CommonEntityDao<TgFuelType> implements ITgFuelType {

    @Inject
    protected TgFuelTypeDao(final IFilter filter) {
        super(filter);
    }
}
