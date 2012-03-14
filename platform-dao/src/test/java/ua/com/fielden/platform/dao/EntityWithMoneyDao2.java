package ua.com.fielden.platform.dao;

import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.dao2.CommonEntityDao2;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.persistence.types.EntityWithMoney;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

/**
 * A DAO for {@link EntityWithMoney} used for testing.
 *
 * @author 01es
 *
 */
@EntityType(EntityWithMoney.class)
public class EntityWithMoneyDao2 extends CommonEntityDao2<EntityWithMoney> {

    @Inject
    protected EntityWithMoneyDao2(final IFilter filter) {
	super(filter);
    }


    @SessionRequired
    public EntityWithMoney saveWithException(final EntityWithMoney entity) {
	final EntityWithMoney savedEntity = super.save(entity);
	throw new RuntimeException("Purposeful exception.");
    }

    @SessionRequired
    public EntityWithMoney saveTwoWithException(final EntityWithMoney one, final EntityWithMoney two) {
	super.save(one);
	super.save(two);
	throw new RuntimeException("Purposeful exception.");
    }


}
