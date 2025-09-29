package ua.com.fielden.platform.security.authorisation;

import static org.junit.Assert.*;
import static ua.com.fielden.platform.sample.domain.ITgFuelType.DEFAULT_VALUE_FOR_PROP_guarded;
import static ua.com.fielden.platform.sample.domain.ITgFuelType.FETCH_PROVIDER_FOR_EDITING;

import org.junit.Before;
import org.junit.Test;

import ua.com.fielden.platform.devdb_support.SecurityTokenAssociator;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.sample.domain.TgFuelType;
import ua.com.fielden.platform.sample.domain.TgPerson;
import ua.com.fielden.platform.security.tokens.persistent.TgFuelType_CanDelete_Token;
import ua.com.fielden.platform.security.tokens.persistent.TgFuelType_CanModify_guarded_Token;
import ua.com.fielden.platform.security.tokens.persistent.TgFuelType_CanSaveNew_Token;
import ua.com.fielden.platform.security.provider.SecurityTokenNode;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.SecurityRoleAssociation;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.security.user.UserAndRoleAssociation;
import ua.com.fielden.platform.security.user.UserRole;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

public class AuthorisationTestCase extends AbstractDaoTestCase {
    private static final String FUEL_TYPE = "U";
    private static final String PERMISSIVE_USERNAME = "TESTUSER";
    private static final String RESTRICTIVE_USERNAME = "TESTUSERRESTRICTIVE";
    private static final String PERMISSIVE_ROLE = "ADMINISTRATION";
    private static final String RESTRICTIVE_ROLE = "CAN CREATE NEW FUEL TYPE";

    @Before
    public void setUp() {
        // set permissive user as the current user before each test
        final IUserProvider up = getInstance(IUserProvider.class);
        up.setUsername(PERMISSIVE_USERNAME, getInstance(IUser.class));
    }

    @Test
    public void user_with_access_to_CanDelete_token_can_delete_entities() {
        final TgFuelType ft = co$(TgFuelType.class).findByKey(FUEL_TYPE);
        assertNotNull(ft);

        co$(TgFuelType.class).delete(ft);

        assertNull(co$(TgFuelType.class).findByKey(FUEL_TYPE));
    }

    @Test
    public void user_with_access_to_CanModify_token_can_modify_property_for_a_persisted_entity() {
        final TgFuelType ft = co$(TgFuelType.class).findByKeyAndFetch(FETCH_PROVIDER_FOR_EDITING.fetchModel(), FUEL_TYPE);
        assertNotNull(ft);

        ft.setGuardedIfPersisted("some value");

        final MetaProperty<String> mpGuarded = ft.getProperty("guardedIfPersisted");
        assertTrue(mpGuarded.isValid());
        assertEquals("some value", ft.getGuardedIfPersisted());
    }

    @Test
    public void user_without_access_to_CanDelete_token_cannot_delete_entities() {
        final IUserProvider up = getInstance(IUserProvider.class);
        up.setUsername(RESTRICTIVE_USERNAME, getInstance(IUser.class));

        final TgFuelType ft = co$(TgFuelType.class).findByKey(FUEL_TYPE);
        assertNotNull(ft);
        try {
            co$(TgFuelType.class).delete(ft);
            fail();
        } catch (final Result ex) {
            assertEquals("Permission denied due to token [%s] restriction.".formatted(TgFuelType_CanDelete_Token.TITLE), ex.getMessage());
        }
    }

    @Test
    public void user_without_access_to_CanModify_token_cannot_modify_property_for_a_persisted_entity() {
        final IUserProvider up = getInstance(IUserProvider.class);
        up.setUsername(RESTRICTIVE_USERNAME, getInstance(IUser.class));

        final TgFuelType ft = co$(TgFuelType.class).findByKeyAndFetch(FETCH_PROVIDER_FOR_EDITING.fetchModel(), FUEL_TYPE);
        assertNotNull(ft);

        ft.setGuardedIfPersisted("some value");

        final MetaProperty<String> mpGuarded = ft.getProperty("guardedIfPersisted");
        assertFalse(mpGuarded.isValid());
        assertEquals("Permission denied due to token [%s] restriction.".formatted(TgFuelType_CanModify_guarded_Token.TITLE), mpGuarded.getFirstFailure().getMessage());
    }

    @Test
    public void user_without_access_to_CanModify_token_can_set_property_to_its_original_value_for_a_persisted_entity() {
        final IUserProvider up = getInstance(IUserProvider.class);
        up.setUsername(RESTRICTIVE_USERNAME, getInstance(IUser.class));

        final TgFuelType ft = co$(TgFuelType.class).findByKeyAndFetch(FETCH_PROVIDER_FOR_EDITING.fetchModel(), FUEL_TYPE);
        assertNotNull(ft);

        ft.setGuardedIfPersisted("some value");

        final MetaProperty<String> mpGuarded = ft.getProperty("guardedIfPersisted");
        assertFalse(mpGuarded.isValid());
        assertEquals("Permission denied due to token [%s] restriction.".formatted(TgFuelType_CanModify_guarded_Token.TITLE), mpGuarded.getFirstFailure().getMessage());

        ft.setGuardedIfPersisted(DEFAULT_VALUE_FOR_PROP_guarded);
        assertTrue(mpGuarded.isValid());
        assertEquals(DEFAULT_VALUE_FOR_PROP_guarded, ft.getGuardedIfPersisted());
    }

    @Test
    public void user_without_access_to_CanModify_token_can_modify_property_for_a_not_persisted_entity() {
        final IUserProvider up = getInstance(IUserProvider.class);
        up.setUsername(RESTRICTIVE_USERNAME, getInstance(IUser.class));

        final TgFuelType ft = new_(TgFuelType.class, "D", "Diesel");
        assertNotNull(ft);

        ft.setGuardedIfPersisted("some value");

        final MetaProperty<String> mpGuarded = ft.getProperty("guardedIfPersisted");
        assertTrue(mpGuarded.isValid());
        assertEquals("some value", ft.getGuardedIfPersisted());
    }

    @Test
    public void user_without_access_to_CanModify_token_cannot_modify_property_without_persistedOnly_option_for_a_not_persisted_entity() {
        final IUserProvider up = getInstance(IUserProvider.class);
        up.setUsername(RESTRICTIVE_USERNAME, getInstance(IUser.class));

        final TgFuelType ft = new_(TgFuelType.class, "D", "Diesel");
        assertNotNull(ft);

        ft.setGuardedEvenIfNotPersisted("some value");

        final MetaProperty<String> mpGuarded = ft.getProperty("guardedEvenIfNotPersisted");
        assertFalse(mpGuarded.isValid());
        assertEquals("Permission denied due to token [%s] restriction.".formatted(TgFuelType_CanModify_guarded_Token.TITLE), mpGuarded.getFirstFailure().getMessage());
    }

    @Test
    public void user_without_access_to_CanModify_token_can_set_property_to_its_original_value_for_a_not_persisted_entity() {
        final IUserProvider up = getInstance(IUserProvider.class);
        up.setUsername(RESTRICTIVE_USERNAME, getInstance(IUser.class));

        final TgFuelType ft = new_(TgFuelType.class, "D", "Diesel");
        assertNotNull(ft);

        ft.setGuardedEvenIfNotPersisted("some value");

        final MetaProperty<String> mpGuarded = ft.getProperty("guardedEvenIfNotPersisted");
        assertFalse(mpGuarded.isValid());
        assertEquals("Permission denied due to token [%s] restriction.".formatted(TgFuelType_CanModify_guarded_Token.TITLE), mpGuarded.getFirstFailure().getMessage());

        ft.setGuardedEvenIfNotPersisted(DEFAULT_VALUE_FOR_PROP_guarded);
        assertTrue(mpGuarded.isValid());
        assertEquals(DEFAULT_VALUE_FOR_PROP_guarded, ft.getGuardedIfPersisted());
    }

    @Test 
    public void originally_permissive_user_becomes_restrictive_once_its_role_gets_deactivated() {
        final IUserProvider up = getInstance(IUserProvider.class);
        up.setUsername(UNIT_TEST_USER, co$(User.class));

        final UserRole role = co$(UserRole.class).findByKey(PERMISSIVE_ROLE);
        assertTrue(role.isActive());
        // let's now deactivate the role 
        save(role.setActive(false));

        // switch back to a permissive user and try performing a "permissive" action
        up.setUsername(PERMISSIVE_USERNAME, co$(User.class));
        final TgFuelType ft = co$(TgFuelType.class).findByKey(FUEL_TYPE);
        assertNotNull(ft);
        
        try {
            co$(TgFuelType.class).delete(ft);
            fail();
        } catch (final Result ex) {
            assertEquals("Permission denied due to token [%s] restriction.".formatted(TgFuelType_CanDelete_Token.TITLE), ex.getMessage());
        }
    }
    
    @Override
    protected void populateDomain() {
        super.populateDomain();
        // for testing authorisation we need a user, a role and association between that user and role, and role with designated security token
        // so, create persons that are users at the same time -- one permissive and one restrictive
        final IUser coUser = co$(User.class);
        final User permissiveUser;
        save(new_(TgPerson.class, "Permissive Person").setUser(permissiveUser = coUser.save(new_(User.class, PERMISSIVE_USERNAME).setBase(true))));
        final User restrictiveUser;
        save(new_(TgPerson.class, "Restrictive Person").setUser(restrictiveUser = coUser.save(new_(User.class, RESTRICTIVE_USERNAME).setBase(true))));

        // now create user roles
        final UserRole permissiveRole = save(new_(UserRole.class, PERMISSIVE_ROLE, "A role, which has a full access to the the system and should be used only for users who need administrative previligies.").setActive(true));
        final UserRole restrictiveRole = save(new_(UserRole.class, RESTRICTIVE_ROLE, "A role, which has access only to the CanSaveNew.").setActive(true));
        // associate permissive role with the permissive user
        save(new_composite(UserAndRoleAssociation.class, permissiveUser, permissiveRole));
        // associated restrictive role only with the restrictive user
        save(new_composite(UserAndRoleAssociation.class, restrictiveUser, restrictiveRole));

        // now let's reuse our standard logic for associating roles and security tokens, which is the last step in this security setup process
        // please note that in this test case only a top level security token is used
        // in case of sub tokens, a tree, not just a single node, would need to be created
        final SecurityTokenAssociator permissiveAssociator = new SecurityTokenAssociator(permissiveRole, co$(SecurityRoleAssociation.class));
        final SecurityTokenNode stnCanDeleteToken = SecurityTokenNode.makeTopLevelNode(TgFuelType_CanDelete_Token.class);
        permissiveAssociator.eval(stnCanDeleteToken);
        final SecurityTokenNode stnCanModify_guarded_Token = SecurityTokenNode.makeTopLevelNode(TgFuelType_CanModify_guarded_Token.class);
        permissiveAssociator.eval(stnCanModify_guarded_Token);
        final SecurityTokenNode stnCanSaveNew_Token = SecurityTokenNode.makeTopLevelNode(TgFuelType_CanSaveNew_Token.class);
        permissiveAssociator.eval(stnCanSaveNew_Token);

        // restrictive role should only be associated with CanSaveNew
        final SecurityTokenAssociator restrictiveAssociator = new SecurityTokenAssociator(restrictiveRole, co$(SecurityRoleAssociation.class));
        restrictiveAssociator.eval(stnCanSaveNew_Token);

        // we also need some fuel types, so that we could test whether their deletion is guarded by token
        save(new_(TgFuelType.class, FUEL_TYPE, "Unleaded"));
    }

}
