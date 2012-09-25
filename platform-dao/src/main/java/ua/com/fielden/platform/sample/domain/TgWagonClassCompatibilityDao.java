package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.sample.domain.ITgWagonClassCompatibility;
import ua.com.fielden.platform.sample.domain.TgWagonClassCompatibility;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

@EntityType(TgWagonClassCompatibility.class)
public class TgWagonClassCompatibilityDao extends CommonEntityDao<TgWagonClassCompatibility> implements ITgWagonClassCompatibility {

    @Inject
    protected TgWagonClassCompatibilityDao(final IFilter filter) {
	super(filter);
    }
}