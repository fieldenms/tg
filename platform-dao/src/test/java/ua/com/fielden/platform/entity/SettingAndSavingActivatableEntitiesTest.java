package ua.com.fielden.platform.entity;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static ua.com.fielden.platform.entity.ActivatableAbstractEntity.ACTIVE;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;

import java.util.List;

import org.junit.Test;

import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.sample.domain.TgCategory;
import ua.com.fielden.platform.sample.domain.TgSubSystem;
import ua.com.fielden.platform.test.AbstractDomainDrivenTestCase;
import ua.com.fielden.platform.test.PlatformTestDomainTypes;

public class SettingAndSavingActivatableEntitiesTest extends AbstractDomainDrivenTestCase {


    @Test
    public void active_entity_with_active_references_should_not_be_allowed_to_become_inactive() {
        final TgCategory cat1 = ao(TgCategory.class).findByKey("Cat1");
        cat1.setActive(false);

        final MetaProperty<Boolean> activeProperty = cat1.getProperty("active");

        assertFalse(activeProperty.isValid());
        final String entityTitle = TitlesDescsGetter.getEntityTitleAndDesc(cat1.getType()).getKey();
        assertEquals(format("Entity %s has active dependencies (%s).", entityTitle, 2), activeProperty.getFirstFailure().getMessage());
    }

    @Test
    public void inactive_activatable_should_not_become_active_due_to_references_to_other_inactive_activatables_validation_test() {
        final TgCategory cat4 = ao(TgCategory.class).findByKeyAndFetch(fetchAll(TgCategory.class), "Cat4");
        assertFalse(cat4.getParent().isActive());

        cat4.setActive(true);

        assertNotNull(cat4.getProperty(ACTIVE).getFirstFailure());
        assertEquals(format("Entity %s has a reference to already inactive entity %s (type %s)", cat4, cat4.getParent(), cat4.getParent().getType()),
                     cat4.getProperty(ACTIVE).getFirstFailure().getMessage());
    }

    @Test
    public void inactive_activatable_should_not_become_active_due_to_references_to_other_inactive_activatables_save_test() {
        final TgCategory cat4 = ao(TgCategory.class).findByKeyAndFetch(fetchAll(TgCategory.class).without("parent"), "Cat4");
        assertNull(cat4.getParent());
        cat4.setActive(true);
        assertTrue(cat4.isValid().isSuccessful());

        try {
            ao(TgCategory.class).save(cat4);
            fail("Should have failed");
        } catch (final Result ex) {
            final TgCategory cat4Full = ao(TgCategory.class).findByKeyAndFetch(fetchAll(TgCategory.class), "Cat4");
            assertEquals(format("Entity %s has a reference to already inactive entity %s (type %s)", cat4Full, cat4Full.getParent(), cat4Full.getParent().getType()),
                         ex.getMessage());
        }
    }

    @Test
    public void deactivation_and_saving_of_self_referenced_activatable_should_be_permissible_and_not_decrement_its_ref_count() {
        final TgCategory cat5 = ao(TgCategory.class).findByKeyAndFetch(fetchAll(TgCategory.class), "Cat5");
        assertEquals(Integer.valueOf(0), cat5.getRefCount());

        final TgCategory savedCat5 = save(cat5.setActive(false));
        assertEquals(Integer.valueOf(0), savedCat5.getRefCount());
        assertFalse(savedCat5.isActive());
        assertFalse(savedCat5.getParent().isActive());
    }

    @Test
    public void activating_entity_that_is_referenced_by_inactive_should_be_permitted_but_not_change_its_ref_count_and_also_update_referenced_not_dirty_active_activatables() {
        final TgCategory cat3 = ao(TgCategory.class).findByKeyAndFetch(fetchAll(TgCategory.class), "Cat3");
        assertEquals(Integer.valueOf(0), cat3.getRefCount());
        final TgCategory savedCat3 = save(cat3.setActive(true));
        assertEquals("RefCount should not change", cat3.getRefCount(), savedCat3.getRefCount());
        assertEquals("RefCount of the referenced non-dirty activatable should have increated by 1.", cat3.getParent().getRefCount() + 1, savedCat3.getParent().getRefCount() + 0);
    }

    @Test
    public void activating_entity_that_was_referencing_inactive_activatable_should_not_change_ref_count_of_that_activatable() {
        final TgCategory cat4 = ao(TgCategory.class).findByKeyAndFetch(fetchAll(TgCategory.class), "Cat4");
        final TgCategory oldParent = cat4.getParent();
        assertFalse(cat4.isActive());
        final TgCategory savedCat4 = save(cat4.setParent(null).setActive(true));
        assertNull(savedCat4.getParent());
        assertTrue(savedCat4.isActive());

        final TgCategory cat3 = ao(TgCategory.class).findByKeyAndFetch(fetchAll(TgCategory.class), "Cat3");
        assertEquals(oldParent.getRefCount(), cat3.getRefCount());
    }

    @Test
    public void activating_entity_that_gets_its_active_activatable_property_dereferenced_should_decrement_ref_counts_of_the_dereferenced_entity() {
        final TgCategory cat3 = ao(TgCategory.class).findByKeyAndFetch(fetchAll(TgCategory.class), "Cat3");
        final TgCategory oldParent = cat3.getParent();
        assertEquals(Integer.valueOf(0), cat3.getRefCount());
        assertEquals(Integer.valueOf(2), oldParent.getRefCount());

        final TgCategory savedCat3 = save(cat3.setParent(null).setActive(true));
        assertNull(savedCat3.getParent());
        assertTrue(savedCat3.isActive());
        assertEquals("RefCount should not change", cat3.getRefCount(), savedCat3.getRefCount());
        assertEquals("RefCount of the de-referenced non-dirty activatable should have not changed.",
                     oldParent.getRefCount(),
                     ao(TgCategory.class).findByKeyAndFetch(fetchAll(TgCategory.class), "Cat1").getRefCount());
    }

    @Test
    public void deactivating_entity_should_lead_to_decrement_of_the_referenced_activatables() {
        final TgCategory cat2 = ao(TgCategory.class).findByKeyAndFetch(fetchAll(TgCategory.class), "Cat2");
        assertTrue(cat2.isActive());
        final TgCategory oldParent = cat2.getParent();

        final TgCategory savedCat2 = save(cat2.setActive(false));
        assertFalse(savedCat2.isActive());
        assertEquals(oldParent.getRefCount() - 1, savedCat2.getParent().getRefCount() + 0);
    }

    @Test
    public void changing_activatable_properties_should_lead_to_decrement_of_dereferenced_instances_and_increment_of_just_referenced_ones() {
        final TgSubSystem sys1 = ao(TgSubSystem.class).findByKeyAndFetch(fetchAll(TgSubSystem.class), "Sys1");
        final TgCategory cat1BeforeChange = sys1.getFirstCategory();
        assertEquals(Integer.valueOf(2), cat1BeforeChange.getRefCount());
        final TgCategory cat6 = ao(TgCategory.class).findByKeyAndFetch(fetchAll(TgCategory.class), "Cat6");

        final TgSubSystem savedSys1 = save(sys1.setFirstCategory(cat6));
        assertEquals(cat1BeforeChange.getRefCount() - 1, ao(TgCategory.class).findByKey("Cat1").getRefCount() + 0);
        assertEquals(cat6.getRefCount() + 1, savedSys1.getFirstCategory().getRefCount() + 0);
    }

    @Test
    public void changing_and_unsetting_activatable_properties_should_lead_to_decrement_of_dereferenced_instances_and_increment_of_just_referenced_ones() {
        final TgSubSystem sys2 = ao(TgSubSystem.class).findByKeyAndFetch(fetchAll(TgSubSystem.class), "Sys2");
        final TgCategory cat6BeforeChange = sys2.getFirstCategory();
        assertEquals(Integer.valueOf(2), cat6BeforeChange.getRefCount());
        final TgCategory cat1 = ao(TgCategory.class).findByKeyAndFetch(fetchAll(TgCategory.class), "Cat1");

        final TgSubSystem savedSys2 = save(sys2.setFirstCategory(cat1).setSecondCategory(null));
        assertEquals(cat6BeforeChange.getRefCount() - 2, ao(TgCategory.class).findByKey("Cat6").getRefCount() + 0);
        assertEquals(cat1.getRefCount() + 1, savedSys2.getFirstCategory().getRefCount() + 0);
    }

    @Test
    public void self_referenced_activatable_should_be_able_to_become_inactive() {
        final TgCategory cat5 = ao(TgCategory.class).findByKeyAndFetch(fetchAll(TgCategory.class), "Cat5");
        final TgCategory savedCat5 = save(cat5.setActive(false));

        assertFalse(savedCat5.isActive());
        assertEquals(savedCat5, savedCat5.getParent());
        assertEquals(Integer.valueOf(0), savedCat5.getRefCount());
    }

    @Test
    public void deactivation_with_simultaneous_derefernesing_of_active_actiavatables_should_be_supported() {
        final TgCategory cat2 = ao(TgCategory.class).findByKeyAndFetch(fetchAll(TgCategory.class), "Cat2");
        final TgCategory cat1 = cat2.getParent();
        final TgCategory savedCat2 = save(cat2.setParent(null).setActive(false));

        assertFalse(savedCat2.isActive());
        assertNull(savedCat2.getParent());
        assertEquals(cat1.getRefCount() - 1, ao(TgCategory.class).findByKey("Cat1").getRefCount() + 0);
    }


    @Test
    public void concurrent_referencing_of_activatable_that_has_just_became_inactive_should_have_been_prevented() {
        final TgCategory cat7 = ao(TgCategory.class).findByKeyAndFetch(fetchAll(TgCategory.class), "Cat7");
        final TgSubSystem subSystem = new_(TgSubSystem.class, "Sys3").setFirstCategory(cat7);

        // let's make concurrent deactivation of just referenced cat7
        save(cat7.setActive(false));

        try {
            save(subSystem);
            fail("An attempt to save successfully associated, but alread inactive activatable should fail.");
        } catch (final Result ex) {
            assertEquals("EntityExists validator: Could not find entity Cat7", ex.getMessage());
        }
    }

    @Override
    protected void populateDomain() {
        TgCategory cat1 = save(new_(TgCategory.class, "Cat1").setActive(true));
        cat1 = save(cat1.setParent(cat1));
        final TgCategory cat2 = save(new_(TgCategory.class, "Cat2").setActive(true).setParent(cat1));
        final TgCategory cat3 = save(new_(TgCategory.class, "Cat3").setActive(false).setParent(cat1));
        save(new_(TgCategory.class, "Cat4").setActive(false).setParent(cat3));
        final TgCategory cat5 = save(new_(TgCategory.class, "Cat5").setActive(true));
        save(cat5.setParent(cat5));

        save(new_(TgSubSystem.class, "Sys1").setFirstCategory(cat1));
        final TgCategory cat6 = save(new_(TgCategory.class, "Cat6").setActive(true));
        final TgCategory cat7 = save(new_(TgCategory.class, "Cat7").setActive(true));
        save(new_(TgSubSystem.class, "Sys2").setFirstCategory(cat6).setSecondCategory(cat6));
    }

    @Override
    protected List<Class<? extends AbstractEntity<?>>> domainEntityTypes() {
        return PlatformTestDomainTypes.entityTypes;
    }

}
