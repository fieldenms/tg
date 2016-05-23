package ua.com.fielden.platform.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.security.user.UserAndRoleAssociation;
import ua.com.fielden.platform.security.user.UserRole;
import ua.com.fielden.platform.test.ioc.UniversalConstantsForTesting;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.utils.IUniversalConstants;

/**
 * A test case for the {@link UserRole} persistence and retrieval.
 * 
 * @author TG Team
 * 
 */
public class UserRoleTest extends AbstractDaoTestCase {

    @Test
    public void saving_new_active_user_role_assigns_created_by_group_of_properties() {
        final IUserProvider up = getInstance(IUserProvider.class);
        final IUniversalConstants constants = getInstance(IUniversalConstants.class);
        final UserRole savedEntity = save(new_(UserRole.class, "NEW ROLE", "desc").setActive(true));

        assertNotNull(savedEntity.getCreatedBy());
        assertEquals(up.getUser(), savedEntity.getCreatedBy());
        assertNotNull(savedEntity.getCreatedDate());
        assertEquals(constants.now().toDate(), savedEntity.getCreatedDate());
        assertNotNull(savedEntity.getCreatedTransactionGuid());

        assertNull(savedEntity.getLastUpdatedBy());
        assertNull(savedEntity.getLastUpdatedDate());
        assertNull(savedEntity.getLastUpdatedTransactionGuid());
    }

    @Test
    public void saving_modified_active_user_role_assigns_last_modified_group_of_properties() {

        final IUserProvider up = getInstance(IUserProvider.class);
        final UniversalConstantsForTesting constants = (UniversalConstantsForTesting) getInstance(IUniversalConstants.class);
        constants.setNow(dateTime("2016-05-16 16:36:57"));

        final UserRole newlySaved = save(new_(UserRole.class, "NEW ROLE").setActive(true));

        // move to the future and change the current user
        constants.setNow(dateTime("2016-05-17 13:36:57"));
        final User currentUser = up.getUser();
        up.setUser(co(User.class).findByKey("USER_1"));

        try {
            // perform entity modification and saving
            final UserRole savedEntity = save(newlySaved.setKey("UPDATED ROLE"));

            assertNotNull(savedEntity.getCreatedBy());
            assertNotNull(savedEntity.getCreatedDate());
            assertNotNull(savedEntity.getCreatedTransactionGuid());

            assertNotNull(savedEntity.getLastUpdatedBy());
            assertEquals(up.getUser(), savedEntity.getLastUpdatedBy());
            assertNotNull(savedEntity.getLastUpdatedDate());
            assertEquals(constants.now().toDate(), savedEntity.getLastUpdatedDate());
            assertNotNull(savedEntity.getLastUpdatedTransactionGuid());
        } finally {
            up.setUser(currentUser);
        }
    }

    @Override
    protected void populateDomain() {
        super.populateDomain();

        final UniversalConstantsForTesting constants = (UniversalConstantsForTesting) getInstance(IUniversalConstants.class);
        constants.setNow(dateTime("2016-05-17 16:36:57"));

        final User user1 = save(new_(User.class, "USER_1").setBase(true));

        // associate the test role with user1
        final UserRole admin = co(UserRole.class).findByKey(UNIT_TEST_ROLE);
        save(new_composite(UserAndRoleAssociation.class, user1, admin));
    }
}