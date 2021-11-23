package ua.com.fielden.platform.entity.activatable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.sample.domain.TgCategory;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

public class SavingNewActivatableEntitiesWithReferencesToOtherActivatablesTest extends AbstractDaoTestCase {

    @Test
    public void saving_new_active_entity_with_reference_should_increase_refCount_of_that_reference() {
        final TgCategory cat1 = co$(TgCategory.class).findByKey("Cat1");
        assertEquals(Integer.valueOf(0), cat1.getRefCount());

        final TgCategory newCat = save(new_(TgCategory.class, "NEW").setActive(true).setParent(cat1));

        assertEquals(Integer.valueOf(0), newCat.getRefCount());
        assertEquals(Integer.valueOf(1), newCat.getParent().getRefCount());
        assertFalse("In-memory referential integrity is not supported.", System.identityHashCode(cat1) == System.identityHashCode(newCat.getParent()));
    }

    @Test
    public void saving_new_inactive_entity_with_reference_should_not_change_refCount_of_that_reference() {
        final TgCategory cat1 = co$(TgCategory.class).findByKey("Cat1");
        final TgCategory newCat = save(new_(TgCategory.class, "NEW").setActive(false).setParent(cat1));

        assertEquals(Integer.valueOf(0), newCat.getRefCount());
        assertEquals(Integer.valueOf(0), newCat.getParent().getRefCount());
    }

    @Test
    public void saving_new_inactive_with_self_reference_should_succeed() {
        final TgCategory cat5 = save(new_(TgCategory.class, "Cat5").setActive(false));
        final TgCategory savedCat5 = save(cat5.setParent(cat5));

        assertFalse(savedCat5.isActive());
        assertFalse(savedCat5.getParent().isActive());
    }

    @Test
    public void saving_new_active_entity_with_stale_reference_should_succeed() {
        final TgCategory cat1_1 = co$(TgCategory.class).findByKey("Cat1");
        final TgCategory cat1_2 = co$(TgCategory.class).findByKey("Cat1");

        final TgCategory newCat1 = new_(TgCategory.class, "NEW1").setActive(true).setParent(cat1_1);
        assertTrue(newCat1.isValid().isSuccessful());
        final TgCategory newCat2 = new_(TgCategory.class, "NEW2").setActive(true).setParent(cat1_2);
        assertTrue(newCat2.isValid().isSuccessful());

        // save newCat1, which should lead to staleness of cat1_2 entity
        final TgCategory updatedNewCat1 = save(newCat1);
        assertEquals(Integer.valueOf(1), updatedNewCat1.getParent().getRefCount());

        // try saving newCat2, which references already stale cat1_2 entity -- this still should succeed
        final TgCategory updatedNewCat2 = save(newCat2);
        assertEquals(Integer.valueOf(2), updatedNewCat2.getParent().getRefCount());

    }

    @Test
    public void saving_new_active_entity_with_stale_but_inactive_reference_should_fail() {
        final TgCategory cat1_1 = co$(TgCategory.class).findByKey("Cat1");

        final TgCategory newCat1 = new_(TgCategory.class, "NEW1").setActive(true).setParent(cat1_1);
        assertTrue(newCat1.isValid().isSuccessful());

        // introduce staleness through deactivation
        final TgCategory cat1_2 = co$(TgCategory.class).findByKey("Cat1");
        co$(TgCategory.class).save(cat1_2.setActive(false));

        try {
            save(newCat1);
            fail("Should have failed due to unsuccessful revalidation of property parent.");
        } catch (final Result ex) {
            assertEquals("Tg Category [Cat1] exists, but is not active.", ex.getMessage());
        }
    }


    @Override
    protected void populateDomain() {
        super.populateDomain();
        
        save(new_(TgCategory.class, "Cat1").setActive(true));
    }
}
