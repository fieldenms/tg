package ua.com.fielden.platform.dao;

import static org.junit.Assert.*;

import java.util.Date;

import org.junit.Test;

import ua.com.fielden.platform.sample.domain.TgCategory;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.test.ioc.UniversalConstantsForTesting;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.utils.IUniversalConstants;

/**
 * Validates the assignment of basic persistent entity properties for activatable entities
 * 
 * @author TG Team
 *
 */
public class HandlingOfAbstractPersistentEntityRulesInApplicationToActivatableEntitiesTest extends AbstractDaoTestCase {
    private static final String username = "USER_1";
    private static final String cat7 = "Cat7";

    @Test
    public void lastUpdatedBy_does_not_change_if_the_only_changed_property_for_an_activatable_entity_is_refCount() {
        final IUserProvider up = getInstance(IUserProvider.class);
        up.setUsername(username, co(User.class));

        // Cat7 will be referenced by a newly created Cat6, and should not change its last-updated information even though its recCount is goint to be changed
        final TgCategory originalCat7 = co(TgCategory.class).findByKey(cat7);
        assertNull(originalCat7.getLastUpdatedBy());
        assertNull(originalCat7.getLastUpdatedDate());
        assertEquals(Integer.valueOf(0), originalCat7.getRefCount());
        
        final TgCategory cat6 = save(new_(TgCategory.class, "Cat6").setActive(true).setParent(originalCat7));
        
        final TgCategory updatedCat7 = cat6.getParent();
        assertTrue(updatedCat7.getLastUpdatedBy() == null);
        assertTrue(updatedCat7.getLastUpdatedDate() == null);
        assertEquals(Integer.valueOf(1), updatedCat7.getRefCount());
    }

    
    @Override
    protected void populateDomain() {
        super.populateDomain();

        final UniversalConstantsForTesting constants = (UniversalConstantsForTesting) getInstance(IUniversalConstants.class);
        constants.setNow(dateTime("2016-05-17 16:36:57"));

        save(new_(User.class, username).setBase(true).setEmail("user_1@company.com").setActive(true));
        
        save(new_(TgCategory.class, cat7).setActive(true));
    }

}