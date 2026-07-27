package ua.com.fielden.platform.entity;

import org.apache.logging.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

import static org.apache.logging.log4j.LogManager.getLogger;

/// This test case is intended to check correctness of existing collection modification validation logic.
///
/// This includes validations of:
/// - Master entity disappearance.
/// - Available entity disappearance.
/// - Collection modification conflict detection.
///
///   These conflicts are most likely taken place after the user has been opened the dialog for collection modification,
///   but hasn't finished collection modification by clicking on SAVE button.
///
public class CollectionModificationValidationTest extends AbstractDaoTestCase {
    private final Logger logger = getLogger(getClass());
    private final String newUsername = "NEW_USER";

    @Ignore
    @Test
    public void collection_modification_is_not_applicable_to_persisted_but_dirty_entity() {
        // TODO Provide a test.
    }

    @Ignore
    @Test
    public void master_entity_removal_prevents_collection_modification() {
        // TODO Provide a test.
    }

    @Ignore
    @Test
    public void available_entity_removal_prevents_collection_modification_when_unselecting_it() {
        // TODO Provide a test.
    }

    @Ignore
    @Test
    public void available_entity_removal_prevents_collection_modification_when_selecting_it() {
        // TODO Provide a test.
    }

    @Ignore
    @Test
    public void another_recorded_collection_modification_prevents_collection_modification() {
        // TODO Provide a test.
    }

    @Ignore
    @Test
    public void collection_modification_succeeds_if_validation_has_been_succeeded() {
        // TODO Provide a test.
    }

    @Ignore
    @Test
    public void available_entities_are_ordered_by_key_and_such_order_does_not_mutate_during_validation_cycles_in_user_roles_collectional_editor() {
        // TODO Provide a test.
    }

}
