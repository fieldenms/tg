package ua.com.fielden.platform.entity.activatable;

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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

/// This test covers various ways of deleting activatable entities.
///
/// @see AbstractActivatableEntityDeletionAndRefCountTestCase
///
public class ActivatableEntityDeletionTest extends AbstractDaoTestCase implements WithActivatabilityTestUtils {

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
        assertRefCount(1, cat);
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
        assertRefCount(1, cat);
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
        assertRefCount(1, cat);
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
        assertRefCount(1, cat);
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
        assertRefCount(1, cat);
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
        assertRefCount(1, cat);
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
        assertRefCount(1, cat);
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
        assertRefCount(2, cat);
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

        assertRefCount(NO_OF_CREATED_ACTIVE_DEPENDENCIES, cat);
    }

}
