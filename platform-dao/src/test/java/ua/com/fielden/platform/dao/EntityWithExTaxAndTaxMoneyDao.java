package ua.com.fielden.platform.dao;

import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.persistence.types.EntityWithExTaxAndTaxMoney;

import com.google.inject.Inject;

/**
 * A DAO for {@link EntityWithExTaxAndTaxMoney} used for testing.
 *
 * @author TG Teams
 *
 */
@EntityType(EntityWithExTaxAndTaxMoney.class)
public class EntityWithExTaxAndTaxMoneyDao extends CommonEntityDao<EntityWithExTaxAndTaxMoney> {

    @Inject
    protected EntityWithExTaxAndTaxMoneyDao(final IFilter filter) {
        super(filter);
    }

}
