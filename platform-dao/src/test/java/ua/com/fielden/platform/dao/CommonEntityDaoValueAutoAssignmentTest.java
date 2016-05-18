package ua.com.fielden.platform.dao;

import static org.junit.Assert.*;

import org.junit.Test;

import ua.com.fielden.platform.persistence.types.EntityWithAutoAssignableProperties;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.test.ioc.UniversalConstantsForTesting;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.utils.IUniversalConstants;

public class CommonEntityDaoValueAutoAssignmentTest extends AbstractDaoTestCase {

    @Test
    public void auto_assignment_of_system_user_is_supported() {
        final EntityWithAutoAssignableProperties entity = new_(EntityWithAutoAssignableProperties.class, "VALUE_1");
        assertNull(entity.getUser());
        final EntityWithAutoAssignableProperties savedEntity = save(entity);
        assertNotNull(savedEntity.getUser());
    }

    @Test
    public void auto_assignment_skips_already_assigned_properties() {
        final EntityWithAutoAssignableProperties entity = new_(EntityWithAutoAssignableProperties.class, "VALUE_1");
        final User user = co(User.class).findByKey("USER_1");
        entity.setUser(user);
        final EntityWithAutoAssignableProperties savedEntity = save(entity);
        assertEquals(user, savedEntity.getUser());
    }
    
    @Override
    protected void populateDomain() {
        super.populateDomain();
        
        final UniversalConstantsForTesting constants = (UniversalConstantsForTesting) getInstance(IUniversalConstants.class);
        constants.setNow(dateTime("2016-05-17 16:36:57"));
        
        save(new_(User.class, "USER_1").setBase(true).setEmail("USER1@unit-test.software").setActive(true));
    }

}