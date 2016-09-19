package ua.com.fielden.platform.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.Test;

import ua.com.fielden.platform.dao.IUserAndRoleAssociation;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.security.user.UserAndRoleAssociation;
import ua.com.fielden.platform.security.user.UserRole;
import ua.com.fielden.platform.security.user.UserRolesUpdater;
import ua.com.fielden.platform.security.user.UserRolesUpdaterProducer;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.web.centre.CentreConfigUpdater;
import ua.com.fielden.platform.web.centre.CentreConfigUpdaterProducer;
import ua.com.fielden.platform.web.centre.CentreContext;

/**
 * This test case is intended to check correctness of existing collection modification validation logic.
 * <p>
 * This includes validations of 1) master entity disappearance 2) available entity disappearance 
 * 3) collection modification conflict detection. 
 * <p>
 * These conflicts are most likely taken place after the user has been opened the dialog for collection modification, but hasn't finished collection modification by clicking on SAVE button.
 * 
 * @author TG Team
 *
 */
public class CollectionModificationValidationTest extends AbstractDaoTestCase {
    private final Logger logger = Logger.getLogger(getClass());
    private final String newUsername = "NEW_USER";
    
    private CentreConfigUpdater createUpdaterWithoutMasterEntity() {
        final CentreConfigUpdaterProducer producer = getInstance(CentreConfigUpdaterProducer.class);
        final CentreContext<AbstractEntity<?>, AbstractEntity<?>> centreContext = new CentreContext<>();
        producer.setCentreContext(centreContext);
        return producer.newEntity();
    }
    
    private UserRolesUpdater createUpdater(final User user) {
        final UserRolesUpdaterProducer producer = getInstance(UserRolesUpdaterProducer.class);
        final CentreContext<AbstractEntity<?>, AbstractEntity<?>> centreContext = new CentreContext<>();
        centreContext.setMasterEntity(user);
        producer.setCentreContext(centreContext);
        return producer.newEntity();
    }
    
    @Test
    public void collection_modification_requires_master_entity() {
        try {
            createUpdaterWithoutMasterEntity();
            fail("Collection modification should fail.");
        } catch (final Exception ex) {
            logger.error(ex.getMessage(), ex);
            assertTrue(ex instanceof Result);
            assertTrue(ex instanceof Result && ((Result) ex).getMessage().equals("The master entity for collection modification is not provided in the context."));
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
        final User user = save(new_(User.class, newUsername));
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
        final User user = save(new_(User.class, newUsername));
        createUpdater(user);
        
        final List<Long> usersToDelete = new ArrayList<>();
        usersToDelete.add(user.getId());
        co(User.class).batchDelete(usersToDelete);
        
        try {
            createUpdater(user);
            fail("Collection modification should fail.");
        } catch (final Exception ex) {
            assertTrue(ex instanceof Result && ((Result) ex).getMessage().equals("The master entity has been deleted. " + "Please cancel this action and try again."));
        }
    }
    
    @Test
    public void available_entity_removal_prevents_collection_modification_when_unselecting_it() {
        final User user = save(new_(User.class, newUsername));
        
        final UserRole role1 = save(new_(UserRole.class, "ROLE1", "desc").setActive(true));
        final UserRole role2 = save(new_(UserRole.class, "ROLE2", "desc").setActive(true));
        final UserRole role3 = save(new_(UserRole.class, "ROLE3", "desc").setActive(true));
        final UserAndRoleAssociation userToRole2 = save(new_composite(UserAndRoleAssociation.class, user, role2));
        final UserAndRoleAssociation userToRole3 = save(new_composite(UserAndRoleAssociation.class, user, role3));
        
        final UserRolesUpdater originalUpdater = createUpdater(user);
        final Set<Long> removedIds = new HashSet<>();
        removedIds.add(role2.getId());
        originalUpdater.setRemovedIds(removedIds);
        
        // removal of available entity
        final Set<UserAndRoleAssociation> associationsToDelete = new HashSet<>();
        associationsToDelete.add(userToRole2);
        this.<IUserAndRoleAssociation, UserAndRoleAssociation>co(UserAndRoleAssociation.class).removeAssociation(associationsToDelete);
        
        final List<Long> rolesToDelete = new ArrayList<>();
        rolesToDelete.add(role2.getId());
        co(UserRole.class).batchDelete(rolesToDelete);

        // starting the process of saving: a) produce it first (should be successfull) b) save
        final UserRolesUpdater updater = createUpdater(user);
        updater.setRemovedIds(removedIds);
        
        try {
            save(updater);
            fail("Collection modification should fail.");
        } catch (final Exception ex) {
            assertTrue(ex instanceof Result && ((Result) ex).getMessage().equals("Another user has deleted the item, that you're trying to un-tick. " + "Please cancel this action and try again."));
        }
    }
    
    @Test
    public void available_entity_removal_prevents_collection_modification_when_selecting_it() {
        final User user = save(new_(User.class, newUsername));
        
        final UserRole role1 = save(new_(UserRole.class, "ROLE1", "desc").setActive(true));
        final UserRole role2 = save(new_(UserRole.class, "ROLE2", "desc").setActive(true));
        final UserRole role3 = save(new_(UserRole.class, "ROLE3", "desc").setActive(true));
        final UserAndRoleAssociation userToRole2 = save(new_composite(UserAndRoleAssociation.class, user, role2));
        final UserAndRoleAssociation userToRole3 = save(new_composite(UserAndRoleAssociation.class, user, role3));
        
        final UserRolesUpdater originalUpdater = createUpdater(user);
        final Set<Long> addedIds = new HashSet<>();
        addedIds.add(role1.getId());
        originalUpdater.setAddedIds(addedIds);
        
        // removal of available entity
        final List<Long> rolesToDelete = new ArrayList<>();
        rolesToDelete.add(role1.getId());
        co(UserRole.class).batchDelete(rolesToDelete);

        // starting the process of saving: a) produce it first (should be successfull) b) save
        final UserRolesUpdater updater = createUpdater(user);
        updater.setAddedIds(addedIds);
        
        try {
            save(updater);
            fail("Collection modification should fail.");
        } catch (final Exception ex) {
            assertTrue(ex instanceof Result && ((Result) ex).getMessage().equals("Another user has deleted the item, that you're trying to choose. " + "Please cancel this action and try again."));
        }
    }

    
    @Test
    public void another_recorded_collection_modification_prevents_collection_modification() {
        final User user = save(new_(User.class, newUsername));
        
        final UserRole role1 = save(new_(UserRole.class, "ROLE1", "desc").setActive(true));
        final UserRole role2 = save(new_(UserRole.class, "ROLE2", "desc").setActive(true));
        final UserRole role3 = save(new_(UserRole.class, "ROLE3", "desc").setActive(true));
        final UserAndRoleAssociation userToRole2 = save(new_composite(UserAndRoleAssociation.class, user, role2));
        final UserAndRoleAssociation userToRole3 = save(new_composite(UserAndRoleAssociation.class, user, role3));
        
        final UserRolesUpdater originalUpdater = createUpdater(user);
        final Set<Long> addedIds = new HashSet<>();
        addedIds.add(role1.getId());
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
        final User user = save(new_(User.class, newUsername));
        
        final UserRole role1 = save(new_(UserRole.class, "ROLE1", "desc").setActive(true));
        final UserRole role2 = save(new_(UserRole.class, "ROLE2", "desc").setActive(true));
        final UserRole role3 = save(new_(UserRole.class, "ROLE3", "desc").setActive(true));
        final UserAndRoleAssociation userToRole2 = save(new_composite(UserAndRoleAssociation.class, user, role2));
        final UserAndRoleAssociation userToRole3 = save(new_composite(UserAndRoleAssociation.class, user, role3));
        
        final UserRolesUpdater updater = createUpdater(user);
        final Set<Long> addedIds = new HashSet<>();
        addedIds.add(role1.getId());
        updater.setAddedIds(addedIds);
        final Set<Long> removedIds = new HashSet<>();
        removedIds.add(role2.getId());
        updater.setRemovedIds(removedIds);
        save(updater);
        
        final UserRolesUpdater newUpdater = createUpdater(user);
        assertEquals(new LinkedHashSet<Long>() {{ add(role3.getId()); add(role1.getId()); }}, newUpdater.getChosenIds());
    }
}