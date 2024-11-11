package ua.com.fielden.platform.entity;

import static org.apache.logging.log4j.LogManager.getLogger;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static ua.com.fielden.platform.entity.factory.EntityFactory.newPlainEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.function.Function;

import org.apache.logging.log4j.Logger;
import org.junit.Test;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.tokens.AlwaysAccessibleToken;
import ua.com.fielden.platform.security.tokens.CompoundModuleToken;
import ua.com.fielden.platform.security.tokens.attachment.AttachmentDownload_CanExecute_Token;
import ua.com.fielden.platform.security.tokens.attachment.Attachment_CanDelete_Token;
import ua.com.fielden.platform.security.tokens.attachment.Attachment_CanReadModel_Token;
import ua.com.fielden.platform.security.tokens.attachment.Attachment_CanRead_Token;
import ua.com.fielden.platform.security.tokens.attachment.Attachment_CanSave_Token;
import ua.com.fielden.platform.security.tokens.compound_master_menu.TgCompoundEntityMaster_OpenMain_MenuItem_CanAccess_Token;
import ua.com.fielden.platform.security.tokens.compound_master_menu.TgCompoundEntityMaster_OpenTgCompoundEntityChild_MenuItem_CanAccess_Token;
import ua.com.fielden.platform.security.tokens.compound_master_menu.TgCompoundEntityMaster_OpenTgCompoundEntityDetail_MenuItem_CanAccess_Token;
import ua.com.fielden.platform.security.tokens.open_compound_master.OpenTgCompoundEntityMasterAction_CanOpen_Token;
import ua.com.fielden.platform.security.tokens.open_simple_master.*;
import ua.com.fielden.platform.security.tokens.persistent.*;
import ua.com.fielden.platform.security.tokens.synthetic.DomainExplorer_CanReadModel_Token;
import ua.com.fielden.platform.security.tokens.synthetic.DomainExplorer_CanRead_Token;
import ua.com.fielden.platform.security.tokens.user.ReUser_CanReadModel_Token;
import ua.com.fielden.platform.security.tokens.user.ReUser_CanRead_Token;
import ua.com.fielden.platform.security.tokens.user.UserAndRoleAssociation_CanDelete_Token;
import ua.com.fielden.platform.security.tokens.user.UserAndRoleAssociation_CanReadModel_Token;
import ua.com.fielden.platform.security.tokens.user.UserAndRoleAssociation_CanRead_Token;
import ua.com.fielden.platform.security.tokens.user.UserAndRoleAssociation_CanSave_Token;
import ua.com.fielden.platform.security.tokens.user.UserRoleTokensUpdater_CanExecute_Token;
import ua.com.fielden.platform.security.tokens.user.UserRole_CanDelete_Token;
import ua.com.fielden.platform.security.tokens.user.UserRole_CanReadModel_Token;
import ua.com.fielden.platform.security.tokens.user.UserRole_CanRead_Token;
import ua.com.fielden.platform.security.tokens.user.UserRole_CanSave_Token;
import ua.com.fielden.platform.security.tokens.user.UserRolesUpdater_CanExecute_Token;
import ua.com.fielden.platform.security.tokens.user.User_CanDelete_Token;
import ua.com.fielden.platform.security.tokens.user.User_CanReadModel_Token;
import ua.com.fielden.platform.security.tokens.user.User_CanRead_Token;
import ua.com.fielden.platform.security.tokens.user.User_CanSave_Token;
import ua.com.fielden.platform.security.tokens.web_api.GraphiQL_CanExecute_Token;
import ua.com.fielden.platform.security.user.SecurityTokenInfo;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.security.user.UserAndRoleAssociation;
import ua.com.fielden.platform.security.user.UserAndRoleAssociationCo;
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

    private UserRoleTokensUpdater createUpdater(final UserRole userRole) {
        final UserRoleTokensUpdaterProducer producer = getInstance(UserRoleTokensUpdaterProducer.class);
        final CentreContext<AbstractEntity<?>, AbstractEntity<?>> context = new CentreContext<>();
        context.setMasterEntity(userRole);
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

    @Test
    public void available_entities_are_ordered_by_key_and_such_order_does_not_mutate_during_validation_cycles_in_userRole_tokens_collectional_editor() {
        final UserRole userRole = save(new_(UserRole.class, "ROLE1", "desc").setActive(true));

        final SecurityTokenInfo alwaysAccessible = EntityFactory.newPlainEntity(SecurityTokenInfo.class, null).setKey(AlwaysAccessibleToken.class.getName());
        final SecurityTokenInfo domainExplorer_CanRead = EntityFactory.newPlainEntity(SecurityTokenInfo.class, null).setKey(DomainExplorer_CanRead_Token.class.getName());
        final SecurityTokenInfo domainExplorer_CanReadModel = EntityFactory.newPlainEntity(SecurityTokenInfo.class, null).setKey(DomainExplorer_CanReadModel_Token.class.getName());
        final SecurityTokenInfo graphiQL_CanExecute = EntityFactory.newPlainEntity(SecurityTokenInfo.class, null).setKey(GraphiQL_CanExecute_Token.class.getName());
        final SecurityTokenInfo entityMasterHelp_CanSave = EntityFactory.newPlainEntity(SecurityTokenInfo.class, null).setKey(UserDefinableHelp_CanSave_Token.class.getName());
        final SecurityTokenInfo keyNumber_CanRead = EntityFactory.newPlainEntity(SecurityTokenInfo.class, null).setKey(KeyNumber_CanRead_Token.class.getName());
        final SecurityTokenInfo keyNumber_CanReadModel = EntityFactory.newPlainEntity(SecurityTokenInfo.class, null).setKey(KeyNumber_CanReadModel_Token.class.getName());

        final SecurityTokenInfo user_CanDelete = EntityFactory.newPlainEntity(SecurityTokenInfo.class, null).setKey(User_CanDelete_Token.class.getName());
        final SecurityTokenInfo user_CanSave = EntityFactory.newPlainEntity(SecurityTokenInfo.class, null).setKey(User_CanSave_Token.class.getName());
        final SecurityTokenInfo user_CanRead = EntityFactory.newPlainEntity(SecurityTokenInfo.class, null).setKey(User_CanRead_Token.class.getName());
        final SecurityTokenInfo user_CanReadModel = EntityFactory.newPlainEntity(SecurityTokenInfo.class, null).setKey(User_CanReadModel_Token.class.getName());
        final SecurityTokenInfo reUser_CanRead = EntityFactory.newPlainEntity(SecurityTokenInfo.class, null).setKey(ReUser_CanRead_Token.class.getName());
        final SecurityTokenInfo reUser_CanReadModel = EntityFactory.newPlainEntity(SecurityTokenInfo.class, null).setKey(ReUser_CanReadModel_Token.class.getName());
        final SecurityTokenInfo userRole_CanDelete = EntityFactory.newPlainEntity(SecurityTokenInfo.class, null).setKey(UserRole_CanDelete_Token.class.getName());
        final SecurityTokenInfo userRole_CanSave = EntityFactory.newPlainEntity(SecurityTokenInfo.class, null).setKey(UserRole_CanSave_Token.class.getName());
        final SecurityTokenInfo userRole_CanRead = EntityFactory.newPlainEntity(SecurityTokenInfo.class, null).setKey(UserRole_CanRead_Token.class.getName());
        final SecurityTokenInfo userRole_CanReadModel = EntityFactory.newPlainEntity(SecurityTokenInfo.class, null).setKey(UserRole_CanReadModel_Token.class.getName());
        final SecurityTokenInfo userRoleAssociation_CanRead = EntityFactory.newPlainEntity(SecurityTokenInfo.class, null).setKey(UserAndRoleAssociation_CanRead_Token.class.getName());
        final SecurityTokenInfo userRoleAssociation_CanReadModel = EntityFactory.newPlainEntity(SecurityTokenInfo.class, null).setKey(UserAndRoleAssociation_CanReadModel_Token.class.getName());
        final SecurityTokenInfo userRolesUpdater_CanExecute = EntityFactory.newPlainEntity(SecurityTokenInfo.class, null).setKey(UserRolesUpdater_CanExecute_Token.class.getName());
        final SecurityTokenInfo userRoleTokensUpdater_CanExecute = EntityFactory.newPlainEntity(SecurityTokenInfo.class, null).setKey(UserRoleTokensUpdater_CanExecute_Token.class.getName());

        final SecurityTokenInfo attachment_CanSave = EntityFactory.newPlainEntity(SecurityTokenInfo.class, null).setKey(Attachment_CanSave_Token.class.getName());
        final SecurityTokenInfo attachment_CanRead = EntityFactory.newPlainEntity(SecurityTokenInfo.class, null).setKey(Attachment_CanRead_Token.class.getName());
        final SecurityTokenInfo attachment_CanReadModel = EntityFactory.newPlainEntity(SecurityTokenInfo.class, null).setKey(Attachment_CanReadModel_Token.class.getName());
        final SecurityTokenInfo attachment_CanDelete = EntityFactory.newPlainEntity(SecurityTokenInfo.class, null).setKey(Attachment_CanDelete_Token.class.getName());
        final SecurityTokenInfo attachmentDownload_CanExecute = EntityFactory.newPlainEntity(SecurityTokenInfo.class, null).setKey(AttachmentDownload_CanExecute_Token.class.getName());

        final SecurityTokenInfo canRead = EntityFactory.newPlainEntity(SecurityTokenInfo.class, null).setKey(_CanRead_Token.class.getName());
        final SecurityTokenInfo canReadModel = EntityFactory.newPlainEntity(SecurityTokenInfo.class, null).setKey(_CanReadModel_Token.class.getName());

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

        final Function<Class<? extends ISecurityToken>, SecurityTokenInfo> createTokenInfo = (token) -> newPlainEntity(SecurityTokenInfo.class, null).setKey(token.getName());

        final UserRoleTokensUpdater updater = createUpdater(userRole);
        final HashSet<SecurityTokenInfo> expectedTokens = setOf(
            alwaysAccessible,
            domainExplorer_CanRead, domainExplorer_CanReadModel,
            graphiQL_CanExecute, entityMasterHelp_CanSave,
            keyNumber_CanRead, keyNumber_CanReadModel,
            user_CanDelete, user_CanSave, user_CanRead, user_CanReadModel,
            reUser_CanRead, reUser_CanReadModel,
            createTokenInfo.apply(UserMaster_CanOpen_Token.class),
            userRole_CanDelete, userRole_CanSave, userRole_CanRead, userRole_CanReadModel,
            createTokenInfo.apply(UserRoleMaster_CanOpen_Token.class),
            userRoleAssociation_CanRead, userRoleAssociation_CanReadModel, createTokenInfo.apply(UserAndRoleAssociation_CanSave_Token.class), createTokenInfo.apply(UserAndRoleAssociation_CanDelete_Token.class),
            userRolesUpdater_CanExecute, userRoleTokensUpdater_CanExecute,
            attachment_CanSave, attachment_CanRead, attachment_CanReadModel, attachment_CanDelete, createTokenInfo.apply(AttachmentMaster_CanOpen_Token.class), attachmentDownload_CanExecute,
            createTokenInfo.apply(DashboardRefreshFrequencyUnit_CanRead_Token.class),
            createTokenInfo.apply(DashboardRefreshFrequencyUnit_CanReadModel_Token.class),
            createTokenInfo.apply(DashboardRefreshFrequency_CanSave_Token.class),
            createTokenInfo.apply(DashboardRefreshFrequency_CanRead_Token.class),
            createTokenInfo.apply(DashboardRefreshFrequency_CanReadModel_Token.class),
            createTokenInfo.apply(DashboardRefreshFrequency_CanDelete_Token.class),
            createTokenInfo.apply(DashboardRefreshFrequencyMaster_CanOpen_Token.class),
            canRead, canReadModel,
            createTokenInfo.apply(TgCloseLeaveExampleDetailMaster_CanOpen_Token.class),
            createTokenInfo.apply(TgCloseLeaveExampleDetailUnpersistedMaster_CanOpen_Token.class),
            createTokenInfo.apply(TgCloseLeaveExampleMaster_CanOpen_Token.class),
            createTokenInfo.apply(TgCollectionalSerialisationParentMaster_CanOpen_Token.class),
            createTokenInfo.apply(TgCompoundEntityDetailMaster_CanOpen_Token.class),
            createTokenInfo.apply(TgDeletionTestEntityMaster_CanOpen_Token.class),
            createTokenInfo.apply(TgEntityForColourMasterMaster_CanOpen_Token.class),
            createTokenInfo.apply(TgEntityWithPropertyDependencyMaster_CanOpen_Token.class),
            createTokenInfo.apply(TgEntityWithTimeZoneDatesMaster_CanOpen_Token.class),
            createTokenInfo.apply(TgGeneratedEntityForTrippleDecAnalysisMaster_CanOpen_Token.class),
            createTokenInfo.apply(TgGeneratedEntityMaster_CanOpen_Token.class),
            createTokenInfo.apply(TgPersistentCompositeEntityMaster_CanOpen_Token.class),
            createTokenInfo.apply(TgPersistentEntityWithPropertiesMaster_CanOpen_Token.class),

            createTokenInfo.apply(TgEntityWithRichTextProp_CanRead_Token.class),
            createTokenInfo.apply(TgEntityWithRichTextProp_CanSave_Token.class),
            createTokenInfo.apply(TgEntityWithRichTextProp_CanReadModel_Token.class),
            createTokenInfo.apply(TgEntityWithRichTextPropMaster_CanOpen_Token.class),

            createTokenInfo.apply(TgNote_CanRead_Token.class),
            createTokenInfo.apply(TgNote_CanSave_Token.class),
            createTokenInfo.apply(TgNote_CanReadModel_Token.class),
            createTokenInfo.apply(TgNoteMaster_CanOpen_Token.class),

            compoundModule, tgComoundEntity_CanDelete, tgCompoundEntity_CanSave, tgCompoundEntityChild_CanDelete, tgCompoundEntityChild_CanSave, tgCompoundEntityDetail_CanSave, openTgCompoundEntityMasterAction_CanOpen,
            tgCompoundEntityMaster_OpenMain_MenuItem_CanAccess, tgCompoundEntityMaster_OpenTgCompoundEntityChild_MenuItem_CanAccess, tgCompoundEntityMaster_OpenTgCompoundEntityDetail_MenuItem_CanAccess
        );
        assertEquals(expectedTokens, updater.getTokens());

        updater.setAddedIds(linkedSetOf(user_CanDelete.getKey(), userRole_CanSave.getKey()));
        assertEquals(expectedTokens, updater.getTokens());

        save(updater);

        final UserRoleTokensUpdater newUpdater = createUpdater(userRole);
        assertEquals(expectedTokens, newUpdater.getTokens());
    }

}
