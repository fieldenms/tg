package ua.com.fielden.platform.dao;

import static org.junit.Assert.*;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;

import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;
import org.junit.Test;

import ua.com.fielden.platform.dao.exceptions.EntityCompanionException;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.persistence.composite.EntityWithDynamicCompositeKey;
import ua.com.fielden.platform.persistence.types.EntityWithMoney;
import ua.com.fielden.platform.test.AbstractDomainDrivenTestCase;
import ua.com.fielden.platform.test.PlatformTestDomainTypes;
import ua.com.fielden.platform.test.ioc.UniversalConstantsForTesting;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.utils.IUniversalConstants;

public class CommonEntityQuckSaveTest extends AbstractDomainDrivenTestCase {

    @Test
    public void entity_version_is_updated_after_quickSave() {
        final IEntityDao<EntityWithMoney> co = ao(EntityWithMoney.class);
        EntityWithMoney entity = co.findByKey("KEY1");

        assertEquals("Incorrect prev version", Long.valueOf(0), entity.getVersion());
        entity.setDesc("new desc");
        entity = co.findById(co.quickSave(entity));
        assertEquals("Incorrect curr version", Long.valueOf(1), entity.getVersion());

        final EntityWithMoney updatedEntity = co.findByKey("KEY1");
        assertEquals("Incorrect prev version", Long.valueOf(1), updatedEntity.getVersion());
    }


    @Test
    public void optimistic_locking_based_on_versioning_works_for_quickSave() {
        final IEntityDao<EntityWithMoney> co = ao(EntityWithMoney.class);
        // get entity, which will be modified but not saved
        final EntityWithMoney entity = co.findByKey("KEY1");
        assertEquals("Incorrect prev version", Long.valueOf(0), entity.getVersion());
        entity.setDesc("new desc");

        // retrieve another instance of the same entity, modify and save -- this should emulate concurrent modification
        final EntityWithMoney anotherEntityInstance = co.findByKey("KEY1");
        anotherEntityInstance.setDesc("another desc");

        co.quickSave(anotherEntityInstance);

        // try to save previously retrieved entity, which should fail due to concurrent modification
        try {
            co.quickSave(entity);
            fail("Should have failed due to optimistic locking.");
        } catch (final Exception e) {
        }
    }

    @Test
    public void transaction_date_property_for_previously_persisted_entity_is_not_reassigned_with_quickSave() {
        final IEntityDao<EntityWithMoney> co = ao(EntityWithMoney.class);
        final EntityWithMoney entity = co.findByKey("KEY1");
        final Date transDate = co.findByKey("KEY1").getTransDate();
        assertNotNull("Test pre-condition is invalid -- transDate should be null.", transDate);
        assertEquals("Transaction property should not have been updated for an existing property.", transDate, co.findById(co.quickSave(entity)).getTransDate());
    }
    
    @Test
    public void transaction_date_property_for_new_entity_gets_auto_assigned_with_quickSave() {
        final EntityWithMoney newEntity = new_(EntityWithMoney.class, "new entity");
        assertNull("Test pre-condition is invalid -- transDate should be null.", newEntity.getTransDate());
        newEntity.setMoney(new Money("12")); // required property -- has to be set
        final IEntityDao<EntityWithMoney> co = ao(EntityWithMoney.class);
        assertNotNull("transDate should have been assigned.", co.findById(co.quickSave(newEntity)).getTransDate());
    }


    @Test
    public void already_assigned_transaction_date_property_for_new_entity_does_not_get_repopulated_with_quickSave() {
        final EntityWithMoney newEntity = new_(EntityWithMoney.class, "new entity");
        final Date date = new DateTime(2009, 01, 01, 0, 0, 0, 0).toDate();
        newEntity.setTransDate(date);
        newEntity.setMoney(new Money("12")); // required property -- has to be set
        final IEntityDao<EntityWithMoney> co = ao(EntityWithMoney.class);
        assertEquals("transDate should not have been re-assigned.", date, co.findById(co.quickSave(newEntity)).getTransDate());
    }

    @Test
    public void test_quickSave_is_more_performant_than_save() {
        final IEntityDao<EntityWithMoney> co = ao(EntityWithMoney.class);
        int times = 10;
        
        long quickSaveTime = 0;
        for (int index = 0; index < times; index++) {
            final EntityWithMoney newEntity = new_(EntityWithMoney.class, "quick entity " + index);
            
            final long start = System.nanoTime();
            co.quickSave(newEntity);
            quickSaveTime += (System.nanoTime() - start);
        }

        long saveTime = 0;
        for (int index = 0; index < times; index++) {
            final EntityWithMoney newEntity = new_(EntityWithMoney.class, "slow entity " + index);
            
            final long start = System.nanoTime();
            co.save(newEntity);
            saveTime += (System.nanoTime() - start);
        }

        assertTrue("save() should not be faster than quickSave()", saveTime > quickSaveTime);
    }

    @Test
    public void quick_save_guard_prevents_accidental_use_of_method_quickSave() {
        final IEntityDao<EntityWithDynamicCompositeKey> co = ao(EntityWithDynamicCompositeKey.class);
        final EntityWithDynamicCompositeKey entity = co.findById(1L, fetchAll(EntityWithDynamicCompositeKey.class));
        assertNotNull(entity);
        
        entity.setKeyPartOne("updated part one value");
        
        try {
            co.quickSave(entity);
            fail("Quick save guard failed.");
        } catch (final EntityCompanionException ex) {
        }
    }




    @Override
    protected void populateDomain() {
        final UniversalConstantsForTesting constants = (UniversalConstantsForTesting) getInstance(IUniversalConstants.class);
        constants.setNow(dateTime("2016-02-19 02:47:00"));

        final EntityWithMoney keyPartTwo = save(new_ (EntityWithMoney.class, "KEY1", "desc").setMoney(new Money("20.00")).setDateTimeProperty(date("2009-03-01 11:00:55")));
        save(new_composite(EntityWithDynamicCompositeKey.class, "key-1-1", keyPartTwo));
        save(new_ (EntityWithMoney.class, "KEY2", "desc").setMoney(new Money("30.00")).setDateTimeProperty(date("2009-03-01 00:00:00")));
        save(new_ (EntityWithMoney.class, "KEY3", "desc").setMoney(new Money("40.00")));
        save(new_ (EntityWithMoney.class, "KEY4", "desc").setMoney(new Money("50.00")).setDateTimeProperty(date("2009-03-01 10:00:00")));

    }

    @Override
    protected List<Class<? extends AbstractEntity<?>>> domainEntityTypes() {
        return PlatformTestDomainTypes.entityTypes;
    }
}