package ua.com.fielden.platform.dao;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.cond;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetch;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAggregates;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAllInclCalc;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchKeyAndDescOnly;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchOnly;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.orderBy;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import org.junit.Test;

import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.OrderingModel;
import ua.com.fielden.platform.persistence.composite.EntityWithDynamicCompositeKey;
import ua.com.fielden.platform.persistence.types.EntityWithMoney;
import ua.com.fielden.platform.test.ioc.UniversalConstantsForTesting;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.utils.IUniversalConstants;

/**
 * A test case to ensure correct instantiation of entities from the instrumentation perspective during retrieval. 
 * 
 * @author TG Team
 *
 */
public class CommonEntityDaoInstrumentationTest extends AbstractDaoTestCase {

    @Test
    public void by_default_find_by_key_returns_instrumented_instances() {
        final EntityWithMoney entity = co(EntityWithMoney.class).findByKey("KEY1");
        assertTrue(entity.isInstrumented());
    }

    @Test
    public void uninstrumented_find_by_key_returns_unnstrumented_instances() {
        final EntityWithMoney entity = co(EntityWithMoney.class).uninstrumented().findByKey("KEY1");
        assertFalse(entity.isInstrumented());
    }
    
    @Test
    public void by_default_find_by_id_returns_instrumented_instances() {
        final IEntityDao<EntityWithMoney> co = co(EntityWithMoney.class);
        final EntityWithMoney entity = co.findById(co.findByKey("KEY1").getId());
        assertTrue(entity.isInstrumented());
    }

    @Test
    public void uninstrumented_find_by_id_returns_uninstrumented_instances() {
        final IEntityDao<EntityWithMoney> co = co(EntityWithMoney.class);
        final EntityWithMoney entity = co.uninstrumented().findById(co.findByKey("KEY1").getId());
        assertFalse(entity.isInstrumented());
    }

    @Test
    public void by_default_find_by_id_and_fetch_returns_instrumented_instances() {
        final IEntityDao<EntityWithMoney> co = co(EntityWithMoney.class);
        final EntityWithMoney entity = co.findById(co.findByKey("KEY1").getId(), fetchAll(EntityWithMoney.class));
        assertTrue(entity.isInstrumented());
    }

    @Test
    public void uninstrumented_find_by_id_and_fetch_returns_uninstrumented_instances() {
        final IEntityDao<EntityWithMoney> co = co(EntityWithMoney.class);
        final EntityWithMoney entity = co.uninstrumented().findById(co.findByKey("KEY1").getId(), fetchAll(EntityWithMoney.class));
        assertFalse(entity.isInstrumented());
    }

    @Test
    public void by_default_find_by_key_and_fetch_returns_instrumented_instances() {
        final IEntityDao<EntityWithMoney> co = co(EntityWithMoney.class);
        final EntityWithMoney entity = co.findByKeyAndFetch(fetchAll(EntityWithMoney.class), "KEY1");
        assertTrue(entity.isInstrumented());
    }

    @Test
    public void uninstrumented_find_by_key_and_fetch_returns_uninstrumented_instances() {
        final IEntityDao<EntityWithMoney> co = co(EntityWithMoney.class);
        final EntityWithMoney entity = co.uninstrumented().findByKeyAndFetch(fetchAll(EntityWithMoney.class), "KEY1");
        assertFalse(entity.isInstrumented());
    }

    
    @Override
    protected void populateDomain() {
        super.populateDomain();
        
        final UniversalConstantsForTesting constants = (UniversalConstantsForTesting) getInstance(IUniversalConstants.class);
        constants.setNow(dateTime("2016-02-19 02:47:00"));

        final EntityWithMoney keyPartTwo = save(new_ (EntityWithMoney.class, "KEY1", "desc").setMoney(new Money("20.00")).setDateTimeProperty(date("2009-03-01 11:00:55")));
        save(new_composite(EntityWithDynamicCompositeKey.class, "key-1-1", keyPartTwo));
        save(new_ (EntityWithMoney.class, "KEY2", "desc").setMoney(new Money("30.00")).setDateTimeProperty(date("2009-03-01 00:00:00")));
        save(new_ (EntityWithMoney.class, "KEY3", "desc").setMoney(new Money("40.00")));
        save(new_ (EntityWithMoney.class, "KEY4", "desc").setMoney(new Money("50.00")).setDateTimeProperty(date("2009-03-01 10:00:00")));
    }

}