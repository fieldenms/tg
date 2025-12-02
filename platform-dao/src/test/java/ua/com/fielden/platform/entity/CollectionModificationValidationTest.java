package ua.com.fielden.platform.entity;

import org.apache.logging.log4j.Logger;
import org.junit.Test;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.security.user.*;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.web.centre.CentreContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;

import static org.apache.logging.log4j.LogManager.getLogger;
import static org.junit.Assert.*;

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

    private UserRolesUpdater createUpdaterWithoutMasterEntity() {
        final UserRolesUpdaterProducer producer = getInstance(UserRolesUpdaterProducer.class);
        final CentreContext<AbstractEntity<?>, AbstractEntity<?>> context = new CentreContext<>();
        producer.setContext(context);
        return producer.newEntity();
    }

    private UserRolesUpdater createUpdater(final User user) {
        final UserRolesUpdaterProducer producer = getInstance(UserRolesUpdaterProducer.class);
        final CentreContext<AbstractEntity<?>, AbstractEntity<?>> context = new CentreContext<>();
        context.setMasterEntity(user);
        producer.setContext(context);
        return producer.newEntity();
    }

    @SafeVarargs
    private static <T> HashSet<T> setOf(final T ... elements) {
        return new HashSet<>(Arrays.asList(elements));
    }

    @SafeVarargs
    private static <T> LinkedHashSet<T> linkedSetOf(final T ... elements) {
        return new LinkedHashSet<>(Arrays.asList(elements));
    }

    @SafeVarargs
    private static <T> ArrayList<T> listOf(final T ... elements) {
        return new ArrayList<>(Arrays.asList(elements));
    }

    @Test
    public void collection_modification_requires_master_entity() {
        try {
            createUpdaterWithoutMasterEntity();
            fail("Collection modification should fail.");
        } catch (final Exception ex) {
            assertTrue(ex instanceof Result);
            assertTrue(ex instanceof Result res && res.getMessage().equals("The master entity for collection modification is not provided in the context."));
        }
    }

    @Test
    public void collection_modification_is_not_applicable_to_new_entity() {
        final User user = new_(User.class, newUsername);

        try {
            createUpdater(user);
            fail("Collection modification should fail.");
        } catch (final Exception ex) {
            assertTrue(ex instanceof Result && ((Result) ex).getMessage().equals("This action is applicable only to a saved entity. Please save entity and try again."));
        }
    }

    @Test
    public void collection_modification_is_not_applicable_to_persisted_but_dirty_entity() {
        final User user = save(new_(User.class, newUsername).setBase(true));
        user.setDesc("New description.");

        try {
            createUpdater(user);
            fail("Collection modification should fail.");
        } catch (final Exception ex) {
            assertTrue(ex instanceof Result && ((Result) ex).getMessage().equals("This action is applicable only to a saved entity. Please save entity and try again."));
        }
    }

    @Test
    public void master_entity_removal_prevents_collection_modification() {
        final User user = save(new_(User.class, newUsername).setBase(true));
        createUpdater(user);

        co$(User.class).batchDelete(listOf(user.getId()));

        try {
            createUpdater(user);
            fail("Collection modification should fail.");
        } catch (final Exception ex) {
            assertTrue(ex instanceof Result && ((Result) ex).getMessage().equals("The master entity has been deleted. " + "Please cancel this action and try again."));
        }
    }

    @Test
    public void available_entity_removal_prevents_collection_modification_when_unselecting_it() {
        final User user = save(new_(User.class, newUsername).setBase(true));

        save(new_(UserRole.class, "ROLE1", "desc").setActive(true));
        final UserRole role2 = save(new_(UserRole.class, "ROLE2", "desc").setActive(true));
        final UserRole role3 = save(new_(UserRole.class, "ROLE3", "desc").setActive(true));
        final UserAndRoleAssociation userToRole2 = save(new_composite(UserAndRoleAssociation.class, user, role2));
        save(new_composite(UserAndRoleAssociation.class, user, role3));

        final UserRolesUpdater updater = createUpdater(user);
        final LinkedHashSet<Long> removedIds = linkedSetOf(role2.getId());
        updater.setRemovedIds(removedIds);

        // removal of available entity
        this.<UserAndRoleAssociationCo, UserAndRoleAssociation>co$(UserAndRoleAssociation.class).removeAssociation(setOf(userToRole2));
        co$(UserRole.class).batchDelete(listOf(role2.getId()));

        try {
            save(updater);
            fail("Collection modification should fail.");
        } catch (final Exception ex) {
            assertTrue(ex instanceof Result && ((Result) ex).getMessage().equals("Another user has deleted the item, that you're trying to un-tick. " + "Please cancel this action and try again."));
        }
    }

    @Test
    public void available_entity_removal_prevents_collection_modification_when_selecting_it() {
        final User user = save(new_(User.class, newUsername).setBase(true));

        final UserRole role1 = save(new_(UserRole.class, "ROLE1", "desc").setActive(true));
        final UserRole role2 = save(new_(UserRole.class, "ROLE2", "desc").setActive(true));
        final UserRole role3 = save(new_(UserRole.class, "ROLE3", "desc").setActive(true));
        save(new_composite(UserAndRoleAssociation.class, user, role2));
        save(new_composite(UserAndRoleAssociation.class, user, role3));

        final UserRolesUpdater updater = createUpdater(user);
        final LinkedHashSet<Long> addedIds = linkedSetOf(role1.getId());
        updater.setAddedIds(addedIds);

        // removal of available entity
        co$(UserRole.class).batchDelete(listOf(role1.getId()));

        try {
            save(updater);
            fail("Collection modification should fail.");
        } catch (final Exception ex) {
            assertTrue(ex instanceof Result && ((Result) ex).getMessage().equals("Another user has deleted the item, that you're trying to choose. Please cancel this action and try again."));
        }
    }

    @Test
    public void another_recorded_collection_modification_prevents_collection_modification() {
        final User user = save(new_(User.class, newUsername).setBase(true));

        final UserRole role1 = save(new_(UserRole.class, "ROLE1", "desc").setActive(true));
        final UserRole role2 = save(new_(UserRole.class, "ROLE2", "desc").setActive(true));
        final UserRole role3 = save(new_(UserRole.class, "ROLE3", "desc").setActive(true));
        save(new_composite(UserAndRoleAssociation.class, user, role2));
        save(new_composite(UserAndRoleAssociation.class, user, role3));

        final UserRolesUpdater originalUpdater = createUpdater(user);
        final LinkedHashSet<Long> addedIds = linkedSetOf(role1.getId());
        originalUpdater.setAddedIds(addedIds);

        // simultaneous collection modification
        final UserRolesUpdater anotherUpdaterToBeImmediatelySaved = createUpdater(user);
        anotherUpdaterToBeImmediatelySaved.setAddedIds(addedIds);
        save(anotherUpdaterToBeImmediatelySaved);

        // starting the process of saving: a) produce it first (should be successful) b) save
        try {
            save(originalUpdater);
            fail("Collection modification should fail.");
        } catch (final Exception ex) {
            assertTrue(ex instanceof Result && ((Result) ex).getMessage().equals("Another user has changed the chosen items. " + "Please cancel this action and try again."));
        }
    }

    @Test
    public void collection_modification_succeeds_if_validation_has_been_succeeded() {
        final User user = save(new_(User.class, newUsername).setBase(true));

        final UserRole role1 = save(new_(UserRole.class, "ROLE1", "desc").setActive(true));
        final UserRole role2 = save(new_(UserRole.class, "ROLE2", "desc").setActive(true));
        final UserRole role3 = save(new_(UserRole.class, "ROLE3", "desc").setActive(true));
        save(new_composite(UserAndRoleAssociation.class, user, role2));
        save(new_composite(UserAndRoleAssociation.class, user, role3));

        final UserRolesUpdater updater = createUpdater(user);
        updater.setAddedIds(linkedSetOf(role1.getId()));
        updater.setRemovedIds(linkedSetOf(role2.getId()));
        save(updater);

        final UserRolesUpdater newUpdater = createUpdater(user);
        assertEquals(linkedSetOf(role3.getId(), role1.getId()), newUpdater.getChosenIds());
    }

    @Test
    public void available_entities_are_ordered_by_key_and_such_order_does_not_mutate_during_validation_cycles_in_user_roles_collectional_editor() {
        final User user = save(new_(User.class, newUsername).setBase(true));

        final UserRole unitTestRole = co$(UserRole.class).findByKey(UNIT_TEST_ROLE);
        final UserRole role1 = save(new_(UserRole.class, "ROLE1", "desc").setActive(true));
        final UserRole role2 = save(new_(UserRole.class, "ROLE2", "desc").setActive(true));
        final UserRole role3 = save(new_(UserRole.class, "ROLE3", "desc").setActive(true));
        save(new_composite(UserAndRoleAssociation.class, user, role2));
        save(new_composite(UserAndRoleAssociation.class, user, role3));

        final UserRolesUpdater updater = createUpdater(user);
        assertEquals(listOf(role1, role2, role3, unitTestRole), new ArrayList<>(updater.getRoles()));

        updater.setAddedIds(linkedSetOf(role1.getId()));
        updater.setRemovedIds(linkedSetOf(role2.getId()));
        assertEquals(listOf(role1, role2, role3, unitTestRole), new ArrayList<>(updater.getRoles()));

        save(updater);

        final UserRolesUpdater newUpdater = createUpdater(user);
        assertEquals(listOf(role1, role2, role3, unitTestRole), new ArrayList<>(newUpdater.getRoles()));
    }

}
