package ua.com.fielden.platform.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static ua.com.fielden.platform.dao.EntityWithMoneyDao.ERR_PURPOSEFUL_EXCEPTION;

import org.junit.Test;

import ua.com.fielden.platform.error.Result;
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
    public void isPersistent_is_false_for_new_entities_that_failed_saving() {
        final IEntityWithMoney co$ = co$(EntityWithMoney.class);

        final EntityWithMoney newEntity = new_(EntityWithMoney.class, "new entity")
                                          .setShortComment("123456");
        try {
            co$.save(newEntity);
            fail("Saving should have failed.");
        } catch(final Exception ex) {  }

        assertFalse(newEntity.isPersisted());
    }

    @Test
    public void isPersistent_is_true_for_newly_saved_entity() {
        final IEntityWithMoney co$ = co$(EntityWithMoney.class);
        final EntityWithMoney savedEntity = co$.save(new_(EntityWithMoney.class, "new entity").setShortComment("12345"));
        assertTrue(savedEntity.isPersisted());
    }

    @Test
    public void exceptions_in_save_do_not_affect_isPersistent_evaluation_for_previously_persisted_entities() {
        final IEntityWithMoney co$ = co$(EntityWithMoney.class);

        final EntityWithMoney newEntity = new_(EntityWithMoney.class, "new entity")
                                          .setShortComment("12345");
        final EntityWithMoney savedEntity = co$.save(newEntity);
        try {
            save(savedEntity.setShortComment("123456"));
            fail("Saving should have failed.");
        } catch (final Exception ex) { }

        assertTrue(savedEntity.isPersisted());
    }

    @Test
    public void saving_modified_persisted_entities_does_not_change_their_id() {
        final IEntityWithMoney co$ = co$(EntityWithMoney.class);

        final EntityWithMoney newEntity = new_(EntityWithMoney.class, "new entity").setShortComment("12345");
        final EntityWithMoney savedEntity = co$.save(newEntity);
        final Long id = savedEntity.getId();
        final Long version = savedEntity.getVersion();
        final EntityWithMoney savedAgainEntity = co$.save(savedEntity.setShortComment("123"));
        
        assertEquals(id, savedAgainEntity.getId());
        assertEquals(new Long(version + 1), savedAgainEntity.getVersion());
    }

    @Test
    public void isPersistent_is_false_for_new_entities_that_were_passed_as_arguments_to_save_and_saved_successfully() {
        final IEntityWithMoney co$ = co$(EntityWithMoney.class);

        final EntityWithMoney newEntity = new_(EntityWithMoney.class, "new entity")
                                          .setShortComment("12345");
        co$.save(newEntity);
        assertFalse(newEntity.isPersisted());
    }

    @Test
    public void repeated_attempt_at_saving_new_entity_after_failed_attempt_succeeds() {
        final EntityWithMoneyDao co$ = co$(EntityWithMoney.class);

        final EntityWithMoney newEntity = new_(EntityWithMoney.class, "valid entity").setShortComment("12345");

        // first attempt that fails on purpose, rolling back the database transaction after insertion occurres
        try {
            co$.saveWithException(newEntity);
            fail("The first attempt to save new entity should have failed.");
        } catch (final Exception ex) {
            assertEquals(ERR_PURPOSEFUL_EXCEPTION, ex.getMessage());
        }

        // second attempt to save the same entity without any deliberate exception
        final EntityWithMoney savedEntity = co$.save(newEntity);
        assertTrue(savedEntity.isPersisted());
        assertTrue(co$.entityExists(savedEntity));
    }

}
