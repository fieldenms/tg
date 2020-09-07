package ua.com.fielden.platform.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Date;

import org.joda.time.DateTime;
import org.junit.Ignore;
import org.junit.Test;

import ua.com.fielden.platform.dao.exceptions.EntityCompanionException;
import ua.com.fielden.platform.persistence.composite.EntityWithDynamicCompositeKey;
import ua.com.fielden.platform.persistence.types.EntityWithMoney;
import ua.com.fielden.platform.persistence.types.EntityWithSimpleMoney;
import ua.com.fielden.platform.reflection.Reflector;
import ua.com.fielden.platform.test.ioc.UniversalConstantsForTesting;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.utils.IUniversalConstants;

public class CommonEntityDaoQuckSaveTest extends AbstractDaoTestCase {

    @Test
    public void save_is_not_overridden_in_EntityWithMoneyDao() {
        assertFalse(Reflector.isMethodOverriddenOrDeclared(CommonEntityDao.class, EntityWithMoneyDao.class, "save", EntityWithMoney.class));
    }

    @Test
    public void save_is_overridden_in_EntityWithSimpleMoneyDao() {
        assertTrue(Reflector.isMethodOverriddenOrDeclared(CommonEntityDao.class, EntityWithSimpleMoneyDao.class, "save", EntityWithSimpleMoney.class));
    }
    
    @Test
    public void entity_version_is_updated_after_quickSave() {
        final IEntityDao<EntityWithMoney> co = co$(EntityWithMoney.class);
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
        final IEntityDao<EntityWithMoney> co = co$(EntityWithMoney.class);
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
        final IEntityDao<EntityWithMoney> co$ = co$(EntityWithMoney.class);
        final EntityWithMoney entity = co$.findByKey("KEY1");
        final Date transDate = co$.findByKey("KEY1").getTransDate();
        assertNotNull("Test pre-condition is invalid -- transDate should be null.", transDate);
        assertEquals("Transaction property should not have been updated for an existing property.", transDate, co$.findById(co$.quickSave(entity.setDesc("some new value"))).getTransDate());
    }
    
    @Test
    public void transaction_date_property_for_new_entity_gets_auto_assigned_with_quickSave() {
        final EntityWithMoney newEntity = new_(EntityWithMoney.class, "new entity");
        assertNull("Test pre-condition is invalid -- transDate should be null.", newEntity.getTransDate());
        newEntity.setMoney(new Money("12")); // required property -- has to be set
        final IEntityDao<EntityWithMoney> co = co$(EntityWithMoney.class);
        assertNotNull("transDate should have been assigned.", co.findById(co.quickSave(newEntity)).getTransDate());
    }


    @Test
    public void already_assigned_transaction_date_property_for_new_entity_does_not_get_repopulated_with_quickSave() {
        final EntityWithMoney newEntity = new_(EntityWithMoney.class, "new entity");
        final Date date = new DateTime(2009, 01, 01, 0, 0, 0, 0).toDate();
        newEntity.setTransDate(date);
        newEntity.setMoney(new Money("12")); // required property -- has to be set
        final IEntityDao<EntityWithMoney> co = co$(EntityWithMoney.class);
        assertEquals("transDate should not have been re-assigned.", date, co.findById(co.quickSave(newEntity)).getTransDate());
    }

    @Test
    @Ignore("This test is not really valuable as a unit test. It has more of a proof-of-concept benchmark connataion.")
    public void test_quickSave_is_more_performant_than_save() {
        final IEntityDao<EntityWithMoney> co = co$(EntityWithMoney.class);
        int times = 100;
        
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

        assertTrue("save() should not be faster than quickSave()", saveTime >= quickSaveTime);
    }

    @Test
    public void quickSave_invocation_on_companions_with_overriden_save_throws_exception_to_prevent_invalid_execution() {
        final IEntityDao<EntityWithSimpleMoney> co = co$(EntityWithSimpleMoney.class);
        try {
            co.quickSave(new_(EntityWithSimpleMoney.class, "SOME KEY"));
            fail();
        } catch (EntityCompanionException ex) {
            assertEquals("Quick save is not supported for entity [ua.com.fielden.platform.persistence.types.EntityWithSimpleMoney] due to an overridden method save (refer companion [ua.com.fielden.platform.dao.EntityWithSimpleMoneyDao]).", 
                         ex.getMessage());
        }
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