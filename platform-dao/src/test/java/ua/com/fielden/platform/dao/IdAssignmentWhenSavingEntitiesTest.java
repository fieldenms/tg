package ua.com.fielden.platform.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import ua.com.fielden.platform.persistence.types.EntityWithMoney;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

/**
 * This test case ensures correct assignment of ID values upon saving of new and modified entities.
 *
 * @author TG Team
 *
 */
public class IdAssignmentWhenSavingEntitiesTest extends AbstractDaoTestCase {

    @Test
    public void isPersistent_evaluates_to_false_for_new_entities_that_failed_saving_due_to_db_level_exceptions() {
        final EntityWithMoneyDao dao = co$(EntityWithMoney.class);

        final EntityWithMoney newEntity = new_(EntityWithMoney.class, "new entity")
                                          .setShortComment("123456");
        try {
            dao.save(newEntity);
            fail("Saving should have failed.");
        } catch(final Exception ex) {  }
        assertFalse(newEntity.isPersisted());
    }

    @Test
    public void isPersistent_evaluates_to_true_for_new_entities_that_were_saved_successfully() {
        final EntityWithMoneyDao dao = co$(EntityWithMoney.class);

        final EntityWithMoney newEntity = new_(EntityWithMoney.class, "new entity")
                                          .setShortComment("12345");
        final EntityWithMoney savedEntity = dao.save(newEntity);
        assertTrue(newEntity.isPersisted());
        assertTrue(savedEntity.isPersisted());
    }

    @Test
    public void db_level_errors_do_not_affect_isPersistent_evaluation_for_previously_persisted_entities() {
        final EntityWithMoneyDao dao = co$(EntityWithMoney.class);

        final EntityWithMoney newEntity = new_(EntityWithMoney.class, "new entity")
                                          .setShortComment("12345");
        final EntityWithMoney savedEntity = dao.save(newEntity);
        try {
            save(savedEntity.setShortComment("123456"));
            fail("Saving should have failed.");
        } catch (final Exception ex) { }

        assertTrue(savedEntity.isPersisted());
    }

    @Test
    public void saving_modified_persisted_entities_does_not_change_their_id() {
        final EntityWithMoneyDao dao = co$(EntityWithMoney.class);

        final EntityWithMoney newEntity = new_(EntityWithMoney.class, "new entity").setShortComment("12345");
        final EntityWithMoney savedEntity = dao.save(newEntity);
        final Long id = savedEntity.getId();
        final Long version = savedEntity.getVersion();
        final EntityWithMoney savedAgainEntity = dao.save(savedEntity.setShortComment("123")); 
        
        assertEquals(id, savedAgainEntity.getId());
        assertEquals(new Long(version + 1), savedAgainEntity.getVersion());
    }

}
