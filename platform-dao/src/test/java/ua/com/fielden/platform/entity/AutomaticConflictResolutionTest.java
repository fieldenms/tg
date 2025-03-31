package ua.com.fielden.platform.entity;

import org.apache.commons.text.RandomStringGenerator;
import org.junit.Test;
import ua.com.fielden.platform.dao.exceptions.EntityCompanionException;
import ua.com.fielden.platform.sample.domain.TgBogie;
import ua.com.fielden.platform.sample.domain.TgBogieClass;
import ua.com.fielden.platform.sample.domain.TgCategory;
import ua.com.fielden.platform.sample.domain.TgSystem;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

import static java.lang.String.format;
import static org.junit.Assert.*;
import static ua.com.fielden.platform.companion.PersistentEntitySaver.ERR_COULD_NOT_RESOLVE_CONFLICTING_CHANGES;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;

public class AutomaticConflictResolutionTest extends AbstractDaoTestCase {

    @Test
    public void non_conflicting_concurrent_changes_get_resolved_automatically() {
        final TgCategory cat1_v1 = co$(TgCategory.class).findByKeyAndFetch(fetchAll(TgCategory.class), "Cat1");
        final TgCategory cat1_v2 = co$(TgCategory.class).findByKeyAndFetch(fetchAll(TgCategory.class), "Cat1");

        final TgCategory savedCat1_v1 = save(cat1_v1.setDesc("new desc"));
        final TgCategory savedCat1_v2 = save(cat1_v2.setKey("CAT1"));

        assertEquals(savedCat1_v1.getDesc(), savedCat1_v2.getDesc());
        assertEquals("CAT1", savedCat1_v2.getKey());
    }

    @Test
    public void identical_concurrent_changes_get_resolved() {
        final TgCategory cat1_v1 = co$(TgCategory.class).findByKeyAndFetch(fetchAll(TgCategory.class), "Cat1");
        final TgCategory cat1_v2 = co$(TgCategory.class).findByKeyAndFetch(fetchAll(TgCategory.class), "Cat1");

        final TgCategory savedCat1_v1 = save(cat1_v1.setDesc("new desc"));
        final TgCategory savedCat1_v2 = save(cat1_v2.setDesc("new desc"));

        assertEquals(savedCat1_v1.getDesc(), savedCat1_v2.getDesc());
    }

    @Test
    public void conflicting_concurrent_changes_prevent_entity_saving() {
        final TgCategory cat1_v1 = co$(TgCategory.class).findByKeyAndFetch(fetchAll(TgCategory.class), "Cat1");
        final TgCategory cat1_v2 = co$(TgCategory.class).findByKeyAndFetch(fetchAll(TgCategory.class), "Cat1");

        save(cat1_v1.setDesc("new desc"));

        try {
            save(cat1_v2.setDesc("other desc"));
            fail("Saving should have failed");
        } catch (final EntityCompanionException ex) {
            assertEquals(format("%s Tg Category [Cat1] could not be saved.", ERR_COULD_NOT_RESOLVE_CONFLICTING_CHANGES), ex.getMessage());
        }
    }

    @Test
    public void concurrent_modification_of_refCount_due_to_new_activatable_dependencies_does_not_lead_to_conflict_resolution_errors() {
        final TgCategory cat1 = co(TgCategory.class).findByKey("Cat1");
        assertEquals(Integer.valueOf(1), cat1.getRefCount());

        save(new_(TgSystem.class, "Sys2").setActive(true).setCategory(cat1));
        save(new_(TgSystem.class, "Sys3").setActive(true).setCategory(cat1));
        assertEquals(Integer.valueOf(3), co(TgCategory.class).findByKey("Cat1").getRefCount());
    }

    @Test
    public void concurrent_modification_of_refCount_due_to_new_activatable_dependencies_does_not_lead_to_conflict_resolution_errors_for_entities_with_no_auto_conflict_resolution() {
        final TgBogieClass bc = co(TgBogieClass.class).findByKey("BC1");
        assertEquals(Integer.valueOf(0), bc.getRefCount());

        save(new_(TgBogie.class, "Bogie2").setBogieClass(bc));
        save(new_(TgBogie.class, "Bogie3").setBogieClass(bc));
        assertEquals(Integer.valueOf(2), co(TgBogieClass.class).findByKey("BC1").getRefCount());
    }

    @Test
    public void identical_concurrent_changes_of_entity_without_auto_conflict_resolutions_results_in_conflict() {
        final TgBogie bogie1_v1 = co$(TgBogie.class).findByKeyAndFetch(fetchAll(TgBogie.class), "Bogie1");
        final TgBogie bogie2_v1 = co$(TgBogie.class).findByKeyAndFetch(fetchAll(TgBogie.class), "Bogie1");

        assertNull(bogie1_v1.getDesc());
        assertNull(bogie2_v1.getDesc());
        assertEquals(bogie1_v1, bogie2_v1);
        
        final String newDesc = "new desc";
        save(bogie1_v1.setDesc(newDesc));

        try {
            save(bogie2_v1.setDesc(newDesc));
            fail("Saving should have failed");
        } catch (final EntityCompanionException ex) {
            assertEquals(format("%s Tg Bogie [Bogie1] could not be saved.", ERR_COULD_NOT_RESOLVE_CONFLICTING_CHANGES), ex.getMessage());
        }
    }

    @Test
    public void identical_concurrent_changes_of_a_property_without_auto_conflict_resolution_resuts_in_conflict() {
        // First, assert that conflict resolution is supported for TgCategory, if property other than `aggregate` is modified concurrently.
        {
            final TgCategory thisCat1 = co$(TgCategory.class).findByKeyAndFetch(fetchAll(TgCategory.class), "Cat1");
            final TgCategory thatCat1 = co$(TgCategory.class).findByKeyAndFetch(fetchAll(TgCategory.class), "Cat1");

            final var newDesc = new RandomStringGenerator.Builder().withinRange('a', 'z').build().generate(30);
            save(thisCat1.setDesc(newDesc));
            save(thatCat1.setDesc(newDesc));
        }

        // Now, assert that conflict resolution is not permitted for TgCategory, if property `aggregate` is modified concurrently.
        {
            final TgCategory thisCat1 = co$(TgCategory.class).findByKeyAndFetch(fetchAll(TgCategory.class), "Cat1");
            final TgCategory thatCat1 = co$(TgCategory.class).findByKeyAndFetch(fetchAll(TgCategory.class), "Cat1");

            save(thisCat1.setAggregate(42));
            try {
                save(thatCat1.setAggregate(42));
                fail();
            } catch (final EntityCompanionException ex) {
                assertEquals("%s Tg Category [Cat1] could not be saved.".formatted(ERR_COULD_NOT_RESOLVE_CONFLICTING_CHANGES), ex.getMessage());
            }
        }
    }

    @Override
    public boolean saveDataPopulationScriptToFile() {
        return false;
    }

    @Override
    public boolean useSavedDataPopulationScript() {
        return false;
    }

    @Override
    protected void populateDomain() {
        super.populateDomain();
        
        if (useSavedDataPopulationScript()) {
            return;
        }

        final TgCategory cat1 = save(new_(TgCategory.class, "Cat1").setActive(true));
        final TgCategory cat1WithSelfReference = save(cat1.setParent(cat1));

        save(new_(TgSystem.class, "Sys1").setActive(true).setCategory(cat1WithSelfReference));

       //entities with @MapEntityTo(autoConflictResolution = false)
        save(new_(TgBogieClass.class, "BC1"));
        save(new_(TgBogie.class, "Bogie1"));
    }

}