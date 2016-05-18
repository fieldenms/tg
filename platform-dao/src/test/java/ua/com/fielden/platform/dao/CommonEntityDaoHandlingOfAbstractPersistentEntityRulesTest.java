package ua.com.fielden.platform.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import ua.com.fielden.platform.entity.AbstractPersistentEntity;
import ua.com.fielden.platform.persistence.types.EntityBasedOnAbstractPersistentEntity;
import ua.com.fielden.platform.persistence.types.EntityBasedOnAbstractPersistentEntity2;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.test.ioc.UniversalConstantsForTesting;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.utils.IUniversalConstants;

/**
 * Validates the assignment of basic persistent entity properties.
 * 
 * @author TG Team
 *
 */
public class CommonEntityDaoHandlingOfAbstractPersistentEntityRulesTest extends AbstractDaoTestCase {

    @Test
    public void saving_new_entity_assigns_created_by_group_of_properties() {
        final IUserProvider up = getInstance(IUserProvider.class);
        final IUniversalConstants constants = getInstance(IUniversalConstants.class);
        
        final EntityBasedOnAbstractPersistentEntity entity = new_(EntityBasedOnAbstractPersistentEntity.class, "VALUE_1");
        final EntityBasedOnAbstractPersistentEntity savedEntity = save(entity);
        
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
    public void saving_modified_entity_assigns_last_modified_group_of_properties() {
        final IUserProvider up = getInstance(IUserProvider.class);
        final UniversalConstantsForTesting constants = (UniversalConstantsForTesting) getInstance(IUniversalConstants.class);
        constants.setNow(dateTime("2016-05-16 16:36:57"));
        
        final EntityBasedOnAbstractPersistentEntity newlySaved = save(new_(EntityBasedOnAbstractPersistentEntity.class, "VALUE_1"));
        
        // move to the future and change the current user
        constants.setNow(dateTime("2016-05-17 13:36:57"));
        up.setUser(co(User.class).findByKey("USER_1"));
        
        // perform entity modification and saving
        final EntityBasedOnAbstractPersistentEntity savedEntity = save(newlySaved.setKey("VALUE_1_"));
        
        assertNotNull(savedEntity.getCreatedBy());
        assertNotNull(savedEntity.getCreatedDate());
        assertNotNull(savedEntity.getCreatedTransactionGuid());
        
        assertNotNull(savedEntity.getLastUpdatedBy());
        assertEquals(up.getUser(), savedEntity.getLastUpdatedBy());
        assertNotNull(savedEntity.getLastUpdatedDate());
        assertEquals(constants.now().toDate(), savedEntity.getLastUpdatedDate());
        assertNotNull(savedEntity.getLastUpdatedTransactionGuid());
    }
    
    @Test
    public void saving_multiple_new_entities_in_the_same_transcation_binds_them_with_by_the_same_GUID() {
        final List<EntityBasedOnAbstractPersistentEntity> entities = new ArrayList<>();
        entities.add(new_(EntityBasedOnAbstractPersistentEntity.class, "VALUE_1"));
        entities.add(new_(EntityBasedOnAbstractPersistentEntity.class, "VALUE_2"));
        entities.add(new_(EntityBasedOnAbstractPersistentEntity.class, "VALUE_3"));
        
        final EntityBasedOnAbstractPersistentEntityDao co = co(EntityBasedOnAbstractPersistentEntity.class);
        List<EntityBasedOnAbstractPersistentEntity> savedEntities = co.saveInSingleTransaction(entities);
        
        assertEquals(3, savedEntities.stream().filter(entity -> entity.getCreatedTransactionGuid() != null).count());
        assertEquals(1, savedEntities.stream().map(entity -> entity.getCreatedTransactionGuid()).distinct().count());
    }

    @Test
    public void saving_multiple_modified_entities_in_the_same_transcation_binds_them_with_by_the_same_GUID() {
        final List<EntityBasedOnAbstractPersistentEntity> entities = new ArrayList<>();
        entities.add(save(new_(EntityBasedOnAbstractPersistentEntity.class, "VALUE_1")).setKey("VALUE_1_"));
        entities.add(save(new_(EntityBasedOnAbstractPersistentEntity.class, "VALUE_2")).setKey("VALUE_2_"));
        entities.add(save(new_(EntityBasedOnAbstractPersistentEntity.class, "VALUE_3")).setKey("VALUE_3_"));
        
        final EntityBasedOnAbstractPersistentEntityDao co = co(EntityBasedOnAbstractPersistentEntity.class);
        final List<EntityBasedOnAbstractPersistentEntity> savedEntities = co.saveInSingleTransaction(entities);
        
        assertEquals(3, savedEntities.stream().filter(entity -> entity.getCreatedTransactionGuid() != null).count());
        assertEquals(3, savedEntities.stream().filter(entity -> entity.getLastUpdatedTransactionGuid() != null).count());
        
        assertEquals(3, savedEntities.stream().map(entity -> entity.getCreatedTransactionGuid()).distinct().count());
        assertEquals(1, savedEntities.stream().map(entity -> entity.getLastUpdatedTransactionGuid()).distinct().count());
    }
    
    @Test
    public void saving_a_mix_of_new_and_modified_entities_in_the_same_transcation_binds_them_by_the_same_GUID() {
        final List<EntityBasedOnAbstractPersistentEntity> entities = new ArrayList<>();
        entities.add(save(new_(EntityBasedOnAbstractPersistentEntity.class, "VALUE_1")).setKey("VALUE_1_")); // modified
        entities.add(new_(EntityBasedOnAbstractPersistentEntity.class, "VALUE_2")); // new
        entities.add(save(new_(EntityBasedOnAbstractPersistentEntity.class, "VALUE_3")).setKey("VALUE_3_")); // modified
        
        final EntityBasedOnAbstractPersistentEntityDao co = co(EntityBasedOnAbstractPersistentEntity.class);
        final List<EntityBasedOnAbstractPersistentEntity> savedEntities = co.saveInSingleTransaction(entities);
        
        assertEquals("2 out of 3 entities are updated", 
                     2, savedEntities.stream().filter(entity -> entity.getLastUpdatedTransactionGuid() != null).count());
        
        assertEquals("All three entities are inserted in different transactions.", 
                     3, savedEntities.stream().map(entity -> entity.getCreatedTransactionGuid()).distinct().count());
        assertEquals("2 out of 3 entities are updated in the same transaction, and the remaining 1 is not updated", 
                     2, savedEntities.stream().map(entity -> entity.getLastUpdatedTransactionGuid()).distinct().count());
        
        assertTrue("The insert transaction for the 2nd entity is the same as the update transaction for the 1st and 3rd entity",
                   savedEntities.get(0).getLastUpdatedTransactionGuid().equals(savedEntities.get(1).getCreatedTransactionGuid()) &&
                   savedEntities.get(2).getLastUpdatedTransactionGuid().equals(savedEntities.get(1).getCreatedTransactionGuid()));
    }
    
    @Test
    public void saving_entities_of_different_types_in_the_same_transaction_binds_them_by_the_same_GUID() {
        final List<EntityBasedOnAbstractPersistentEntity> entities1 = new ArrayList<>();
        entities1.add(save(new_(EntityBasedOnAbstractPersistentEntity.class, "VALUE_1")).setKey("VALUE_1_")); // modified
        entities1.add(new_(EntityBasedOnAbstractPersistentEntity.class, "VALUE_2")); // new

        final List<EntityBasedOnAbstractPersistentEntity2> entities2 = new ArrayList<>();
        entities2.add(save(new_(EntityBasedOnAbstractPersistentEntity2.class, "VALUE_1")).setKey("VALUE_1_")); // modified
        entities2.add(new_(EntityBasedOnAbstractPersistentEntity2.class, "VALUE_2")); // new

        
        final EntityBasedOnAbstractPersistentEntityDao co = co(EntityBasedOnAbstractPersistentEntity.class);
        final List<AbstractPersistentEntity<?>> savedEntities = co.nestedSaveWithDifferentCompanion(entities1, entities2);
        
        assertEquals("2 out of 4 entities of different types are updated", 
                     2, savedEntities.stream().filter(entity -> entity.getLastUpdatedTransactionGuid() != null).count());
        
        assertEquals("2 entities of different types are inserted in the same transaction, 2 others -- in separate transactions", 
                     3, savedEntities.stream().map(entity -> entity.getCreatedTransactionGuid()).distinct().count());
        assertEquals("Only 2 out of 4 entities of different types are updated in the same transaction, the other 2 are inserted", 
                     2, savedEntities.stream().map(entity -> entity.getLastUpdatedTransactionGuid()).distinct().count());
        
        assertTrue("The insert transaction for the 2nd and 4th entity of different types is the same as the update transaction for the 1st and 3rd entity, also of different types.",
                   savedEntities.get(1/*2nd*/).getCreatedTransactionGuid().equals(savedEntities.get(0/*1st*/).getLastUpdatedTransactionGuid()) &&
                   savedEntities.get(1/*2nd*/).getCreatedTransactionGuid().equals(savedEntities.get(2/*3rd*/).getLastUpdatedTransactionGuid()) &&
                   savedEntities.get(3/*4th*/).getCreatedTransactionGuid().equals(savedEntities.get(0/*1st*/).getLastUpdatedTransactionGuid()) &&
                   savedEntities.get(3/*4th*/).getCreatedTransactionGuid().equals(savedEntities.get(2/*3rd*/).getLastUpdatedTransactionGuid()));
    }
    
    @Override
    protected void populateDomain() {
        super.populateDomain();
        
        final UniversalConstantsForTesting constants = (UniversalConstantsForTesting) getInstance(IUniversalConstants.class);
        constants.setNow(dateTime("2016-05-17 16:36:57"));
        
        save(new_(User.class, "USER_1").setBase(true));
    }

}