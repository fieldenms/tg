package ua.com.fielden.platform.entity;

import static org.junit.Assert.*;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;

import java.util.List;

import org.junit.Test;

import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.sample.domain.TgCategory;
import ua.com.fielden.platform.sample.domain.TgSubSystem;
import ua.com.fielden.platform.test.AbstractDomainDrivenTestCase;
import ua.com.fielden.platform.test.PlatformTestDomainTypes;

public class AutomaticConflictResolutionTest extends AbstractDomainDrivenTestCase {

    @Test
    public void non_conflicting_changes_should_have_been_resolved() {
        final TgCategory cat1_v1 = ao(TgCategory.class).findByKeyAndFetch(fetchAll(TgCategory.class), "Cat1");
        final TgCategory cat1_v2 = ao(TgCategory.class).findByKeyAndFetch(fetchAll(TgCategory.class), "Cat1");

        final TgCategory savedCat1_v1 = save(cat1_v1.setDesc("new desc"));
        final TgCategory savedCat1_v2 = save(cat1_v2.setKey("CAT1"));

        assertEquals(savedCat1_v1.getDesc(), savedCat1_v2.getDesc());
        assertEquals("CAT1", savedCat1_v2.getKey());
    }

    @Test
    public void identical_conflicting_changes_should_have_been_resolved() {
        final TgCategory cat1_v1 = ao(TgCategory.class).findByKeyAndFetch(fetchAll(TgCategory.class), "Cat1");
        final TgCategory cat1_v2 = ao(TgCategory.class).findByKeyAndFetch(fetchAll(TgCategory.class), "Cat1");

        final TgCategory savedCat1_v1 = save(cat1_v1.setDesc("new desc"));
        final TgCategory savedCat1_v2 = save(cat1_v2.setDesc("new desc"));

        assertEquals(savedCat1_v1.getDesc(), savedCat1_v2.getDesc());
    }

    @Test
    public void conflicting_changes_should_have_prevent_entity_saving() {
        final TgCategory cat1_v1 = ao(TgCategory.class).findByKeyAndFetch(fetchAll(TgCategory.class), "Cat1");
        final TgCategory cat1_v2 = ao(TgCategory.class).findByKeyAndFetch(fetchAll(TgCategory.class), "Cat1");

        save(cat1_v1.setDesc("new desc"));

        try {
            save(cat1_v2.setDesc("other desc"));
            fail("Saving should have failed");
        } catch (final Result ex) {
            assertEquals("Could not resolve conflicting changes. Entity Cat1 (Tg Category) could not be saved.", ex.getMessage());
        }

    }

    @Test
    public void concurrent_setting_of_activatable_property_should_not_lead_to_conflict_resolution_errors() {
        final TgCategory cat1 = ao(TgCategory.class).findByKey("Cat1");

        save(new_(TgSubSystem.class, "Sys2").setFirstCategory(cat1));
        try {
            save(new_(TgSubSystem.class, "Sys3").setFirstCategory(cat1));
            assertEquals(Integer.valueOf(3), ao(TgCategory.class).findByKey("Cat1").getRefCount());
        } catch (final Result ex) {
            fail("saving should have been successful");
        }
    }


    @Override
    protected void populateDomain() {
        TgCategory cat1 = save(new_(TgCategory.class, "Cat1").setActive(true));
        cat1 = save(cat1.setParent(cat1));

        save(new_(TgSubSystem.class, "Sys1").setFirstCategory(cat1));
    }

    @Override
    protected List<Class<? extends AbstractEntity<?>>> domainEntityTypes() {
        return PlatformTestDomainTypes.entityTypes;
    }

}
