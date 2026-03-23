package ua.com.fielden.platform.user;

import org.junit.Test;
import ua.com.fielden.platform.basic.config.IApplicationSettings;
import ua.com.fielden.platform.basic.config.IApplicationSettings.AuthMode;
import ua.com.fielden.platform.entity.annotation.mutator.BeforeChange;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.validation.UserAlmostUniqueEmailValidator;
import ua.com.fielden.platform.property.validator.EmailValidator;
import ua.com.fielden.platform.property.validator.StringValidator;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.security.exceptions.SecurityException;
import ua.com.fielden.platform.security.user.*;
import ua.com.fielden.platform.security.user.validators.UserBaseOnUserValidator;
import ua.com.fielden.platform.test.ioc.UniversalConstantsForTesting;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.utils.IUniversalConstants;

import java.util.Date;
import java.util.Optional;
import java.util.stream.Stream;

import static java.lang.String.format;
import static org.junit.Assert.*;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.entity.ActivatableAbstractEntity.ACTIVE;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchOnly;
import static ua.com.fielden.platform.security.user.User.*;
import static ua.com.fielden.platform.security.user.UserSecret.SECRET_RESET_UUID_SEPERATOR;
import static ua.com.fielden.platform.security.user.UserSecretCo.RESET_UUID_EXPIRATION_IN_MUNUTES;

/// A test case to cover basic user validations.
///
public class UserTestCase extends AbstractDaoTestCase {

    private final IUser coUser = co$(User.class);
    private final UserSecretCo coUserSecret = co$(UserSecret.class);

    @Test
    public void username_does_not_permit_password_reset_UUID_separator() {
        final User user = new_(User.class);
        final String value = format("USER%s1", SECRET_RESET_UUID_SEPERATOR);
        user.setKey(value);
        assertFalse(user.getProperty(KEY).isValid());
        assertEquals(format(StringValidator.validationErrorTemplate, user.getProperty(KEY).getTitle(), TitlesDescsGetter.getEntityTitleAndDesc(User.class).getKey()), user.getProperty("key").validationResult().getMessage());
    }

    @Test
    public void username_permits_spaces_dots_and_at_char() {
        final User user = new_(User.class);
        user.setKey("Name Surname");
        assertEquals("Name Surname", user.getKey());
        user.setKey("Name.Surname");
        assertEquals("Name.Surname", user.getKey());
        user.setKey("Name.Surname@fielden.com.au");
        assertEquals("Name.Surname@fielden.com.au", user.getKey());
    }

    @Test
    public void propety_email_in_user_is_defined_with_almost_unique_validator() {
        final User user1 = coUser.findByKey("USER1");
        final MetaProperty<String> emailProp = user1.getProperty(EMAIL);
        assertTrue(emailProp.getValidationAnnotations().stream().filter(a -> a instanceof BeforeChange).flatMap(a -> Stream.of(((BeforeChange) a).value())).anyMatch(handler -> handler.value().isAssignableFrom(UserAlmostUniqueEmailValidator.class)));
    }
    
    @Test
    public void non_base_users_must_have_unique_email_addresses() {
        final User user5 = coUser.findByKey("USER5");
        user5.setEmail("user5@company.com");
        assertFalse(user5.isBase());
        assertNotNull(coUser.save(user5).getEmail());
        
        final User user6 = coUser.findByKey("USER6");
        user6.setEmail("user5@company.com");
        assertFalse(user6.isBase());
        assertFalse(user6.isValid().isSuccessful());
        assertFalse(user6.getProperty(EMAIL).isValid());
        assertEquals(format(UserAlmostUniqueEmailValidator.validationErrorTemplate, "user5@company.com", EMAIL, User.class.getName()), user6.getProperty(EMAIL).getFirstFailure().getMessage());
    }

    @Test
    public void base_users_may_the_same_email_address() {
        final User user1 = coUser.findByKey("USER1");
        user1.setEmail("user1@company.com");
        assertTrue(user1.isBase());
        assertNotNull(coUser.save(user1).getEmail());
        
        final User user2 = coUser.findByKey("USER2");
        user2.setEmail("user1@company.com");
        assertTrue(user2.isBase());
        assertTrue(user2.isValid().isSuccessful());
        assertTrue(user2.getProperty(EMAIL).isValid());
        assertEquals("user1@company.com", user2.getEmail());
    }

    @Test
    public void base_user_becoming_non_base_cannot_have_the_same_email_address_as_other_base_users() {
        final User user1 = coUser.findByKey("USER1");
        user1.setEmail("user1@company.com");
        assertTrue(user1.isBase());
        assertNotNull(coUser.save(user1).getEmail());
        
        final User user2 = coUser.findByKey("USER2");
        user2.setEmail("user1@company.com");
        assertTrue(user2.isBase());
        
        assertTrue(user2.isValid().isSuccessful());
        assertTrue(user2.getProperty(EMAIL).isValid());
        assertEquals("user1@company.com", user2.getEmail());
        
        user2.setBase(false);
        assertFalse(user2.isValid().isSuccessful());
        assertFalse(user2.getProperty(EMAIL).isValid());
        assertEquals(format(UserAlmostUniqueEmailValidator.validationErrorTemplate, "user1@company.com", EMAIL, User.class.getName()), user2.getProperty(EMAIL).getFirstFailure().getMessage());
    }

    @Test
    public void base_users_may_not_have_the_same_email_address_as_non_base_user() {
        final User user5 = coUser.findByKey("USER5");
        user5.setEmail("user5@company.com");
        assertFalse(user5.isBase());
        assertNotNull(coUser.save(user5).getEmail());
        
        final User user1 = coUser.findByKey("USER1");
        user1.setEmail("user5@company.com");
        assertTrue(user1.isBase());
        assertFalse(user1.isValid().isSuccessful());
        assertFalse(user1.getProperty(EMAIL).isValid());
        assertEquals(format(UserAlmostUniqueEmailValidator.validationErrorTemplate, "user5@company.com", EMAIL, User.class.getName()), user1.getProperty(EMAIL).getFirstFailure().getMessage());
    }

    @Test
    public void non_base_users_may_not_have_the_same_email_address_as_base_user() {
        final User user1 = coUser.findByKey("USER1");
        user1.setEmail("user1@company.com");
        assertTrue(user1.isBase());
        assertNotNull(coUser.save(user1).getEmail());
        
        final User user5 = coUser.findByKey("USER5");
        user5.setEmail("user1@company.com");
        assertFalse(user5.isBase());
        assertFalse(user5.isValid().isSuccessful());
        assertFalse(user5.getProperty(EMAIL).isValid());
        assertEquals(format(UserAlmostUniqueEmailValidator.validationErrorTemplate, "user1@company.com", EMAIL, User.class.getName()), user5.getProperty(EMAIL).getFirstFailure().getMessage());
    }

    @Test
    public void non_base_user_becoming_base_may_have_the_same_email_address_as_other_base_users() {
        final User user1 = coUser.findByKey("USER1");
        user1.setEmail("user1@company.com");
        assertTrue(user1.isBase());
        assertNotNull(coUser.save(user1).getEmail());
        
        final User user5 = coUser.findByKey("USER5");
        user5.setEmail("user1@company.com");
        assertFalse(user5.isBase());
        assertFalse(user5.isValid().isSuccessful());
        assertFalse(user5.getProperty(EMAIL).isValid());
        assertEquals(format(UserAlmostUniqueEmailValidator.validationErrorTemplate, "user1@company.com", EMAIL, User.class.getName()), user5.getProperty(EMAIL).getFirstFailure().getMessage());
        
        user5.setBase(true);
        assertTrue(user5.isBase());
        assertTrue(user5.isValid().isSuccessful());
    }

    @Test
    public void multiple_inactive_users_may_have_their_email_addresses_set_to_null() {
        final User user3 = coUser.findByKey("USER3").setActive(false).setEmail(null);
        assertTrue(user3.isValid().isSuccessful());
        assertNull(coUser.save(user3).getEmail());
        
        final User user4 = coUser.findByKey("USER4").setActive(false).setEmail(null);
        assertTrue(user4.isValid().isSuccessful());
        assertNull(coUser.save(user3).getEmail());
    }

    @Test
    public void email_address_value_has_bacis_validation() {
        
        final User user1 = coUser.findByKey("USER1");
        
        user1.setEmail("user1@");
        assertFalse(user1.getProperty(EMAIL).isValid());
        assertEquals(format(EmailValidator.validationErrorTemplate, "user1@", EMAIL, "User") ,user1.getProperty(EMAIL).getFirstFailure().getMessage());
        
        user1.setEmail("@company.com");
        assertFalse(user1.getProperty(EMAIL).isValid());
        assertEquals(format(EmailValidator.validationErrorTemplate, "@company.com", EMAIL, "User") ,user1.getProperty(EMAIL).getFirstFailure().getMessage());

        user1.setEmail("user1@company . com");
        assertFalse(user1.getProperty(EMAIL).isValid());
        assertEquals(format(EmailValidator.validationErrorTemplate, "user1@company . com", EMAIL, "User") ,user1.getProperty(EMAIL).getFirstFailure().getMessage());
        
        user1.setEmail("user1@company@la.com");
        assertFalse(user1.getProperty(EMAIL).isValid());
        assertEquals(format(EmailValidator.validationErrorTemplate, "user1@company@la.com", EMAIL, "User") ,user1.getProperty(EMAIL).getFirstFailure().getMessage());
        
        user1.setEmail("user1@company.com");
        assertTrue(user1.getProperty(EMAIL).isValid());
    }

    @Test
    public void weak_user_passwords_are_identified() {
        assertFalse(coUser.isPasswordStrong("passW0rd?"));
        assertFalse(coUser.isPasswordStrong("today123"));
        assertFalse(coUser.isPasswordStrong("aaaaa aaaa"));
    }
    
    @Test
    public void strong_user_passwords_are_identified() {
        assertTrue(coUser.isPasswordStrong("today725A !"));
        assertTrue(coUser.isPasswordStrong("My voice is my password. Verify!"));
        assertTrue(coUser.isPasswordStrong("Sentances are strong and memorable passwords"));
    }
    
    @Test 
    public void user_identified_by_username_can_have_correct_password_reset_UUID_generated() {
        final Optional<UserSecret> secret = coUser.assignPasswordResetUuid("USER3", expirationTime());
        assertTrue(secret.isPresent());
        assertNotNull(secret.get().getResetUuid());
    }

    @Test 
    public void locating_user_by_name_during_password_reset_UUID_generation_is_case_insensitive() {
        final Optional<UserSecret> secret = coUser.assignPasswordResetUuid("user3", expirationTime());
        assertTrue(secret.isPresent());
        assertNotNull(secret.get().getResetUuid());
    }
    
    @Test
    public void user_identified_by_email_can_have_correct_password_reset_UUID_generated() {
        final Optional<UserSecret> secret = coUser.assignPasswordResetUuid("user3@company.com", expirationTime());
        assertTrue(secret.isPresent());
        assertNotNull(secret.get().getResetUuid());
    }
    
    @Test
    public void password_reset_UUID_assignment_returns_empty_value_for_unidentified_user() {
        final Optional<UserSecret> secret = coUser.assignPasswordResetUuid("invalid@company.com", expirationTime());
        assertFalse(secret.isPresent());
    }
    
    @Test
    public void password_reset_UUID_structure_is_valid() {
        final UniversalConstantsForTesting consts = (UniversalConstantsForTesting) getInstance(IUniversalConstants.class);
        consts.setNow(dateTime("2016-04-05 14:00:00"));
        
        final Optional<UserSecret> secret = coUser.assignPasswordResetUuid("USER3", expirationTime());
        assertTrue(secret.isPresent());
        
        final String[] uuidParts = secret.get().getResetUuid().split(SECRET_RESET_UUID_SEPERATOR);
        assertEquals(3, uuidParts.length);
        assertEquals(secret.get().getKey().getKey(), uuidParts[0]);
        assertEquals(consts.now().plusMinutes(RESET_UUID_EXPIRATION_IN_MUNUTES).getMillis(), Long.valueOf(uuidParts[2]).longValue());
    }

    @Test
    public void user_can_be_found_by_password_reset_UUID() {
        final UniversalConstantsForTesting consts = (UniversalConstantsForTesting) getInstance(IUniversalConstants.class);
        consts.setNow(dateTime("2016-04-05 14:00:00"));
        
        final String uuid = coUser.assignPasswordResetUuid("USER3", expirationTime()).get().getResetUuid();
        final Optional<User> user = coUser.findUserByResetUuid(uuid);
        assertTrue(user.isPresent());
    }

    @Test
    public void resetting_user_password_removes_reset_UUID() {
        final UserSecret secret = coUser.assignPasswordResetUuid("USER3", expirationTime()).get();
        final UserSecret updatedSecret = coUser.resetPasswd(secret.getKey(), "new and strong password!");
        assertNull(updatedSecret.getResetUuid());
    }
    
    @Test
    public void expired_password_reset_UUID_is_invalid() {
        final UniversalConstantsForTesting consts = (UniversalConstantsForTesting) getInstance(IUniversalConstants.class);
        final String now = "2016-04-05 14:00:00";
        consts.setNow(dateTime(now));
        
        final String uuid = coUser.assignPasswordResetUuid("USER3", expirationTime()).get().getResetUuid();
        assertTrue(coUser.isPasswordResetUuidValid(uuid, consts.now().toDate()));
        // move the time forward
        consts.setNow(dateTime(now).plusDays(2));
        assertFalse(coUser.isPasswordResetUuidValid(uuid, consts.now().toDate()));
    }

    @Test
    public void password_reset_UUID_with_no_associated_user_is_invalid() {
        final UniversalConstantsForTesting consts = (UniversalConstantsForTesting) getInstance(IUniversalConstants.class);
        final String now = "2016-04-05 14:00:00";
        consts.setNow(dateTime(now));
        
        final UserSecret secret = coUser.assignPasswordResetUuid("USER3", expirationTime()).get();
        final String uuid = secret.getResetUuid();
        coUserSecret.save(secret.setResetUuid(null));
        
        assertFalse(coUser.isPasswordResetUuidValid(uuid, consts.now().toDate()));
    }

    @Test
    public void incorrect_password_reset_UUID_associated_with_user_is_recognized_as_invalid() {
        final UniversalConstantsForTesting consts = (UniversalConstantsForTesting) getInstance(IUniversalConstants.class);
        final String now = "2016-04-05 14:00:00";
        consts.setNow(dateTime(now));

        final UserSecret secret = coUserSecret.findByUsername("USER3").get();
        final String uuid = "incorrect password reset UUID";
        coUserSecret.save(secret.setResetUuid(uuid));
        
        final Optional<User> foundUser = coUser.findUserByResetUuid(uuid);
        assertFalse(foundUser.isPresent());
        assertFalse(coUser.isPasswordResetUuidValid(uuid, consts.now().toDate()));
    }

    @Test
    public void password_reset_for_non_existing_user_fails() {
        final UniversalConstantsForTesting consts = (UniversalConstantsForTesting) getInstance(IUniversalConstants.class);
        final String now = "2016-05-19 11:02:00";
        consts.setNow(dateTime(now));
        
        assertFalse(coUser.assignPasswordResetUuid("NON-EXISTING-USER", expirationTime()).isPresent());
    }

    @Test
    public void password_reset_for_inactive_user_fails() {
        final UniversalConstantsForTesting consts = (UniversalConstantsForTesting) getInstance(IUniversalConstants.class);
        final String now = "2016-05-19 11:03:00";
        consts.setNow(dateTime(now));
        
        final User inactiveUser = coUser.findByKey("INACTIVE_USER");
        assertFalse(coUser.assignPasswordResetUuid(inactiveUser.getKey(), expirationTime()).isPresent());
    }

    @Test 
    public void unit_test_user_cannot_be_persisted() {
        final IUser coUser = co$(User.class);
        final User user = new_(User.class, User.system_users.VIRTUAL_USER.name()).setBase(true);
        
        try {
            coUser.save(user);
            fail();
        } catch (final SecurityException ex) {
            assertEquals("VIRTUAL_USER cannot be persisted.", ex.getMessage());
        }
    }
    
    @Test
    public void current_user_has_no_sensitive_information_retrieved_by_default() {
        final IUserProvider up = getInstance(IUserProvider.class);
        up.setUsername(up.getUser().getKey(), co$(User.class)); // refresh the user

        final User currUser = up.getUser();
        assertTrue(currUser.isInstrumented());
        assertFalse(currUser.getProperty(KEY).isProxy());
        assertTrue(currUser.getProperty(EMAIL).isProxy());
        assertFalse(currUser.getProperty(ACTIVE).isProxy());
        assertFalse(currUser.getProperty(BASE).isProxy());
        assertFalse(currUser.getProperty(BASED_ON_USER).isProxy());
        assertFalse(currUser.getProperty(ACTIVE_ROLES).isProxy());
        assertFalse(currUser.getProperty(INACTIVE_ROLES).isProxy());
    }

    @Test
    public void findUser_returns_a_user_with_expected_fetch_model() {
        final User user5 = coUser.findByKeyAndFetch(co$(User.class).getFetchProvider().fetchModel(), "USER5");
        assertTrue(user5.getProperty(BASED_ON_USER).isValid());
        assertEquals("USER1", user5.getBasedOnUser().getKey());
        
        final User baseUser3 = coUser.findByKeyAndFetch(co$(User.class).getFetchProvider().fetchModel(), "USER3");
        user5.setBasedOnUser(baseUser3);
        save(user5);

        final User user5Again = coUser.findUser("USER5"); 
        assertTrue(user5Again.isInstrumented());
        assertFalse(user5Again.getProperty(KEY).isProxy());
        assertTrue(user5Again.getProperty(EMAIL).isProxy());
        assertFalse(user5Again.getProperty(ACTIVE).isProxy());
        assertFalse(user5Again.getProperty(BASE).isProxy());
        assertFalse(user5Again.getProperty(BASED_ON_USER).isProxy());
        assertTrue(user5Again.getBasedOnUser().isIdOnlyProxy());
        assertEquals(user5Again.getBasedOnUser().getId(), baseUser3.getId());
    }

    @Test
    public void self_modification_of_user_instance_result_in_correct_assignment_of_the_last_updated_by_group_of_properties() {
        final IUser co$ = co$(User.class);
        final IUserProvider up = getInstance(IUserProvider.class);
        up.setUsername(up.getUser().getKey(), co$); // refresh the user

        final User currUser = co$.findByEntityAndFetch(co$.getFetchProvider().fetchModel(), up.getUser());
        assertNotNull(currUser);
        assertTrue(currUser.isPersisted());
        assertEquals(0L, currUser.getVersion().longValue());

        final UniversalConstantsForTesting constants = (UniversalConstantsForTesting) getInstance(IUniversalConstants.class);
        constants.setNow(dateTime("2016-05-16 16:36:57"));

        // modify and save
        final String email = "new_email@company.com";
        final User savedUser = save(currUser.setEmail(email));
        assertEquals(1L, savedUser.getVersion().longValue());

        // refresh the user instance in the provider
        up.setUsername(currUser.getKey(), co$(User.class));

        final User user = up.getUser();
        assertTrue(user.isPersisted());
        assertEquals(1L, savedUser.getVersion().longValue());

        assertNotNull(user.getLastUpdatedBy());
        assertEquals(user, user.getLastUpdatedBy());
        assertNotNull(user.getLastUpdatedDate());
        assertEquals(constants.now().toDate(), user.getLastUpdatedDate());
        assertNotNull(user.getLastUpdatedTransactionGuid());
    }
    
    @Test
    public void property_lastUpdatedBy_is_fetched_as_id_only_proxy_even_for_a_fetchOnly_strategy_for_owning_entity() {
        final User user = co$(User.class).findByKeyAndFetch(fetchOnly(User.class), "USER1");
        assertTrue(user.getLastUpdatedBy().isIdOnlyProxy());
    }
 
    @Test
    public void if_base_prop_is_set_to_false_then_prop_basedOnUser_becomes_required() {
        final User user1 = coUser.findByKeyAndFetch(co$(User.class).getFetchProvider().fetchModel(), "USER1");
        user1.setBase(false);
        
        assertTrue(user1.getProperty(BASED_ON_USER).isRequired());
        
        final User user3 = coUser.findByKey("USER2");
        user1.setBasedOnUser(user3);
        
        final User user1saved = save(user1);
        assertEquals(user3, user1saved.getBasedOnUser());
    }


    @Test
    public void if_base_prop_is_set_to_true_then_prop_basedOnUser_becomes_not_required_and_its_value_is_removed() {
        final User user5 = coUser.findByKeyAndFetch(co$(User.class).getFetchProvider().fetchModel(), "USER5");
        assertTrue(user5.getProperty(BASED_ON_USER).isRequired());
        assertNotNull(user5.getBasedOnUser());
        user5.setBase(true);
        assertFalse(user5.getProperty(BASED_ON_USER).isRequired());
        assertNull(user5.getBasedOnUser());
        
        final User user5saved = save(user5);
        assertTrue(user5saved.isBase());
    }
    
    @Test
    public void system_users_cannot_have_property_base_changed() {
        final User unitTestUser = coUser.findByKeyAndFetch(co$(User.class).getFetchProvider().fetchModel(), User.system_users.UNIT_TEST_USER);
        assertNotNull(unitTestUser);
        assertTrue(unitTestUser.isBase());
        assertTrue(unitTestUser.getProperty(BASE).isValid());
        unitTestUser.setBase(false);
        assertFalse(unitTestUser.getProperty(BASE).isValid());
        assertTrue(unitTestUser.isBase());
    }

    @Test
    public void baseUser_cannot_be_assigned_to_itself() {
        final User user1 = coUser.findByKeyAndFetch(IUser.FETCH_PROVIDER.fetchModel(), "USER1");
        assertTrue(user1.isBase());
        assertTrue(user1.getProperty(BASED_ON_USER).isValid());
        user1.setBasedOnUser(coUser.findByKeyAndFetch(IUser.FETCH_PROVIDER.fetchModel(), "USER1"));
        
        assertFalse(user1.getProperty(BASED_ON_USER).isValid());
        assertEquals(UserBaseOnUserValidator.SELF_REFERENCE_IS_NOT_PERMITTED, user1.getProperty(BASED_ON_USER).getFirstFailure().getMessage());
    }

    @Test
    public void system_users_cannot_have_property_basedOnUser_changed() {
        final User unitTestUser = coUser.findByKeyAndFetch(co$(User.class).getFetchProvider().fetchModel(), User.system_users.UNIT_TEST_USER);
        assertNotNull(unitTestUser);
        assertNull(unitTestUser.getBasedOnUser());
        assertTrue(unitTestUser.getProperty(BASED_ON_USER).isValid());
        final User user3 = coUser.findByKeyAndFetch(co$(User.class).getFetchProvider().fetchModel(), "USER3");
        unitTestUser.setBasedOnUser(user3);
        assertFalse(unitTestUser.getProperty(BASED_ON_USER).isValid());
        assertNull(unitTestUser.getBasedOnUser());
        assertEquals(format(UserBaseOnUserValidator.SYSTEM_BUILT_IN_ACCOUNTS_CANNOT_HAVE_BASED_ON_USER, unitTestUser.getKey()), unitTestUser.getProperty(BASED_ON_USER).getFirstFailure().getMessage());
    }

    @Test
    public void only_a_base_user_can_be_set_as_basedOnUser() {
        final User user5 = coUser.findByKeyAndFetch(co$(User.class).getFetchProvider().fetchModel(), "USER5");
        assertTrue(user5.getProperty(BASED_ON_USER).isValid());
        assertEquals("USER1", user5.getBasedOnUser().getKey());
        
        final User nonbaseUser6 = coUser.findByKeyAndFetch(co$(User.class).getFetchProvider().fetchModel(), "USER6");
        user5.setBasedOnUser(nonbaseUser6);
        assertFalse(user5.getProperty(BASED_ON_USER).isValid());
        assertEquals("USER1", user5.getBasedOnUser().getKey());
        assertEquals(format(UserBaseOnUserValidator.ONLY_BASE_USER_CAN_BE_USED_FOR_INHERITANCE, nonbaseUser6.getKey()), user5.getProperty(BASED_ON_USER).getFirstFailure().getMessage());
        
        final User baseUser3 = coUser.findByKeyAndFetch(co$(User.class).getFetchProvider().fetchModel(), "USER3");
        user5.setBasedOnUser(baseUser3);
        final User savedUser5 = save(user5);
        assertTrue(savedUser5.getProperty(BASED_ON_USER).isValid());
        assertEquals("USER3", savedUser5.getBasedOnUser().getKey());
    }

    @Test
    public void once_basedOnUser_is_assigned_to_a_base_user_then_its_property_base_becomes_false() {
        final User user3 = coUser.findByKeyAndFetch(co$(User.class).getFetchProvider().fetchModel(), "USER3");
        assertTrue(user3.isBase());

        final User baseUser4 = coUser.findByKeyAndFetch(co$(User.class).getFetchProvider().fetchModel(), "USER4");
        user3.setBasedOnUser(baseUser4);
        assertFalse(user3.isBase());
    }

    @Test
    public void lockout_of_existing_account_deactivates_user_and_removes_their_secret() {
        coUser.lockoutUser("USER3");
        final User inactiveUser = coUser.findUser("USER3");
        assertFalse(inactiveUser.isActive());
        assertFalse(coUserSecret.findByIdOptional(inactiveUser.getId(), coUserSecret.getFetchProvider().fetchModel()).isPresent());
    }

    @Test
    public void activating_lockout_user_kicks_in_password_rest_procedure() {
        coUser.lockoutUser("USER3");
        final User inactiveUser = coUser.findUser("USER3");
        assertFalse(inactiveUser.isActive());
        assertFalse(coUserSecret.findByIdOptional(inactiveUser.getId()).isPresent());
        
        save(inactiveUser.setActive(true));
        
        final Optional<UserSecret> maybeSecret = coUserSecret.findByIdOptional(inactiveUser.getId());
        assertTrue(maybeSecret.isPresent());
        assertNotNull(maybeSecret.get().getResetUuid());
    }

    @Test
    public void lockout_of_non_existing_account_does_not_throw_exceptions() {
        final String username = "NON-EXISTING-USER-TO-LOCKOUT";
        assertNull(coUser.findUser(username));
        coUser.lockoutUser(username);
    }

    @Test
    public void deactivating_active_users_removes_their_secretes() {
        final User user = coUser.findUser("USER3");
        assertTrue(user.isActive());
        assertTrue(coUserSecret.findByIdOptional(user.getId()).isPresent());
        
        save(user.setActive(false));
        
        assertFalse(coUserSecret.findByIdOptional(user.getId()).isPresent());
    }

    @Test
    public void deactivating_user_with_active_dependencies_fails() {
        final User user3 = coUser.findUser("USER3");
        assertEquals(Integer.valueOf(0), user3.getRefCount());
        
        coUser.save(coUser.findUser("USER5").setBasedOnUser(user3).setActive(true));
        final User user3_1 = coUser.findUser("USER3");
        assertEquals(Integer.valueOf(1), user3_1.getRefCount());
        try {
           coUser.save(user3_1.setActive(false));
           fail("Saving invalid user should have failed.");
        } catch (final Exception ex) {
            assertTrue(ex.getMessage().startsWith("User [USER3] has 1 active dependency."));
        }
    }

    @Test
    public void in_RSO_authentication_mode_new_users_have_property_ssoOnly_equal_false_and_readonly() {
        assertEquals("Invalid initial conditions for this test.", AuthMode.RSO, getInstance(IApplicationSettings.class).authMode());
        
        final User user = co$(User.class).new_();
        assertFalse(user.isSsoOnly());
        assertFalse(user.getProperty(User.SSO_ONLY).isEditable());
    }

    /**
     * One caveat to this test is that if some user was saved in the SSO authentication mode with {@code ssoOnly = true}, then retrieving such user in the RSO mode would still have that value set to {@code true}.
     * But the property itself would be read-only. However, transitioning from SSO to RSO mode for an application is very unlikely. Also, the authentication logic should not be considering {@code ssoOnly} in the RSO mode.
     */
    @Test
    public void in_RSO_authentication_mode_retrieved_for_editing_users_have_property_ssoOnly_equal_false_and_readonly() {
        assertEquals("Invalid initial conditions for this test.", AuthMode.RSO, getInstance(IApplicationSettings.class).authMode());
        
        final User user = co$(User.class).findByKey("USER3");
        assertFalse(user.isSsoOnly());
        assertFalse(user.getProperty(User.SSO_ONLY).isEditable());
    }

    private Date expirationTime() {
        final UniversalConstantsForTesting consts = (UniversalConstantsForTesting) getInstance(IUniversalConstants.class);
        return consts.now().plusMinutes(RESET_UUID_EXPIRATION_IN_MUNUTES).toDate();
    }

    @Override
    protected void populateDomain() {
        super.populateDomain();
        
        // add inactive users with no email addresses
        coUser.save(new_(User.class, "INACTIVE_USER").setBase(true).setActive(false));
        final User user1 = coUser.save(coUser.save(new_(User.class, "USER1").setBase(true)).setEmail("USER1@company.com"));
        coUser.save(new_(User.class, "USER2").setBase(true));

        // add active users with email addresses
        coUser.save(new_(User.class, "USER3").setBase(true).setEmail("USER3@company.com").setActive(true));
        coUser.save(new_(User.class, "USER4").setBase(true).setEmail("USER4@company.com").setActive(true));
        
        // based on users
        coUser.save(new_(User.class, "USER5").setBasedOnUser(user1).setEmail("USER5@company.com"));
        coUser.save(new_(User.class, "USER6").setBasedOnUser(user1).setEmail("USER6@company.com"));
    }

}