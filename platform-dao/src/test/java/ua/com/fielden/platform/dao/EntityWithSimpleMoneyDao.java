package ua.com.fielden.platform.dao;

import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.persistence.types.EntityWithSimpleMoney;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

/**
 * A DAO for {@link EntityWithSimpleMoney} used for testing.
 *
 * @author 01es
 *
 */
@EntityType(EntityWithSimpleMoney.class)
public class EntityWithSimpleMoneyDao extends CommonEntityDao<EntityWithSimpleMoney> {

    @Inject
    protected EntityWithSimpleMoneyDao(final IFilter filter) {
        super(filter);
    }

}
