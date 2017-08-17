package ua.com.fielden.platform.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;

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
        up.setUsername(username, co$(User.class));

        // Cat7 will be referenced by a newly created Cat6, and should not change its last-updated information even though its recCount is goint to be changed
        final TgCategory originalCat7 = co$(TgCategory.class).findByKey(cat7);
        assertNull(originalCat7.getLastUpdatedBy());
        assertNull(originalCat7.getLastUpdatedDate());
        assertEquals(Integer.valueOf(0), originalCat7.getRefCount());
        
        final TgCategory cat6 = save(new_(TgCategory.class, "Cat6").setActive(true).setParent(originalCat7));
        
        final TgCategory updatedCat7 = cat6.getParent();
        assertTrue(updatedCat7.getLastUpdatedBy() == null);
        assertTrue(updatedCat7.getLastUpdatedDate() == null);
        assertEquals(Integer.valueOf(1), updatedCat7.getRefCount());
    }

    @Test
    public void saving_new_activatable_entity_does_not_lead_to_changes_of_createdBy_user_refCount() {
        final IUserProvider up = getInstance(IUserProvider.class);
        up.setUsername(username, co$(User.class));
        final User currUser = up.getUser();
        
        final Integer originalUserRefCount = currUser.getRefCount();
        
        final TgCategory cat6 = save(new_(TgCategory.class, "Cat6").setActive(true));
        assertEquals(currUser, cat6.getCreatedBy());
        assertEquals(originalUserRefCount, cat6.getCreatedBy().getRefCount());
    }
    
    @Test
    public void updating_activatable_entity_does_not_lead_to_changes_of_lastUpdatedBy_user_refCount() {
        final IUserProvider up = getInstance(IUserProvider.class);
        up.setUsername(username, co$(User.class));
        final User currUser = up.getUser();
        
        final Integer originalUserRefCount = currUser.getRefCount();
        assertEquals(Integer.valueOf(0), originalUserRefCount);
        
        save(co$(TgCategory.class).findByKey(cat7).setDesc("some new desc"));
        final TgCategory updatedCat7 = co$(TgCategory.class).findByKeyAndFetch(fetchAll(TgCategory.class), cat7); 
        
        assertEquals(currUser, updatedCat7.getLastUpdatedBy());
        assertEquals(originalUserRefCount, updatedCat7.getLastUpdatedBy().getRefCount());
    }
    
    @Test
    public void deactivating_activatable_entity_does_not_lead_to_changes_of_lastUpdatedBy_and_createdBy_user_refCount_values() {
        final IUserProvider up = getInstance(IUserProvider.class);
        up.setUsername(username, co$(User.class));
        final User currUser = up.getUser();
        
        final TgCategory cat7Orig = co$(TgCategory.class).findByKeyAndFetch(fetchAll(TgCategory.class), cat7);
        assertTrue(cat7Orig.isActive());
        
        final Integer origCreatedByUserRefCount = cat7Orig.getCreatedBy().getRefCount();
        assertEquals(Integer.valueOf(0), origCreatedByUserRefCount); 
        final Integer origUpdatedByUserRefCount = currUser.getRefCount();
        assertEquals(Integer.valueOf(0), origUpdatedByUserRefCount);
        
        save(cat7Orig.setActive(false));
        
        final TgCategory updatedCat7 = co$(TgCategory.class).findByKeyAndFetch(fetchAll(TgCategory.class), cat7); 
        
        assertEquals(currUser, updatedCat7.getLastUpdatedBy());
        assertEquals(Integer.valueOf(0), updatedCat7.getLastUpdatedBy().getRefCount());
        assertEquals(cat7Orig.getCreatedBy(), updatedCat7.getCreatedBy());
        assertEquals(Integer.valueOf(0), updatedCat7.getCreatedBy().getRefCount());
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