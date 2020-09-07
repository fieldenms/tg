package ua.com.fielden.platform.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;

import org.apache.log4j.Logger;
import org.junit.Test;

import ua.com.fielden.platform.dao.IUserAndRoleAssociation;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.security.tokens.AlwaysAccessibleToken;
import ua.com.fielden.platform.security.tokens.CompoundModuleToken;
import ua.com.fielden.platform.security.tokens.attachment.AttachmentDownload_CanExecute_Token;
import ua.com.fielden.platform.security.tokens.attachment.Attachment_CanDelete_Token;
import ua.com.fielden.platform.security.tokens.attachment.Attachment_CanSave_Token;
import ua.com.fielden.platform.security.tokens.compound_master_menu.TgCompoundEntityMaster_OpenMain_MenuItem_CanAccess_Token;
import ua.com.fielden.platform.security.tokens.compound_master_menu.TgCompoundEntityMaster_OpenTgCompoundEntityChild_MenuItem_CanAccess_Token;
import ua.com.fielden.platform.security.tokens.compound_master_menu.TgCompoundEntityMaster_OpenTgCompoundEntityDetail_MenuItem_CanAccess_Token;
import ua.com.fielden.platform.security.tokens.open_compound_master.OpenTgCompoundEntityMasterAction_CanOpen_Token;
import ua.com.fielden.platform.security.tokens.persistent.TgCompoundEntityChild_CanDelete_Token;
import ua.com.fielden.platform.security.tokens.persistent.TgCompoundEntityChild_CanSave_Token;
import ua.com.fielden.platform.security.tokens.persistent.TgCompoundEntityDetail_CanSave_Token;
import ua.com.fielden.platform.security.tokens.persistent.TgCompoundEntity_CanDelete_Token;
import ua.com.fielden.platform.security.tokens.persistent.TgCompoundEntity_CanSave_Token;
import ua.com.fielden.platform.security.tokens.user.UserRoleTokensUpdater_CanExecute_Token;
import ua.com.fielden.platform.security.tokens.user.UserRole_CanDelete_Token;
import ua.com.fielden.platform.security.tokens.user.UserRole_CanSave_Token;
import ua.com.fielden.platform.security.tokens.user.UserRolesUpdater_CanExecute_Token;
import ua.com.fielden.platform.security.tokens.user.User_CanDelete_Token;
import ua.com.fielden.platform.security.tokens.user.User_CanSave_Token;
import ua.com.fielden.platform.security.user.SecurityTokenInfo;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.security.user.UserAndRoleAssociation;
import ua.com.fielden.platform.security.user.UserRole;
import ua.com.fielden.platform.security.user.UserRoleTokensUpdater;
import ua.com.fielden.platform.security.user.UserRoleTokensUpdaterProducer;
import ua.com.fielden.platform.security.user.UserRolesUpdater;
import ua.com.fielden.platform.security.user.UserRolesUpdaterProducer;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.web.centre.CentreContext;

/**
 * This test case is intended to check correctness of existing collection modification validation logic.
 * <p>
 * This includes validations of 1) master entity disappearance 2) available entity disappearance 
 * 3) collection modification conflict detection.
 * <p>
 * These conflicts are most likely taken place after the user has been opened the dialog for collection modification, but hasn't finished collection modification by clicking on SAVE button.
 * 
 * Also 4) order of available entities (user roles, userRole tokens) are checked, by-key order is required.
 * 
 * @author TG Team
 *
 */
public class CollectionModificationValidationTest extends AbstractDaoTestCase {
    private final Logger logger = Logger.getLogger(getClass());
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
    
    private UserRoleTokensUpdater createUpdater(final UserRole userRole) {
        final UserRoleTokensUpdaterProducer producer = getInstance(UserRoleTokensUpdaterProducer.class);
        final CentreContext<AbstractEntity<?>, AbstractEntity<?>> context = new CentreContext<>();
        context.setMasterEntity(userRole);
        producer.setContext(context);
        return producer.newEntity();
    }
    
    @SafeVarargs
    private static <T> HashSet<T> setOf(final T ... elements) {
        return new HashSet<T>(Arrays.asList(elements));
    }
    @SafeVarargs
    private static <T> LinkedHashSet<T> linkedSetOf(final T ... elements) {
        return new LinkedHashSet<T>(Arrays.asList(elements));
    }
    @SafeVarargs
    private static <T> ArrayList<T> listOf(final T ... elements) {
        return new ArrayList<T>(Arrays.asList(elements));
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
        final User user = save(new_(User.class, newUsername));
        
        save(new_(UserRole.class, "ROLE1", "desc").setActive(true));
        final UserRole role2 = save(new_(UserRole.class, "ROLE2", "desc").setActive(true));
        final UserRole role3 = save(new_(UserRole.class, "ROLE3", "desc").setActive(true));
        final UserAndRoleAssociation userToRole2 = save(new_composite(UserAndRoleAssociation.class, user, role2));
        save(new_composite(UserAndRoleAssociation.class, user, role3));
        
        final UserRolesUpdater updater = createUpdater(user);
        final LinkedHashSet<Long> removedIds = linkedSetOf(role2.getId());
        updater.setRemovedIds(removedIds);
        
        // removal of available entity
        this.<IUserAndRoleAssociation, UserAndRoleAssociation>co$(UserAndRoleAssociation.class).removeAssociation(setOf(userToRole2));
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
        final User user = save(new_(User.class, newUsername));
        
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
            assertTrue(ex instanceof Result && ((Result) ex).getMessage().equals("Another user has deleted the item, that you're trying to choose. " + "Please cancel this action and try again."));
        }
    }

    
    @Test
    public void another_recorded_collection_modification_prevents_collection_modification() {
        final User user = save(new_(User.class, newUsername));
        
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
        final User user = save(new_(User.class, newUsername));
        
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
        final User user = save(new_(User.class, newUsername));
        
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
    
    @Test
    public void available_entities_are_ordered_by_key_and_such_order_does_not_mutate_during_validation_cycles_in_userRole_tokens_collectional_editor() {
        final UserRole userRole = save(new_(UserRole.class, "ROLE1", "desc").setActive(true));
        
        final SecurityTokenInfo alwaysAccessible = EntityFactory.newPlainEntity(SecurityTokenInfo.class, null).setKey(AlwaysAccessibleToken.class.getName());

        final SecurityTokenInfo userDelete = EntityFactory.newPlainEntity(SecurityTokenInfo.class, null).setKey(User_CanDelete_Token.class.getName());
        final SecurityTokenInfo userSave = EntityFactory.newPlainEntity(SecurityTokenInfo.class, null).setKey(User_CanSave_Token.class.getName());
        final SecurityTokenInfo userRoleDelete = EntityFactory.newPlainEntity(SecurityTokenInfo.class, null).setKey(UserRole_CanDelete_Token.class.getName());
        final SecurityTokenInfo userRoleSave = EntityFactory.newPlainEntity(SecurityTokenInfo.class, null).setKey(UserRole_CanSave_Token.class.getName());
        final SecurityTokenInfo userRolesUpdater_CanExecute = EntityFactory.newPlainEntity(SecurityTokenInfo.class, null).setKey(UserRolesUpdater_CanExecute_Token.class.getName());
        final SecurityTokenInfo userRoleTokensUpdater_CanExecute = EntityFactory.newPlainEntity(SecurityTokenInfo.class, null).setKey(UserRoleTokensUpdater_CanExecute_Token.class.getName());

        final SecurityTokenInfo attachmentSave = EntityFactory.newPlainEntity(SecurityTokenInfo.class, null).setKey(Attachment_CanSave_Token.class.getName());
        final SecurityTokenInfo attachmentDelete = EntityFactory.newPlainEntity(SecurityTokenInfo.class, null).setKey(Attachment_CanDelete_Token.class.getName());
        final SecurityTokenInfo attachmentDownload = EntityFactory.newPlainEntity(SecurityTokenInfo.class, null).setKey(AttachmentDownload_CanExecute_Token.class.getName());
        
        final SecurityTokenInfo compoundModule = EntityFactory.newPlainEntity(SecurityTokenInfo.class, null).setKey(CompoundModuleToken.class.getName());
        final SecurityTokenInfo tgComoundEntity_CanDelete = EntityFactory.newPlainEntity(SecurityTokenInfo.class, null).setKey(TgCompoundEntity_CanDelete_Token.class.getName());
        final SecurityTokenInfo tgCompoundEntity_CanSave = EntityFactory.newPlainEntity(SecurityTokenInfo.class, null).setKey(TgCompoundEntity_CanSave_Token.class.getName());
        final SecurityTokenInfo tgCompoundEntityChild_CanDelete = EntityFactory.newPlainEntity(SecurityTokenInfo.class, null).setKey(TgCompoundEntityChild_CanDelete_Token.class.getName());
        final SecurityTokenInfo tgCompoundEntityChild_CanSave = EntityFactory.newPlainEntity(SecurityTokenInfo.class, null).setKey(TgCompoundEntityChild_CanSave_Token.class.getName());
        final SecurityTokenInfo tgCompoundEntityDetail_CanSave = EntityFactory.newPlainEntity(SecurityTokenInfo.class, null).setKey(TgCompoundEntityDetail_CanSave_Token.class.getName());
        final SecurityTokenInfo openTgCompoundEntityMasterAction_CanOpen = EntityFactory.newPlainEntity(SecurityTokenInfo.class, null).setKey(OpenTgCompoundEntityMasterAction_CanOpen_Token.class.getName());
        final SecurityTokenInfo tgCompoundEntityMaster_OpenMain_MenuItem_CanAccess = EntityFactory.newPlainEntity(SecurityTokenInfo.class, null).setKey(TgCompoundEntityMaster_OpenMain_MenuItem_CanAccess_Token.class.getName());
        final SecurityTokenInfo tgCompoundEntityMaster_OpenTgCompoundEntityChild_MenuItem_CanAccess = EntityFactory.newPlainEntity(SecurityTokenInfo.class, null).setKey(TgCompoundEntityMaster_OpenTgCompoundEntityChild_MenuItem_CanAccess_Token.class.getName());
        final SecurityTokenInfo tgCompoundEntityMaster_OpenTgCompoundEntityDetail_MenuItem_CanAccess = EntityFactory.newPlainEntity(SecurityTokenInfo.class, null).setKey(TgCompoundEntityMaster_OpenTgCompoundEntityDetail_MenuItem_CanAccess_Token.class.getName());

        final UserRoleTokensUpdater updater = createUpdater(userRole);
        final HashSet<SecurityTokenInfo> expectedTokens = setOf(
            alwaysAccessible,
            userDelete, userSave,
            userRoleDelete, userRoleSave,
            userRolesUpdater_CanExecute, userRoleTokensUpdater_CanExecute,
            attachmentSave, attachmentDelete, attachmentDownload,
            compoundModule, tgComoundEntity_CanDelete, tgCompoundEntity_CanSave, tgCompoundEntityChild_CanDelete, tgCompoundEntityChild_CanSave, tgCompoundEntityDetail_CanSave, openTgCompoundEntityMasterAction_CanOpen, 
            tgCompoundEntityMaster_OpenMain_MenuItem_CanAccess, tgCompoundEntityMaster_OpenTgCompoundEntityChild_MenuItem_CanAccess, tgCompoundEntityMaster_OpenTgCompoundEntityDetail_MenuItem_CanAccess
        );
        assertEquals(expectedTokens, updater.getTokens());
        
        updater.setAddedIds(linkedSetOf(userDelete.getKey(), userRoleSave.getKey()));
        assertEquals(expectedTokens, updater.getTokens());
        
        save(updater);
        
        final UserRoleTokensUpdater newUpdater = createUpdater(userRole);
        assertEquals(expectedTokens, newUpdater.getTokens());
    }
}