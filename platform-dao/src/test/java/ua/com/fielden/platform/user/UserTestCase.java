package ua.com.fielden.platform.user;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;

import java.util.Optional;

import org.junit.Test;

import ua.com.fielden.platform.entity.annotation.Unique;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.validation.UniqueValidator;
import ua.com.fielden.platform.property.validator.EmailValidator;
import ua.com.fielden.platform.property.validator.StringValidator;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.test.ioc.UniversalConstantsForTesting;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.utils.IUniversalConstants;

/**
 * A test case to cover basic user validations.
 *
 * @author TG Team
 *
 */
public class UserTestCase extends AbstractDaoTestCase {

    private final IUser coUser = ao(User.class);

    @Test
    public void username_does_not_permit_password_reset_UUID_separator() {
        final User user = new_(User.class);
        final String value = format("USER%s1", User.passwordResetUuidSeperator);
        user.setKey(value);
        assertFalse(user.getProperty(KEY).isValid());
        assertEquals(format(StringValidator.validationErrorTemplate, value, KEY, User.class.getSimpleName()), user.getProperty("key").getFirstFailure().getMessage());
    }

    @Test
    public void propety_email_in_user_defined_as_unique() {
        final User user1 = coUser.findByKey("USER1");
        final MetaProperty<String> emailProp = user1.getProperty("email");
        assertTrue(emailProp.getValidationAnnotations().stream().filter(a -> a instanceof Unique).count() > 0);
    }
    
    @Test
    public void users_have_unique_email_addresses() {
        final User user1 = coUser.findByKey("USER1");
        user1.setEmail("user1@company.com");
        assertNotNull(coUser.save(user1).getEmail());
        
        final User user2 = coUser.findByKey("USER2");
        user2.setEmail("user1@company.com");
        assertFalse(user2.isValid().isSuccessful());
        assertFalse(user2.getProperty("email").isValid());
        assertEquals(format(UniqueValidator.validationErrorTemplate, "user1@company.com", "email", User.class.getName()), user2.getProperty("email").getFirstFailure().getMessage());
    }

    @Test
    public void multiple_users_may_have_their_email_addresses_set_to_null() {
        final User user3 = coUser.findByKey("USER3").setEmail(null);
        assertTrue(user3.isValid().isSuccessful());
        assertNull(coUser.save(user3).getEmail());
        
        final User user4 = coUser.findByKey("USER4").setEmail(null);
        assertTrue(user4.isValid().isSuccessful());
        assertNull(coUser.save(user3).getEmail());
    }

    @Test
    public void email_address_value_has_bacis_validation() {
        
        final User user1 = coUser.findByKey("USER1");
        
        user1.setEmail("user1@");
        assertFalse(user1.getProperty("email").isValid());
        assertEquals(format(EmailValidator.validationErrorTemplate, "user1@", "email", "User") ,user1.getProperty("email").getFirstFailure().getMessage());
        
        user1.setEmail("@company.com");
        assertFalse(user1.getProperty("email").isValid());
        assertEquals(format(EmailValidator.validationErrorTemplate, "@company.com", "email", "User") ,user1.getProperty("email").getFirstFailure().getMessage());

        user1.setEmail("user1@company . com");
        assertFalse(user1.getProperty("email").isValid());
        assertEquals(format(EmailValidator.validationErrorTemplate, "user1@company . com", "email", "User") ,user1.getProperty("email").getFirstFailure().getMessage());
        
        user1.setEmail("user1@company@la.com");
        assertFalse(user1.getProperty("email").isValid());
        assertEquals(format(EmailValidator.validationErrorTemplate, "user1@company@la.com", "email", "User") ,user1.getProperty("email").getFirstFailure().getMessage());
        
        user1.setEmail("user1@company.com");
        assertTrue(user1.getProperty("email").isValid());
    }

    @Test
    public void weak_user_passwords_are_identified() {
        assertFalse(coUser.isPasswordStrong("passW0rd?"));
        assertFalse(coUser.isPasswordStrong("today123"));
        assertFalse(coUser.isPasswordStrong("aaaaa aaaa"));
    }
    
    @Test
    public void strong_user_passwords_are_identified() {
        assertTrue(coUser.isPasswordStrong("today123A"));
        assertTrue(coUser.isPasswordStrong("My voice is my password. Verify!"));
        assertTrue(coUser.isPasswordStrong("Sentances are strong and memorable passwords"));
    }
    
    @Test 
    public void user_identified_by_username_can_have_correct_password_reset_UUID_generated() {
        final Optional<User> user = coUser.assignPasswordResetUuid("USER3");
        assertTrue(user.isPresent());
        assertNotNull(user.get().getResetUuid());
    }
    
    @Test 
    public void locating_user_by_name_during_password_reset_UUID_generation_is_case_insensitive() {
        final Optional<User> user = coUser.assignPasswordResetUuid("user3");
        assertTrue(user.isPresent());
        assertNotNull(user.get().getResetUuid());
    }
    
    @Test
    public void user_identified_by_email_can_have_correct_password_reset_UUID_generated() {
        final Optional<User> user = coUser.assignPasswordResetUuid("user3@company.com");
        assertTrue(user.isPresent());
        assertNotNull(user.get().getResetUuid());
    }
    
    @Test
    public void password_reset_UUID_assignment_returns_empty_value_for_unidentified_user() {
        final Optional<User> user = coUser.assignPasswordResetUuid("invalid@company.com");
        assertFalse(user.isPresent());
    }
    
    @Test
    public void password_reset_UUID_structure_is_valid() {
        final UniversalConstantsForTesting consts = (UniversalConstantsForTesting) getInstance(IUniversalConstants.class);
        consts.setNow(dateTime("2016-04-05 14:00:00"));
        
        final Optional<User> user = coUser.assignPasswordResetUuid("USER3");
        assertTrue(user.isPresent());
        
        final String[] uuidParts = user.get().getResetUuid().split(User.passwordResetUuidSeperator);
        assertEquals(3, uuidParts.length);
        assertEquals(user.get().getKey(), uuidParts[0]);
        assertEquals(consts.now().plusHours(24).getMillis(), Long.valueOf(uuidParts[2]).longValue());
    }

    @Test
    public void user_can_be_found_by_password_reset_UUID() {
        final UniversalConstantsForTesting consts = (UniversalConstantsForTesting) getInstance(IUniversalConstants.class);
        consts.setNow(dateTime("2016-04-05 14:00:00"));
        
        final String uuid = coUser.assignPasswordResetUuid("USER3").get().getResetUuid();
        final Optional<User> user = coUser.findUserByResetUuid(uuid);
        assertTrue(user.isPresent());
        assertEquals(uuid, user.get().getResetUuid());
    }

    @Test
    public void resetting_user_password_removes_reset_UUID() {
        final User user = coUser.assignPasswordResetUuid("USER3").get();
        final User updatedUser = coUser.resetPasswd(user, "new and strong password!");
        assertNull(updatedUser.getResetUuid());
    }
    
    @Test
    public void expired_password_reset_UUID_is_invalid() {
        final UniversalConstantsForTesting consts = (UniversalConstantsForTesting) getInstance(IUniversalConstants.class);
        final String now = "2016-04-05 14:00:00";
        consts.setNow(dateTime(now));
        
        final String uuid = coUser.assignPasswordResetUuid("USER3").get().getResetUuid();
        assertTrue(coUser.isPasswordResetUuidValid(uuid));
        // move the time forward
        consts.setNow(dateTime(now).plusDays(2));
        assertFalse(coUser.isPasswordResetUuidValid(uuid));
    }

    @Test
    public void password_reset_UUID_with_no_associated_user_is_invalid() {
        final UniversalConstantsForTesting consts = (UniversalConstantsForTesting) getInstance(IUniversalConstants.class);
        final String now = "2016-04-05 14:00:00";
        consts.setNow(dateTime(now));
        
        final User user = coUser.assignPasswordResetUuid("USER3").get();
        final String uuid = user.getResetUuid();
        coUser.save(user.setResetUuid(null));
        
        assertFalse(coUser.isPasswordResetUuidValid(uuid));
    }

    @Test
    public void incorrect_password_reset_UUID_associated_with_user_is_recognized_as_invalid() {
        final UniversalConstantsForTesting consts = (UniversalConstantsForTesting) getInstance(IUniversalConstants.class);
        final String now = "2016-04-05 14:00:00";
        consts.setNow(dateTime(now));

        final User user = coUser.findByKeyAndFetch(fetchAll(User.class), "USER3");
        final String uuid = "incorrect password reset UUID";
        coUser.save(user.setResetUuid(uuid));
        
        final Optional<User> foundUser = coUser.findUserByResetUuid(uuid);
        assertFalse(foundUser.isPresent());
        assertFalse(coUser.isPasswordResetUuidValid(uuid));
    }

    
    @Override
    protected void populateDomain() {
        super.populateDomain(); // creates the default current user TEST

        // add users without email
        coUser.save(new_(User.class, "USER1").setBase(true));
        coUser.save(new_(User.class, "USER2").setBase(true));

        // add users with email
        coUser.save(new_(User.class, "USER3").setBase(true).setEmail("user3@company.com"));
        coUser.save(new_(User.class, "USER4").setBase(true).setEmail("user4@company.com"));
        
        // ensure that TEST is the current user
        final IUserProvider up = getInstance(IUserProvider.class);
        up.setUsername("TEST", getInstance(IUser.class));
    }

}