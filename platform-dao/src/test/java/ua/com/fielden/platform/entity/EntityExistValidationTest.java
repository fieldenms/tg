package ua.com.fielden.platform.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.sample.domain.TgCategory;
import ua.com.fielden.platform.sample.domain.TgSystem;
import ua.com.fielden.platform.test.AbstractDomainDrivenTestCase;
import ua.com.fielden.platform.test.PlatformTestDomainTypes;

public class EntityExistValidationTest extends AbstractDomainDrivenTestCase {


    @Test
    public void assigning_existing_active_entity_to_property_with_exists_validation_should_be_possile() {
        final TgCategory cat1 = co(TgCategory.class).findByKey("Cat1");
        final TgSystem sys = new_(TgSystem.class, "Sys2").setActive(true).setFirstCategory(cat1);

        assertTrue(sys.isValid().isSuccessful());
    }

    @Test
    public void assigning_existing_but_inactive_entity_to_property_with_exists_validation_should_not_be_possile() {
        final TgCategory cat2 = co(TgCategory.class).findByKey("Cat2");
        final TgSystem sys = new_(TgSystem.class, "Sys2").setActive(true).setFirstCategory(cat2);

        final Result result = sys.isValid();
        assertFalse(result.isSuccessful());
        assertEquals("Tg Category Cat2 exists, but is not active.", result.getMessage());
    }

    @Test
    public void assigning_non_existing_entity_to_property_with_exists_validation_should_not_be_possile() {
        final TgCategory cat2 = co(TgCategory.class).findByKey("Cat2");

        // let's delete cat2 to make it non-existing
        co(TgCategory.class).delete(cat2);

        final TgSystem sys = new_(TgSystem.class, "Sys2").setActive(true).setFirstCategory(cat2);

        final Result result = sys.isValid();
        assertFalse(result.isSuccessful());
        assertEquals("Tg Category Cat2 does not exist.", result.getMessage());
    }

    @Test
    public void assigning_non_existing_entity_to_property_with_skipped_exists_validation_should_be_possile() {
        final TgCategory cat2 = co(TgCategory.class).findByKey("Cat2");
        final TgSystem sys = new_(TgSystem.class, "Sys2").setActive(true).setSecondCategory(cat2);

        assertTrue(sys.isValid().isSuccessful());
        assertNotNull(sys.getSecondCategory());
    }

    @Test
    public void assigning_existing_entity_to_property_with_skipped_exists_validation_should_be_possile() {
        final TgSystem sys = new_(TgSystem.class, "Sys2").setActive(true).setSecondCategory(new_(TgCategory.class, "Cat3"));

        assertTrue(sys.isValid().isSuccessful());
        assertNotNull(sys.getSecondCategory());
    }

    @Test
    public void assigning_existing_entity_to_proprty_with_skipped_exists_validation_should_be_possile() {
        final TgCategory cat2 = co(TgCategory.class).findByKey("Cat2");
        final TgSystem sys = new_(TgSystem.class, "Sys2").setActive(true).setSecondCategory(cat2);

        assertTrue(sys.isValid().isSuccessful());
        assertNotNull(sys.getSecondCategory());
    }

    @Test
    public void assigning_non_existing_entity_to_property_with_only_active_check_skipped_should_not_be_possile() {
        final TgCategory cat2 = co(TgCategory.class).findByKey("Cat2");

        // let's delete cat2 to make it non-existing
        co(TgCategory.class).delete(cat2);

        final TgSystem sys = new_(TgSystem.class, "Sys2").setActive(true).setThirdCategory(cat2);

        final Result result = sys.isValid();
        assertFalse(result.isSuccessful());
        assertEquals("Tg Category Cat2 does not exist.", result.getMessage());
    }

    @Test
    public void assigning_existing_but_inactive_entity_to_property_with_only_active_check_skipped_should_be_possile() {
        final TgCategory cat2 = co(TgCategory.class).findByKey("Cat2");
        final TgSystem sys = new_(TgSystem.class, "Sys2").setActive(true).setThirdCategory(cat2);

        assertTrue(sys.isValid().isSuccessful());
        assertNotNull(sys.getThirdCategory());
    }

    @Test
    public void assigning_existing_and_active_entity_to_property_with_only_active_check_skipped_should_be_possile() {
        final TgCategory cat1 = co(TgCategory.class).findByKey("Cat1");
        final TgSystem sys = new_(TgSystem.class, "Sys2").setActive(true).setThirdCategory(cat1);

        assertTrue(sys.isValid().isSuccessful());
        assertNotNull(sys.getThirdCategory());
    }


    @Override
    protected void populateDomain() {
        final TgCategory cat1 = save(new_(TgCategory.class, "Cat1").setActive(true));
        save(new_(TgCategory.class, "Cat2").setActive(false).setParent(cat1));
    }

    @Override
    protected List<Class<? extends AbstractEntity<?>>> domainEntityTypes() {
        return PlatformTestDomainTypes.entityTypes;
    }

}
