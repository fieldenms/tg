package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

/**
 * DAO for {@link ITgOriginator}
 *
 * @author TG Team
 *
 */
@EntityType(TgOriginator.class)
public class TgOriginatorDao extends CommonEntityDao<TgOriginator> implements ITgOriginator {

    @Inject
    protected TgOriginatorDao(final IFilter filter) {
        super(filter);
    }

}
