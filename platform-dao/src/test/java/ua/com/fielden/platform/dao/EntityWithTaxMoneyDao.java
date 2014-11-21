package ua.com.fielden.platform.dao;

import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.persistence.types.EntityWithTaxMoney;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

/**
 * A DAO for {@link EntityWithTaxMoney} used for testing.
 *
 * @author 01es
 *
 */
@EntityType(EntityWithTaxMoney.class)
public class EntityWithTaxMoneyDao extends CommonEntityDao<EntityWithTaxMoney> {

    @Inject
    protected EntityWithTaxMoneyDao(final IFilter filter) {
        super(filter);
    }

}
