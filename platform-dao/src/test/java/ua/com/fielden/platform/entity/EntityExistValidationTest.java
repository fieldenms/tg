package ua.com.fielden.platform.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.sample.domain.TgCategory;
import ua.com.fielden.platform.sample.domain.TgSystem;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

public class EntityExistValidationTest extends AbstractDaoTestCase {


    @Test
    public void existing_active_entity_can_be_assigned_to_property_with_default_validation() {
        final TgCategory cat1 = co(TgCategory.class).findByKey("Cat1");
        final TgSystem sys = new_(TgSystem.class, "Sys2").setActive(true).setFirstCategory(cat1);

        assertTrue(sys.isValid().isSuccessful());
    }

    @Test
    public void existing_but_inactive_entity_cannot_be_assigned_to_property_with_default_validation() {
        final TgCategory cat2 = co(TgCategory.class).findByKey("Cat2");
        final TgSystem sys = new_(TgSystem.class, "Sys2").setActive(true).setFirstCategory(cat2);

        final Result result = sys.isValid();
        assertFalse(result.isSuccessful());
        assertEquals("Tg Category [Cat2] exists, but is not active.", result.getMessage());
    }

    @Test
    public void non_existing_entity_cannot_be_assigned_to_property_with_default_validation() {
        final TgCategory cat2 = co(TgCategory.class).findByKey("Cat2");

        // let's delete cat2 to make it non-existing
        co(TgCategory.class).delete(cat2);

        final TgSystem sys = new_(TgSystem.class, "Sys2").setActive(true).setFirstCategory(cat2);

        final Result result = sys.isValid();
        assertFalse(result.isSuccessful());
        assertEquals("Tg Category [Cat2] was not found.", result.getMessage());
    }

    @Test
    public void existing_entity_can_be_assigned_to_property_with_skipped_exists_validation() {
        final TgCategory cat2 = co(TgCategory.class).findByKey("Cat2");
        final TgSystem sys = new_(TgSystem.class, "Sys2").setActive(true).setSecondCategory(cat2);

        assertTrue(sys.isValid().isSuccessful());
        assertNotNull(sys.getSecondCategory());
    }

    @Test
    public void non_existing_entity_can_be_assigned_to_property_with_skipped_exists_validation() {
        final TgSystem sys = new_(TgSystem.class, "Sys2").setActive(true).setSecondCategory(new_(TgCategory.class, "Cat3"));

        assertTrue(sys.isValid().isSuccessful());
        assertNotNull(sys.getSecondCategory());
    }

    @Test
    public void non_existing_entity_cannot_be_assigned_to_property_with_only_active_check_skipped_validation() {
        final TgCategory cat2 = co(TgCategory.class).findByKey("Cat2");

        // let's delete cat2 to make it non-existing
        co(TgCategory.class).delete(cat2);

        final TgSystem sys = new_(TgSystem.class, "Sys2").setActive(true).setThirdCategory(cat2);

        final Result result = sys.isValid();
        assertFalse(result.isSuccessful());
        assertEquals("Tg Category [Cat2] was not found.", result.getMessage());
    }

    @Test
    public void existing_but_inactive_entity_can_be_assigned_to_property_with_only_active_check_skipped_validation() {
        final TgCategory cat2 = co(TgCategory.class).findByKey("Cat2");
        final TgSystem sys = new_(TgSystem.class, "Sys2").setActive(true).setThirdCategory(cat2);

        assertTrue(sys.isValid().isSuccessful());
        assertNotNull(sys.getThirdCategory());
    }

    @Test
    public void existing_and_active_entity_can_be_assigned_to_property_with_only_active_check_skipped_validation() {
        final TgCategory cat1 = co(TgCategory.class).findByKey("Cat1");
        final TgSystem sys = new_(TgSystem.class, "Sys2").setActive(true).setThirdCategory(cat1);

        assertTrue(sys.isValid().isSuccessful());
        assertNotNull(sys.getThirdCategory());
    }


    @Override
    protected void populateDomain() {
        super.populateDomain();
        
        final TgCategory cat1 = save(new_(TgCategory.class, "Cat1").setActive(true));
        save(new_(TgCategory.class, "Cat2").setActive(false).setParent(cat1));
    }

}
