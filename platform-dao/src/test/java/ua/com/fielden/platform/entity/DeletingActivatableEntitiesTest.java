package ua.com.fielden.platform.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;

import java.util.Random;

import org.junit.Ignore;
import org.junit.Test;

import ua.com.fielden.platform.sample.domain.TgCategory;
import ua.com.fielden.platform.sample.domain.TgSystem;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

public class DeletingActivatableEntitiesTest extends AbstractDaoTestCase {

    @Test
    public void deletion_of_active_entity_that_references_the_same_active_entity_in_two_properties_decrements_its_refCount_by_two() {
        final TgCategory cat = save(new_(TgCategory.class, "NEW_CAT").setActive(true));
        
        final TgSystem sys = save(new_(TgSystem.class, "NEW_SYS").setActive(true).setFirstCategory(cat).setSecondCategory(cat));
        assertEquals(Integer.valueOf(2), co(TgCategory.class).findByKey(cat.getKey()).getRefCount());
        
        co(TgSystem.class).delete(sys);
        
        assertFalse(co(TgSystem.class).findByIdOptional(sys.getId()).isPresent());
        assertEquals(Integer.valueOf(0), co(TgCategory.class).findByKey(cat.getKey()).getRefCount());
    }
    
    @Test
    public void deletion_of_active_entity_that_references_two_different_active_entities_decrements_their_refCount_by_one() {
        final TgCategory cat1 = save(new_(TgCategory.class, "NEW_CAT_1").setActive(true));
        final TgCategory cat2 = save(new_(TgCategory.class, "NEW_CAT_2").setActive(true));
        
        // set properties one by one and assert refCount increasing
        final TgSystem sys = save(new_(TgSystem.class, "NEW_SYS").setActive(true).setFirstCategory(cat1).setSecondCategory(cat2));
        assertEquals(Integer.valueOf(1), co(TgCategory.class).findByKey(cat1.getKey()).getRefCount());
        assertEquals(Integer.valueOf(1), co(TgCategory.class).findByKey(cat2.getKey()).getRefCount());
        
        co(TgSystem.class).delete(sys);
        
        assertFalse(co(TgSystem.class).findByIdOptional(sys.getId()).isPresent());
        assertEquals(Integer.valueOf(0), co(TgCategory.class).findByKey(cat1.getKey()).getRefCount());
        assertEquals(Integer.valueOf(0), co(TgCategory.class).findByKey(cat2.getKey()).getRefCount());
    }


    @Test
    public void deletion_one_of_active_entities_that_reference_some_active_entity_decrements_its_refCount_by_one() {
        final TgCategory cat = save(new_(TgCategory.class, "NEW_CAT").setActive(true));
        
        final TgSystem sys1 = save(new_(TgSystem.class, "NEW_SYS_1").setActive(true).setFirstCategory(cat));
        save(new_(TgSystem.class, "NEW_SYS_2").setActive(true).setSecondCategory(cat));
        
        assertEquals(Integer.valueOf(2), co(TgCategory.class).findByKey(cat.getKey()).getRefCount());
        
        co(TgSystem.class).delete(sys1);
        
        assertFalse(co(TgSystem.class).findByIdOptional(sys1.getId()).isPresent());
        assertEquals(Integer.valueOf(1), co(TgCategory.class).findByKey(cat.getKey()).getRefCount());
    }

    
    @Test
    @Ignore
    public void consistency_test_with_concurrent_creation_and_deletion_of_activatable_entities_correctly_recomputes_refCount() throws Exception {
        final int NO_OF_CREATED_ACTIVE_DEPENDENCIES = 1000;
        final TgCategory cat = save(new_(TgCategory.class, "NEW_CAT").setActive(true));
        
        final Random rnd1 = new Random();
        final Random rnd2 = new Random();
        
        final User transactionUser = co(User.class).findByKeyAndFetch(fetchAll(User.class), UNIT_TEST_USER);
        final IUserProvider up = getInstance(IUserProvider.class);
        
        
        final Thread creationThread = new Thread(() -> {
            up.setUser(transactionUser);

            for (int index = 0; index < NO_OF_CREATED_ACTIVE_DEPENDENCIES; index++) {
                save(new_(TgSystem.class, "NEW_SYS_" + index).setActive(true).setFirstCategory(cat));
                try {
                    Thread.sleep(rnd1.nextInt(10));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                System.out.println("CREATED");
            }
        });
        
        final Thread creationDeletionThread = new Thread(() -> {
            up.setUser(transactionUser);
            
            for (int index = 0; index < NO_OF_CREATED_ACTIVE_DEPENDENCIES; index++) {
                final TgSystem sys = save(new_(TgSystem.class, "NEW_SYS").setActive(true).setFirstCategory(cat));
                try {
                    Thread.sleep(rnd2.nextInt(10));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
                co(TgSystem.class).delete(sys);
                System.out.println("CREATED/DELETED");
            }
        });
        
        creationThread.start();
        creationDeletionThread.start();
        
        creationThread.join();
        creationDeletionThread.join();
        
        assertEquals(Integer.valueOf(NO_OF_CREATED_ACTIVE_DEPENDENCIES), co(TgCategory.class).findByKey(cat.getKey()).getRefCount());
    }

}
