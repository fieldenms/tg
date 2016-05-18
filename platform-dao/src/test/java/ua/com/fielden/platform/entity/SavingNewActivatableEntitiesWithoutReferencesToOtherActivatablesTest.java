package ua.com.fielden.platform.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import ua.com.fielden.platform.entity.validation.annotation.ValidationAnnotation;
import ua.com.fielden.platform.persistence.types.EntityBasedOnAbstractPersistentEntity;
import ua.com.fielden.platform.sample.domain.TgCategory;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.test.ioc.UniversalConstantsForTesting;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.utils.IUniversalConstants;

public class SavingNewActivatableEntitiesWithoutReferencesToOtherActivatablesTest extends AbstractDaoTestCase {

    @Test
    public void entity_exists_validator_should_be_assigned_automatically() {
        final TgCategory newCat = new_(TgCategory.class, "NEW");
        assertNotNull(newCat);
        assertNotNull(newCat.getProperty("parent").getValidators().get(ValidationAnnotation.ENTITY_EXISTS));
    }

    @Test
    public void new_activatable_entity_should_be_successfully_persisted() {
        final TgCategory newCat = save(new_(TgCategory.class, "NEW").setActive(true));
        assertEquals(Integer.valueOf(0), newCat.getRefCount());
    }
    
    @Test
    public void saving_new_activatable_entity_assigns_created_by_group_of_properties() {
        final IUserProvider up = getInstance(IUserProvider.class);
        final IUniversalConstants constants = getInstance(IUniversalConstants.class);
        
        final TgCategory savedEntity = save(new_(TgCategory.class, "NEW").setActive(true));
        
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
    public void saving_modified_activatable_entity_assigns_last_modified_group_of_properties() {
    
        final IUserProvider up = getInstance(IUserProvider.class);
        final UniversalConstantsForTesting constants = (UniversalConstantsForTesting) getInstance(IUniversalConstants.class);
        constants.setNow(dateTime("2016-05-16 16:36:57"));
        
        final TgCategory newlySaved = save(new_(TgCategory.class, "NEW").setActive(true));
        
        // move to the future and change the current user
        constants.setNow(dateTime("2016-05-17 13:36:57"));
        up.setUser(co(User.class).findByKey("USER_1"));
        
        // perform entity modification and saving
        final TgCategory savedEntity = save(newlySaved.setKey("UPDATED"));
        
        assertNotNull(savedEntity.getCreatedBy());
        assertNotNull(savedEntity.getCreatedDate());
        assertNotNull(savedEntity.getCreatedTransactionGuid());
        
        assertNotNull(savedEntity.getLastUpdatedBy());
        assertEquals(up.getUser(), savedEntity.getLastUpdatedBy());
        assertNotNull(savedEntity.getLastUpdatedDate());
        assertEquals(constants.now().toDate(), savedEntity.getLastUpdatedDate());
        assertNotNull(savedEntity.getLastUpdatedTransactionGuid());
    }

    @Override
    protected void populateDomain() {
        super.populateDomain();
        
        final UniversalConstantsForTesting constants = (UniversalConstantsForTesting) getInstance(IUniversalConstants.class);
        constants.setNow(dateTime("2016-05-17 16:36:57"));
        
        save(new_(User.class, "USER_1").setBase(true));
    }

}
