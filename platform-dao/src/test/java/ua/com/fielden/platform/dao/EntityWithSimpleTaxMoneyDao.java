package ua.com.fielden.platform.dao;

import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.persistence.types.EntityWithSimpleTaxMoney;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

/**
 * A DAO for {@link EntityWithSimpleTaxMoney} used for testing.
 *
 * @author TG Teams
 *
 */
@EntityType(EntityWithSimpleTaxMoney.class)
public class EntityWithSimpleTaxMoneyDao extends CommonEntityDao<EntityWithSimpleTaxMoney> {

    @Inject
    protected EntityWithSimpleTaxMoneyDao(final IFilter filter) {
        super(filter);
    }

}
