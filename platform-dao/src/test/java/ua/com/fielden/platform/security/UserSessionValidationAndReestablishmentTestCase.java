package ua.com.fielden.platform.security;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import ua.com.fielden.platform.sample.domain.TgPerson;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.test.ioc.UniversalConstantsForTesting;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.utils.IUniversalConstants;

/**
 * A test case to cover user session management functionality.
 *
 * @author TG Team
 *
 */
public class UserSessionValidationAndReestablishmentTestCase extends AbstractDaoTestCase {

    @Test
    public void should_be_able_to_find_person_USER2() {
        final TgPerson person = ao(TgPerson.class).findByKey("USER2");
        assertNotNull(person);
    }

    /**
     * Domain state population method.
     * <p>
     * <b>IMPORTANT:
     * </p>
     * this method executes only once for a Test Case. At the same time, new instances of a Test Case are create for each test method. Thus, this method should not be used for
     * initialisation of the Test Case state other than the persisted domain state.
     */
    @Override
    protected void populateDomain() {
        super.populateDomain();
        // Here is how the Test Case universal constants can be set.
        // In this case the notion of now is overridden, which makes possible to have it system-type invariant.
        final UniversalConstantsForTesting constants = (UniversalConstantsForTesting) getInstance(IUniversalConstants.class);
        constants.setNow(dateTime("2011-11-01 13:01:00"));

        // Here is the three Person objects are persisted using the the inherited from TG testing framework methods.
        final TgPerson person1 = save(new_(TgPerson.class, "USER1").setUsername("USER1").setBase(true));
        final User user = ao(User.class).findById(person1.getId());
        save(new_(TgPerson.class, "USER2").setUsername("USER2").setBasedOnUser(user));
        save(new_(TgPerson.class, "USER3").setUsername("USER3").setBase(true));
    }

}