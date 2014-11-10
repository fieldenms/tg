package ua.com.fielden.platform.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;

import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.sample.domain.TgCategory;
import ua.com.fielden.platform.test.AbstractDomainDrivenTestCase;
import ua.com.fielden.platform.test.PlatformTestDomainTypes;

public class SavingNewActivatableEntitiesWithReferencesToOtherActivatablesTest extends AbstractDomainDrivenTestCase {

    @Test
    public void saving_new_active_entity_with_reference_should_increase_refCount_of_that_reference() {
        final TgCategory cat1 = ao(TgCategory.class).findByKey("Cat1");
        assertEquals(Integer.valueOf(0), cat1.getRefCount());

        final TgCategory newCat = save(new_(TgCategory.class, "NEW").setActive(true).setParent(cat1));

        assertEquals(Integer.valueOf(0), newCat.getRefCount());
        assertEquals(Integer.valueOf(1), newCat.getParent().getRefCount());
        assertFalse("In-memory referential integrity is not supported.", System.identityHashCode(cat1) == System.identityHashCode(newCat.getParent()));
    }

    @Test
    public void saving_new_inactive_entity_with_reference_should_not_change_refCount_of_that_reference() {
        final TgCategory cat1 = ao(TgCategory.class).findByKey("Cat1");
        final TgCategory newCat = save(new_(TgCategory.class, "NEW").setActive(false).setParent(cat1));

        assertEquals(Integer.valueOf(0), newCat.getRefCount());
        assertEquals(Integer.valueOf(0), newCat.getParent().getRefCount());
    }

    @Test
    public void saving_new_active_entity_with_stale_reference_should_succeed() {
        final TgCategory cat1_1 = ao(TgCategory.class).findByKey("Cat1");
        final TgCategory cat1_2 = ao(TgCategory.class).findByKey("Cat1");

        final TgCategory newCat1 = new_(TgCategory.class, "NEW1").setActive(true).setParent(cat1_1);
        assertTrue(newCat1.isValid().isSuccessful());
        final TgCategory newCat2 = new_(TgCategory.class, "NEW2").setActive(true).setParent(cat1_2);
        assertTrue(newCat2.isValid().isSuccessful());

        // save newCat1, which should lead to staleness of cat1_2 entity
        save(newCat1);
        assertEquals(Integer.valueOf(1), newCat1.getParent().getRefCount());

        // try saving newCat2, which references already stale cat1_2 entity -- this still should succeed
        save(newCat2);
        assertEquals(Integer.valueOf(2), newCat2.getParent().getRefCount());

    }

    @Test
    public void saving_new_active_entity_with_stale_but_inactive_reference_should_fail() {
        final TgCategory cat1_1 = ao(TgCategory.class).findByKey("Cat1");

        final TgCategory newCat1 = new_(TgCategory.class, "NEW1").setActive(true).setParent(cat1_1);
        assertTrue(newCat1.isValid().isSuccessful());

        // introduce staleness through deactivation
        final TgCategory cat1_2 = ao(TgCategory.class).findByKey("Cat1");
        ao(TgCategory.class).save(cat1_2.setActive(false));

        try {
            save(newCat1);
            fail("Should have failed due to unsuccessful revalidation of property parent.");
        } catch (final Result ex) {
            assertEquals("EntityExists validator: Could not find entity Cat1", ex.getMessage());
        }
    }


    @Override
    protected void populateDomain() {
        save(new_(TgCategory.class, "Cat1").setActive(true));
       }

    @Override
    protected List<Class<? extends AbstractEntity<?>>> domainEntityTypes() {
        return PlatformTestDomainTypes.entityTypes;
    }

}
