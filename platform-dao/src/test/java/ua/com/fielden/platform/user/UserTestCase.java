package ua.com.fielden.platform.user;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ua.com.fielden.platform.entity.annotation.Unique;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.validation.UniqueValidator;
import ua.com.fielden.platform.property.validator.StringValidator;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

/**
 * A test case to cover basic user validations.
 *
 * @author TG Team
 *
 */
public class UserTestCase extends AbstractDaoTestCase {

    private final IUser coUser = ao(User.class);


    @Test
    public void propety_email_in_user_defined_as_unique() {
        final User user1 = coUser.findByKey("USER-1");
        final MetaProperty<String> emailProp = user1.getProperty("email");
        assertTrue(emailProp.getValidationAnnotations().stream().filter(a -> a instanceof Unique).count() > 0);
    }
    
    @Test
    public void users_have_unique_email_addresses() {
        final User user1 = coUser.findByKey("USER-1");
        user1.setEmail("user1@company.com");
        assertNotNull(coUser.save(user1).getEmail());
        
        final User user2 = coUser.findByKey("USER-2");
        user2.setEmail("user1@company.com");
        assertFalse(user2.isValid().isSuccessful());
        assertFalse(user2.getProperty("email").isValid());
        assertEquals(format(UniqueValidator.validationErrorTemplate, "user1@company.com", "email", User.class.getName()), user2.getProperty("email").getFirstFailure().getMessage());
    }

    @Test
    public void multiple_users_may_have_their_email_addresses_set_to_null() {
        final User user3 = coUser.findByKey("USER-3").setEmail(null);
        assertTrue(user3.isValid().isSuccessful());
        assertNull(coUser.save(user3).getEmail());
        
        final User user4 = coUser.findByKey("USER-4").setEmail(null);
        assertTrue(user4.isValid().isSuccessful());
        assertNull(coUser.save(user3).getEmail());
    }

    @Test
    public void email_address_value_has_bacis_validation() {
        
        final User user1 = coUser.findByKey("USER-1");
        
        user1.setEmail("user1@");
        assertFalse(user1.getProperty("email").isValid());
        assertEquals(format(StringValidator.validationErrorTemplate, "user1@", "email", "User") ,user1.getProperty("email").getFirstFailure().getMessage());
        
        user1.setEmail("@company.com");
        assertFalse(user1.getProperty("email").isValid());
        assertEquals(format(StringValidator.validationErrorTemplate, "@company.com", "email", "User") ,user1.getProperty("email").getFirstFailure().getMessage());

        user1.setEmail("user1@company . com");
        assertFalse(user1.getProperty("email").isValid());
        assertEquals(format(StringValidator.validationErrorTemplate, "user1@company . com", "email", "User") ,user1.getProperty("email").getFirstFailure().getMessage());
        
        user1.setEmail("user1@company.com");
        assertTrue(user1.getProperty("email").isValid());
    }

    
    @Override
    protected void populateDomain() {
        super.populateDomain(); // creates the default current user TEST

        // add users without email
        coUser.save(new_(User.class, "USER-1").setBase(true));
        coUser.save(new_(User.class, "USER-2").setBase(true));

        // add users with email
        coUser.save(new_(User.class, "USER-3").setBase(true).setEmail("user3@company.com"));
        coUser.save(new_(User.class, "USER-4").setBase(true).setEmail("user4@company.com"));
        
        // ensure that TEST is the current user
        final IUserProvider up = getInstance(IUserProvider.class);
        up.setUsername("TEST", getInstance(IUser.class));
    }

}