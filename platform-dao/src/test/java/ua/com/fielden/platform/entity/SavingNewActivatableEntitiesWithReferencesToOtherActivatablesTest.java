package ua.com.fielden.platform.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import ua.com.fielden.platform.sample.domain.TgCategory;
import ua.com.fielden.platform.test.AbstractDomainDrivenTestCase;
import ua.com.fielden.platform.test.PlatformTestDomainTypes;

public class SavingNewActivatableEntitiesWithReferencesToOtherActivatablesTest extends AbstractDomainDrivenTestCase {

    @Test
    @Ignore
    public void saving_new_active_entity_with_reference_should_increase_refCount_of_that_reference() {
        final TgCategory cat1 = ao(TgCategory.class).findByKey("Cat1");
        assertEquals(Integer.valueOf(0), cat1.getRefCount());

        final TgCategory newCat = save(new_(TgCategory.class, "NEW").setActive(true).setParent(cat1));

        assertEquals(Integer.valueOf(0), newCat.getRefCount());
        assertEquals(Integer.valueOf(1), newCat.getParent().getRefCount());
        assertEquals("In-memory referential integrity is not followed.", Integer.valueOf(1), cat1.getRefCount());
    }

    @Test
    public void saving_new_inactive_entity_with_reference_should_not_change_refCount_of_that_reference() {
        final TgCategory cat1 = ao(TgCategory.class).findByKey("Cat1");
        final TgCategory newCat = save(new_(TgCategory.class, "NEW").setActive(false).setParent(cat1));

        assertEquals(Integer.valueOf(0), newCat.getRefCount());
        assertEquals(Integer.valueOf(0), newCat.getParent().getRefCount());
    }

    @Test
    @Ignore
    public void saving_new_active_entity_with_reference_should_recognise_stale_data() {
        final TgCategory cat1_1 = ao(TgCategory.class).findByKey("Cat1");
        final TgCategory cat1_2 = ao(TgCategory.class).findByKey("Cat1");

        final TgCategory newCat1 = new_(TgCategory.class, "NEW1").setActive(true).setParent(cat1_1);
        assertTrue(newCat1.isValid().isSuccessful());
        final TgCategory newCat2 = new_(TgCategory.class, "NEW2").setActive(true).setParent(cat1_2);
        assertTrue(newCat2.isValid().isSuccessful());

        // save newCat1, which should lead to staleness of cat1_2 entity
        save(newCat1);
        assertEquals(Integer.valueOf(1), cat1_1.getRefCount());

        // try saving newCat2, which references already stale cat1_2 entity
        try {
            save(newCat2);
            fail("Should have failed due to stale data");
        } catch (final Exception ex) {
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
