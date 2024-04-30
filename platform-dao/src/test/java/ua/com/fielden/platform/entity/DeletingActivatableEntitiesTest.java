package ua.com.fielden.platform.entity;

import org.junit.Ignore;
import org.junit.Test;
import ua.com.fielden.platform.sample.domain.TgCategory;
import ua.com.fielden.platform.sample.domain.TgSystem;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static org.junit.Assert.*;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

public class DeletingActivatableEntitiesTest extends AbstractDaoTestCase {

    @Test
    public void deletion_of_active_entity_that_references_the_same_active_entity_in_two_properties_decrements_its_refCount_by_two() {
        final TgCategory cat = save(new_(TgCategory.class, "NEW_CAT").setActive(true));
        
        final TgSystem sys = save(new_(TgSystem.class, "NEW_SYS").setActive(true).setFirstCategory(cat).setSecondCategory(cat));
        assertEquals(Integer.valueOf(2), co$(TgCategory.class).findByKey(cat.getKey()).getRefCount());
        
        co$(TgSystem.class).delete(sys);
        
        assertFalse(co$(TgSystem.class).findByIdOptional(sys.getId()).isPresent());
        assertEquals(Integer.valueOf(0), co$(TgCategory.class).findByKey(cat.getKey()).getRefCount());
    }
    
    @Test
    public void deletion_of_active_entity_that_references_two_different_active_entities_decrements_their_refCount_by_one() {
        final TgCategory cat1 = save(new_(TgCategory.class, "NEW_CAT_1").setActive(true));
        final TgCategory cat2 = save(new_(TgCategory.class, "NEW_CAT_2").setActive(true));
        
        // set properties one by one and assert refCount increasing
        final TgSystem sys = save(new_(TgSystem.class, "NEW_SYS").setActive(true).setFirstCategory(cat1).setSecondCategory(cat2));
        assertEquals(Integer.valueOf(1), co$(TgCategory.class).findByKey(cat1.getKey()).getRefCount());
        assertEquals(Integer.valueOf(1), co$(TgCategory.class).findByKey(cat2.getKey()).getRefCount());
        
        co$(TgSystem.class).delete(sys);
        
        assertFalse(co$(TgSystem.class).findByIdOptional(sys.getId()).isPresent());
        assertEquals(Integer.valueOf(0), co$(TgCategory.class).findByKey(cat1.getKey()).getRefCount());
        assertEquals(Integer.valueOf(0), co$(TgCategory.class).findByKey(cat2.getKey()).getRefCount());
    }


    @Test
    public void deletion_one_of_active_entities_that_reference_some_active_entity_decrements_its_refCount_by_one() {
        final TgCategory cat = save(new_(TgCategory.class, "NEW_CAT").setActive(true));
        
        final TgSystem sys1 = save(new_(TgSystem.class, "NEW_SYS_1").setActive(true).setFirstCategory(cat));
        save(new_(TgSystem.class, "NEW_SYS_2").setActive(true).setSecondCategory(cat));
        
        assertEquals(Integer.valueOf(2), co$(TgCategory.class).findByKey(cat.getKey()).getRefCount());
        
        co$(TgSystem.class).delete(sys1);
        
        assertFalse(co$(TgSystem.class).findByIdOptional(sys1.getId()).isPresent());
        assertEquals(Integer.valueOf(1), co$(TgCategory.class).findByKey(cat.getKey()).getRefCount());
    }

    @Test
    public void deletion_of_inactive_entities_does_not_effect_refCount_of_referenced_by_them_active_entities() {
        final TgCategory cat = save(new_(TgCategory.class, "NEW_CAT_1").setActive(true));

        final TgSystem sys1 = save(new_(TgSystem.class, "NEW_SYS_1").setActive(true).setFirstCategory(cat));
        final TgSystem sys2 = save(new_(TgSystem.class, "NEW_SYS_2").setActive(false).setSecondCategory(cat));

        assertEquals(Integer.valueOf(1), co$(TgCategory.class).findByKey(cat.getKey()).getRefCount());

        co$(TgSystem.class).delete(sys2);

        assertFalse(co$(TgSystem.class).findByIdOptional(sys2.getId()).isPresent());
        assertEquals(Integer.valueOf(1), co$(TgCategory.class).findByKey(cat.getKey()).getRefCount());
    }

    @Test
    public void deletion_with_EQL_model_is_supported_for_activatable_entities() {
        final TgCategory cat = save(new_(TgCategory.class, "NEW_CAT").setActive(true));
        final TgSystem sys1 = save(new_(TgSystem.class, "NEW_SYS_1").setActive(true).setFirstCategory(cat));
        final TgSystem sys2 = save(new_(TgSystem.class, "NEW_SYS_2").setActive(true).setSecondCategory(cat));
        final TgSystem sys3 = save(new_(TgSystem.class, "NEW_SYS_3").setActive(true).setSecondCategory(cat));
        
        co$(TgSystem.class).delete(select(TgSystem.class).where().prop("key").in().values(sys1.getKey(), sys2.getKey()).model());
        
        assertFalse(co$(TgSystem.class).findByIdOptional(sys1.getId()).isPresent());
        assertFalse(co$(TgSystem.class).findByIdOptional(sys2.getId()).isPresent());
        assertTrue(co$(TgSystem.class).findByIdOptional(sys3.getId()).isPresent());
        assertEquals(Integer.valueOf(1), co$(TgCategory.class).findByKey(cat.getKey()).getRefCount());
    }

    @Test
    public void deletion_with_paremeterised_EQL_model_is_supported_for_activatable_entities() {
        final TgCategory cat = save(new_(TgCategory.class, "NEW_CAT").setActive(true));
        final TgSystem sys1 = save(new_(TgSystem.class, "NEW_SYS_1").setActive(true).setFirstCategory(cat));
        final TgSystem sys2 = save(new_(TgSystem.class, "NEW_SYS_2").setActive(true).setSecondCategory(cat));
        final TgSystem sys3 = save(new_(TgSystem.class, "NEW_SYS_3").setActive(true).setSecondCategory(cat));
        
        final Map<String, Object> params = new HashMap<>();
        params.put("sys1", sys1.getKey());
        params.put("sys3", sys3.getKey());
        co$(TgSystem.class).delete(select(TgSystem.class).where().prop("key").in().params("sys1", "sys3").model(), params);
        
        assertFalse(co$(TgSystem.class).findByIdOptional(sys1.getId()).isPresent());
        assertTrue(co$(TgSystem.class).findByIdOptional(sys2.getId()).isPresent());
        assertFalse(co$(TgSystem.class).findByIdOptional(sys3.getId()).isPresent());
        assertEquals(Integer.valueOf(1), co$(TgCategory.class).findByKey(cat.getKey()).getRefCount());
    }

    @Test
    public void batch_deletion_with_EQL_model_is_supported_for_activatable_entities() {
        final TgCategory cat = save(new_(TgCategory.class, "NEW_CAT").setActive(true));
        final TgSystem sys1 = save(new_(TgSystem.class, "NEW_SYS_1").setActive(true).setFirstCategory(cat));
        final TgSystem sys2 = save(new_(TgSystem.class, "NEW_SYS_2").setActive(true).setSecondCategory(cat));
        final TgSystem sys3 = save(new_(TgSystem.class, "NEW_SYS_3").setActive(true).setSecondCategory(cat));
        
        co$(TgSystem.class).batchDelete(select(TgSystem.class).where().prop("key").in().values(sys1.getKey(), sys2.getKey()).model());
        
        assertFalse(co$(TgSystem.class).findByIdOptional(sys1.getId()).isPresent());
        assertFalse(co$(TgSystem.class).findByIdOptional(sys2.getId()).isPresent());
        assertTrue(co$(TgSystem.class).findByIdOptional(sys3.getId()).isPresent());
        assertEquals(Integer.valueOf(1), co$(TgCategory.class).findByKey(cat.getKey()).getRefCount());
    }

    @Test
    public void batch_deletion_with_paremeterised_EQL_model_is_supported_for_activatable_entities() {
        final TgCategory cat = save(new_(TgCategory.class, "NEW_CAT").setActive(true));
        final TgSystem sys1 = save(new_(TgSystem.class, "NEW_SYS_1").setActive(true).setFirstCategory(cat));
        final TgSystem sys2 = save(new_(TgSystem.class, "NEW_SYS_2").setActive(true).setSecondCategory(cat));
        final TgSystem sys3 = save(new_(TgSystem.class, "NEW_SYS_3").setActive(true).setSecondCategory(cat));
        
        final Map<String, Object> params = new HashMap<>();
        params.put("sys1", sys1.getKey());
        params.put("sys3", sys3.getKey());
        co$(TgSystem.class).batchDelete(select(TgSystem.class).where().prop("key").in().params("sys1", "sys3").model(), params);
        
        assertFalse(co$(TgSystem.class).findByIdOptional(sys1.getId()).isPresent());
        assertTrue(co$(TgSystem.class).findByIdOptional(sys2.getId()).isPresent());
        assertFalse(co$(TgSystem.class).findByIdOptional(sys3.getId()).isPresent());
        assertEquals(Integer.valueOf(1), co$(TgCategory.class).findByKey(cat.getKey()).getRefCount());
    }

    @Test
    public void batch_deletion_by_IDs_is_supported_for_activatable_entities() {
        final TgCategory cat = save(new_(TgCategory.class, "NEW_CAT").setActive(true));
        final TgSystem sys1 = save(new_(TgSystem.class, "NEW_SYS_1").setActive(true).setFirstCategory(cat));
        final TgSystem sys2 = save(new_(TgSystem.class, "NEW_SYS_2").setActive(true).setSecondCategory(cat));
        final TgSystem sys3 = save(new_(TgSystem.class, "NEW_SYS_3").setActive(true).setSecondCategory(cat));
        
        co$(TgSystem.class).batchDelete(Arrays.asList(new Long[] {sys1.getId(), sys2.getId()}));
        
        assertFalse(co$(TgSystem.class).findByIdOptional(sys1.getId()).isPresent());
        assertFalse(co$(TgSystem.class).findByIdOptional(sys2.getId()).isPresent());
        assertTrue(co$(TgSystem.class).findByIdOptional(sys3.getId()).isPresent());
        assertEquals(Integer.valueOf(1), co$(TgCategory.class).findByKey(cat.getKey()).getRefCount());
    }

    @Test
    public void batch_deletion_by_instances_is_supported_for_activatable_entities() {
        final TgCategory cat = save(new_(TgCategory.class, "NEW_CAT").setActive(true));
        final TgSystem sys1 = save(new_(TgSystem.class, "NEW_SYS_1").setActive(true).setFirstCategory(cat));
        final TgSystem sys2 = save(new_(TgSystem.class, "NEW_SYS_2").setActive(true).setSecondCategory(cat));
        final TgSystem sys3 = save(new_(TgSystem.class, "NEW_SYS_3").setActive(true).setSecondCategory(cat));
        
        co$(TgSystem.class).batchDelete(Arrays.asList(new TgSystem[] {sys1, sys2}));
        
        assertFalse(co$(TgSystem.class).findByIdOptional(sys1.getId()).isPresent());
        assertFalse(co$(TgSystem.class).findByIdOptional(sys2.getId()).isPresent());
        assertTrue(co$(TgSystem.class).findByIdOptional(sys3.getId()).isPresent());
        assertEquals(Integer.valueOf(1), co$(TgCategory.class).findByKey(cat.getKey()).getRefCount());
    }

    @Test
    public void batch_deletion_by_prop_matching_IDs_is_supported_for_activatable_entities() {
        final TgCategory cat = save(new_(TgCategory.class, "NEW_CAT").setActive(true));
        final TgSystem sys1 = save(new_(TgSystem.class, "NEW_SYS_1").setActive(true).setFirstCategory(cat));
        final TgSystem sys2 = save(new_(TgSystem.class, "NEW_SYS_2").setActive(true).setSecondCategory(cat));
        final TgSystem sys3 = save(new_(TgSystem.class, "NEW_SYS_3").setActive(true).setSecondCategory(cat));
        
        co$(TgSystem.class).batchDeleteByPropertyValues("secondCategory", Arrays.asList(new Long[] {cat.getId()}));
        
        assertTrue(co$(TgSystem.class).findByIdOptional(sys1.getId()).isPresent());
        assertFalse(co$(TgSystem.class).findByIdOptional(sys2.getId()).isPresent());
        assertFalse(co$(TgSystem.class).findByIdOptional(sys3.getId()).isPresent());
        assertEquals(Integer.valueOf(1), co$(TgCategory.class).findByKey(cat.getKey()).getRefCount());
    }

    @Test
    public void batch_deletion_by_prop_matching_values_is_supported_for_activatable_entities() {
        final TgCategory cat = save(new_(TgCategory.class, "NEW_CAT").setActive(true));
        final TgSystem sys1 = save(new_(TgSystem.class, "NEW_SYS_1").setActive(true).setFirstCategory(cat));
        final TgSystem sys2 = save(new_(TgSystem.class, "NEW_SYS_2").setActive(true).setSecondCategory(cat));
        final TgSystem sys3 = save(new_(TgSystem.class, "NEW_SYS_3").setActive(true).setSecondCategory(cat));
        
        co$(TgSystem.class).batchDeleteByPropertyValues("firstCategory", Arrays.asList(new TgCategory[] {cat}));
        
        assertFalse(co$(TgSystem.class).findByIdOptional(sys1.getId()).isPresent());
        assertTrue(co$(TgSystem.class).findByIdOptional(sys2.getId()).isPresent());
        assertTrue(co$(TgSystem.class).findByIdOptional(sys3.getId()).isPresent());
        assertEquals(Integer.valueOf(2), co$(TgCategory.class).findByKey(cat.getKey()).getRefCount());
    }

    @Test
    @Ignore
    public void consistency_test_with_concurrent_creation_and_deletion_of_activatable_entities_correctly_recomputes_refCount() throws Exception {
        final int NO_OF_CREATED_ACTIVE_DEPENDENCIES = 1000;
        final TgCategory cat = save(new_(TgCategory.class, "NEW_CAT").setActive(true));
        
        final Random rnd1 = new Random();
        final Random rnd2 = new Random();
        
        final User transactionUser = co$(User.class).findByKeyAndFetch(fetchAll(User.class), UNIT_TEST_USER);
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
            }
        });
        
        final Thread creationDeletionThread = new Thread(() -> {
            up.setUser(transactionUser);
            
            for (int index = 0; index < NO_OF_CREATED_ACTIVE_DEPENDENCIES; index++) {
                final TgSystem sys = save(new_(TgSystem.class, "NEW_SYS").setActive(true).setFirstCategory(cat));
                final String msg = "CREATED... " + co$(TgCategory.class).findByKey(cat.getKey()).getRefCount();
                try {
                    Thread.sleep(rnd2.nextInt(10));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
                co$(TgSystem.class).delete(sys);
            }
        });
        
        creationThread.start();
        creationDeletionThread.start();
        
        creationThread.join();
        creationDeletionThread.join();
        
        assertEquals(Integer.valueOf(NO_OF_CREATED_ACTIVE_DEPENDENCIES), co$(TgCategory.class).findByKey(cat.getKey()).getRefCount());
    }

}
