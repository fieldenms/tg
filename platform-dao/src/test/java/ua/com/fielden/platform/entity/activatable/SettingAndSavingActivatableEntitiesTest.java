package ua.com.fielden.platform.entity.activatable;

import org.junit.Test;
import ua.com.fielden.platform.dao.exceptions.EntityCompanionException;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.sample.domain.TgCategory;
import ua.com.fielden.platform.sample.domain.TgPerson;
import ua.com.fielden.platform.sample.domain.TgSubSystem;
import ua.com.fielden.platform.sample.domain.TgSystem;
import ua.com.fielden.platform.security.user.*;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.*;
import static ua.com.fielden.platform.entity.ActivatableAbstractEntity.ACTIVE;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetch;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;
import static ua.com.fielden.platform.entity.validation.ActivePropertyValidator.ERR_INACTIVE_REFERENCES;
import static ua.com.fielden.platform.entity.validation.EntityExistsValidator.ERR_ENTITY_EXISTS_BUT_NOT_ACTIVE;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitleAndDesc;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getTitleAndDesc;

public class SettingAndSavingActivatableEntitiesTest extends AbstractDaoTestCase {

    @Test
    public void active_entity_with_active_references_cannot_be_deactivated() {
        final TgCategory cat1 = co$(TgCategory.class).findByKey("Cat1");
        cat1.setActive(false);

        final MetaProperty<Boolean> activeProperty = cat1.getProperty("active");

        assertFalse(activeProperty.isValid());
        final String entityTitle = getEntityTitleAndDesc(cat1.getType()).getKey();
        assertTrue(activeProperty.getFirstFailure().getMessage().startsWith(format("%s [%s] has %s active dependencies", entityTitle, cat1, 2)));
    }

    @Test
    public void activating_an_entity_that_references_inactive_entities_does_not_pass_validation() {
        final var cat4 = co$(TgCategory.class).findByKeyAndFetch(fetchAll(TgCategory.class), "Cat4");
        assertFalse(cat4.isActive());
        assertFalse(cat4.getParent().isActive());

        cat4.setActive(true);

        assertNotNull(cat4.getProperty(ACTIVE).getFirstFailure());
        assertEquals("Property [Selfy] in Tg Category [Cat4] references inactive Tg Category [Cat3].",
                     cat4.getProperty(ACTIVE).getFirstFailure().getMessage());
    }

    @Test
    public void activating_an_entity_that_references_inactive_entities_referenced_via_proxied_properties_passes_validation_but_save_fails() {
        final var cat4 = co$(TgCategory.class).findByKeyAndFetch(fetchAll(TgCategory.class).without("parent"), "Cat4");
        assertFalse(cat4.isActive());
        assertThat(cat4.getPropertyIfNotProxy("parent")).isEmpty();
        cat4.setActive(true);
        assertTrue(cat4.isValid().isSuccessful());

        assertThatThrownBy(() -> co$(TgCategory.class).save(cat4))
                .isInstanceOf(EntityCompanionException.class)
                .satisfies(ex -> {
                    final var categoryTitle = TitlesDescsGetter.getEntityTitleAndDesc(TgCategory.class).getKey();
                    final var cat4Full = co$(TgCategory.class).findByKeyAndFetch(fetchAll(TgCategory.class), "Cat4");
                    assertEquals(format(ERR_INACTIVE_REFERENCES, getTitleAndDesc("parent", TgCategory.class).getKey(), categoryTitle, cat4Full, categoryTitle, cat4Full.getParent()),
                                 ex.getMessage());
                });
    }

    @Test
    public void activating_entity_referencing_inactive_values_in_properties_with_SkipEntityExistsValidation_where_skipActiveOnly_eq_true_is_permitted() {
        final TgCategory cat4 = co$(TgCategory.class).findByKeyAndFetch(fetchAll(TgCategory.class), "Cat4");
        assertFalse(cat4.isActive());

        final TgSystem inactiveSys3 = save(new_(TgSystem.class, "Sys3").setActive(false).setThirdCategory(cat4));
        
        final TgSystem activatedSys3 = inactiveSys3.setActive(true);
        assertNull(activatedSys3.getProperty(ACTIVE).getFirstFailure());
        save(activatedSys3);
    }

    @Test
    public void activating_entity_referencing_inactive_values_in_properties_with_SkipEntityExistsValidation_where_skipActiveOnly_eq_false_is_not_permitted() {
        final TgCategory cat4 = co$(TgCategory.class).findByKeyAndFetch(fetchAll(TgCategory.class), "Cat4");
        assertFalse(cat4.isActive());
        
        final TgSystem inactiveSys3 = save(new_(TgSystem.class, "Sys3").setActive(false).setSecondCategory(cat4));
        
        final TgSystem sys3 = inactiveSys3.setActive(true);
        assertNotNull(sys3.getProperty(ACTIVE).getFirstFailure());
        assertEquals("Property [Second Cat] in Tg System [Sys3] references inactive Tg Category [Cat4].", sys3.getProperty(ACTIVE).getFirstFailure().getMessage());
    }

    
    @Test
    public void deactivation_and_saving_of_self_referenced_activatable_is_permissible_but_does_not_decrement_its_ref_count() {
        final TgCategory cat5 = co$(TgCategory.class).findByKeyAndFetch(fetchAll(TgCategory.class), "Cat5");
        assertEquals(Integer.valueOf(0), cat5.getRefCount());

        final TgCategory savedCat5 = save(cat5.setActive(false));
        assertEquals(Integer.valueOf(0), savedCat5.getRefCount());
        assertFalse(savedCat5.isActive());
        assertFalse(savedCat5.getParent().isActive());
    }

    @Test
    public void activating_entity_that_is_referenced_by_inactive_is_permitted_but_does_not_change_its_ref_count_and_also_updates_referenced_not_dirty_active_activatables() {
        final TgCategory cat3 = co$(TgCategory.class).findByKeyAndFetch(fetchAll(TgCategory.class), "Cat3");
        assertEquals(Integer.valueOf(0), cat3.getRefCount());
        final TgCategory cat1 = co$(TgCategory.class).findByKey("Cat1");
        assertTrue(cat1.isActive());

        final TgCategory savedCat3 = save(cat3.setActive(true));
        assertTrue(co$(TgCategory.class).findByKey("Cat1").isActive());
        assertEquals("RefCount should not change", cat3.getRefCount(), savedCat3.getRefCount());
        assertEquals("RefCount of the referenced non-dirty activatable should have increated by 1.", cat3.getParent().getRefCount() + 1, savedCat3.getParent().getRefCount() + 0);
    }

    @Test
    public void activating_entity_that_was_referencing_inactive_activatable_does_not_change_ref_count_of_that_activatable() {
        final TgCategory cat4 = co$(TgCategory.class).findByKeyAndFetch(fetchAll(TgCategory.class), "Cat4");
        final TgCategory oldParent = cat4.getParent();
        assertFalse(cat4.isActive());
        final TgCategory savedCat4 = save(cat4.setParent(null).setActive(true));
        assertNull(savedCat4.getParent());
        assertTrue(savedCat4.isActive());

        final TgCategory cat3 = co$(TgCategory.class).findByKeyAndFetch(fetchAll(TgCategory.class), "Cat3");
        assertEquals(oldParent.getRefCount(), cat3.getRefCount());
    }

    @Test
    public void activating_entity_A_and_dereferencing_entity_B_does_not_affect_refCount_of_B() {
        final TgCategory cat3 = co$(TgCategory.class).findByKeyAndFetch(fetchAll(TgCategory.class), "Cat3");
        final TgCategory oldParent = cat3.getParent();
        assertEquals(Integer.valueOf(0), cat3.getRefCount());
        assertEquals(Integer.valueOf(2), oldParent.getRefCount());

        final TgCategory savedCat3 = save(cat3.setParent(null).setActive(true));
        assertNull(savedCat3.getParent());
        assertTrue(savedCat3.isActive());
        assertEquals("RefCount should not change", cat3.getRefCount(), savedCat3.getRefCount());
        assertEquals("RefCount of the de-referenced non-dirty activatable should have not changed.",
                oldParent.getRefCount(),
                co$(TgCategory.class).findByKeyAndFetch(fetchAll(TgCategory.class), "Cat1").getRefCount());
    }

    @Test
    public void deactivation_of_an_entity_decrements_refCount_of_referenced_active_entities() {
        final var cat2 = co$(TgCategory.class).findByKeyAndFetch(fetchAll(TgCategory.class), "Cat2");
        assertTrue(cat2.isActive());
        final var oldParent = cat2.getParent();
        assertTrue(oldParent.isActive());
        assertThat(oldParent.getRefCount()).isPositive();

        final var savedCat2 = save(cat2.setActive(false));
        assertFalse(savedCat2.isActive());
        assertEquals(oldParent.getRefCount() - 1, savedCat2.getParent().getRefCount() + 0);
    }

    @Test
    public void concurrent_deactivation_of_the_same_entity_decrements_refCount_of_referenced_active_entities_only_once() {
        final var co$Category = co$(TgCategory.class);

        {
            final var catA = save(new_(TgCategory.class, "A").setActive(true));
            save(new_(TgCategory.class, "B").setActive(true).setParent(catA));
            save(new_(TgCategory.class, "C").setActive(true).setParent(catA));
        }

        final var catA = co$Category.findByKey("A");
        assertEquals(Integer.valueOf(2), catA.getRefCount());

        final var catB_1 = co$Category.findByKey("B").setActive(false);
        final var catB_2 = co$Category.findByKey("B").setActive(false);

        save(catB_1);
        assertEquals(catA.getRefCount() - 1, co$Category.findByKey("A").getRefCount() + 0);

        save(catB_2);
        assertEquals(catA.getRefCount() - 1, co$Category.findByKey("A").getRefCount() + 0);
    }

    @Test
    public void concurrent_activation_of_the_same_entity_increments_refCount_of_referenced_active_entities_only_once() {
        final var co$Category = co$(TgCategory.class);

        {
            final var catA = save(new_(TgCategory.class, "A").setActive(true));
            save(new_(TgCategory.class, "B").setActive(false).setParent(catA));
        }

        final var catA = co$Category.findByKey("A");
        assertEquals(Integer.valueOf(0), catA.getRefCount());

        final var catB_1 = co$Category.findByKey("B").setActive(true);
        final var catB_2 = co$Category.findByKey("B").setActive(true);

        save(catB_1);
        assertEquals(catA.getRefCount() + 1, co$Category.findByKey("A").getRefCount() + 0);

        save(catB_2);
        assertEquals(catA.getRefCount() + 1, co$Category.findByKey("A").getRefCount() + 0);
    }

    @Test
    public void changing_activatable_properties_leads_to_decrement_of_dereferenced_instances_and_increment_of_just_referenced_ones() {
        final TgSystem sys1 = co$(TgSystem.class).findByKeyAndFetch(fetchAll(TgSystem.class), "Sys1");
        final TgCategory cat1BeforeChange = sys1.getCategory();
        assertEquals(Integer.valueOf(2), cat1BeforeChange.getRefCount());
        final TgCategory cat6 = co$(TgCategory.class).findByKeyAndFetch(fetchAll(TgCategory.class), "Cat6");

        final TgSystem savedSys1 = save(sys1.setCategory(cat6));
        assertEquals(cat1BeforeChange.getRefCount() - 1, co$(TgCategory.class).findByKey("Cat1").getRefCount() + 0);
        assertEquals(cat6.getRefCount() + 1, savedSys1.getCategory().getRefCount() + 0);
    }

    @Test
    public void non_activatable_entities_do_not_effect_ref_count_of_referenced_activatables() {
        final TgSubSystem subSys1 = co$(TgSubSystem.class).findByKeyAndFetch(fetchAll(TgSubSystem.class), "SubSys1");
        final TgCategory cat6BeforeChange = subSys1.getFirstCategory();
        assertEquals(Integer.valueOf(2), cat6BeforeChange.getRefCount());
        final TgCategory cat1 = co$(TgCategory.class).findByKeyAndFetch(fetchAll(TgCategory.class), "Cat1");

        final TgSubSystem savedSubSys1 = save(subSys1.setFirstCategory(cat1).setSecondCategory(null));
        assertEquals(cat6BeforeChange.getRefCount(), co$(TgCategory.class).findByKey("Cat6").getRefCount());
        assertEquals(cat1.getRefCount(), savedSubSys1.getFirstCategory().getRefCount());
    }

    @Test
    public void changing_and_unsetting_activatable_properties_leads_to_decrement_of_dereferenced_instances_and_increment_of_just_referenced_ones() {
        final TgSystem sys2 = co$(TgSystem.class).findByKeyAndFetch(fetchAll(TgSystem.class), "Sys2");
        final TgCategory cat6BeforeChange = sys2.getFirstCategory();
        assertEquals(Integer.valueOf(2), cat6BeforeChange.getRefCount());
        final TgCategory cat1 = co$(TgCategory.class).findByKeyAndFetch(fetchAll(TgCategory.class), "Cat1");

        final TgSystem savedSys2 = save(sys2.setFirstCategory(cat1).setSecondCategory(null));
        assertEquals(cat6BeforeChange.getRefCount() - 2, co$(TgCategory.class).findByKey("Cat6").getRefCount() + 0);
        assertEquals(cat1.getRefCount() + 1, savedSys2.getFirstCategory().getRefCount() + 0);
    }

    @Test
    public void self_referenced_activatable_can_become_inactive() {
        final TgCategory cat5 = co$(TgCategory.class).findByKeyAndFetch(fetchAll(TgCategory.class), "Cat5");
        final TgCategory savedCat5 = save(cat5.setActive(false));

        assertFalse(savedCat5.isActive());
        assertEquals(savedCat5, savedCat5.getParent());
        assertEquals(Integer.valueOf(0), savedCat5.getRefCount());
    }

    @Test
    public void deactivation_with_simultaneous_dereferencing_of_active_activatables_decrements_refCount_of_dereferenced_active_activatables() {
        final TgCategory cat2 = co$(TgCategory.class).findByKeyAndFetch(fetchAll(TgCategory.class), "Cat2");
        final TgCategory cat1 = cat2.getParent();
        final TgCategory savedCat2 = save(cat2.setParent(null).setActive(false));

        assertFalse(savedCat2.isActive());
        assertNull(savedCat2.getParent());
        assertEquals(cat1.getRefCount() - 1, co$(TgCategory.class).findByKey("Cat1").getRefCount() + 0);
    }


    @Test
    public void concurrent_referencing_by_new_entity_of_activatable_that_has_just_became_inactive_is_prevented() {
        final var cat7 = co$(TgCategory.class).findByKeyAndFetch(fetchAll(TgCategory.class), "Cat7");
        assertTrue(cat7.isActive());

        final var sys3 = new_(TgSystem.class, "Sys3").setActive(true).setFirstCategory(cat7);

        // Concurrent deactivation of Cat7 just referenced by Sys3.
        save(co$(TgCategory.class).findByEntityAndFetch(fetchAll(TgCategory.class), cat7)
                     .setActive(false));

        assertThatThrownBy(() -> save(sys3))
                .hasMessage(ERR_ENTITY_EXISTS_BUT_NOT_ACTIVE.formatted(getEntityTitleAndDesc(TgCategory.class).getKey(), cat7));
    }

    @Test
    public void concurrent_referencing_by_persisted_entity_of_activatable_that_has_just_became_inactive_is_prevented() {
        final var cat7 = co$(TgCategory.class).findByKeyAndFetch(fetchAll(TgCategory.class), "Cat7");
        assertTrue(cat7.isActive());

        final var sys3 = save(new_(TgSystem.class, "Sys3").setActive(true))
                .setFirstCategory(cat7);

        save(co$(TgCategory.class)
                     .findByEntityAndFetch(fetchAll(TgCategory.class), cat7)
                     .setActive(false));

        assertThatThrownBy(() -> save(sys3))
                .hasMessage(ERR_ENTITY_EXISTS_BUT_NOT_ACTIVE.formatted(getEntityTitleAndDesc(TgCategory.class).getKey(), cat7));
    }

    @Test
    public void refCount_value_is_equal_to_number_of_references_from_entities_in_different_properties_rather_than_to_number_of_such_entities() {
        final TgCategory cat = save(new_(TgCategory.class, "NEW_CAT").setActive(true));
        
        // set properties one by one and assert refCount increasing
        TgSystem sys = save(new_(TgSystem.class, "NEW_SYS").setActive(true).setFirstCategory(cat));
        
        assertEquals(Integer.valueOf(1), co$(TgCategory.class).findByKey(cat.getKey()).getRefCount());
        
        sys = save(sys.setSecondCategory(cat));
        
        assertEquals(Integer.valueOf(2), co$(TgCategory.class).findByKey(cat.getKey()).getRefCount());
        
        // unset properties one by one and assert refCount decreasing
        sys = save(sys.setFirstCategory(null));
        
        assertEquals(Integer.valueOf(1), co$(TgCategory.class).findByKey(cat.getKey()).getRefCount());
        
        sys = save(sys.setSecondCategory(null));
        
        assertEquals(Integer.valueOf(0), co$(TgCategory.class).findByKey(cat.getKey()).getRefCount());

        // set two properties at once and then deactive the entity to assert that refCount becomes zero
        sys = save(sys.setFirstCategory(cat).setSecondCategory(cat));
        assertEquals(Integer.valueOf(2), co$(TgCategory.class).findByKey(cat.getKey()).getRefCount());
        sys = save(sys.setActive(false));
        assertEquals(Integer.valueOf(0), co$(TgCategory.class).findByKey(cat.getKey()).getRefCount());
    }

    @Test
    public void recomputation_of_refCount_after_multiple_changes_of_activatable_property_with_referencing_is_performed_for_correct_instances() {
        final TgSystem sys1 = co$(TgSystem.class).findByKeyAndFetch(fetchAll(TgSystem.class), "Sys1");

        final TgCategory cat1 = co$(TgCategory.class).findByKeyAndFetch(fetchAll(TgCategory.class), "Cat1");
        assertEquals(Integer.valueOf(2), cat1.getRefCount());
        final TgCategory cat5 = co$(TgCategory.class).findByKeyAndFetch(fetchAll(TgCategory.class), "Cat5");
        assertEquals(Integer.valueOf(0), cat5.getRefCount());        
        final TgCategory cat6 = co$(TgCategory.class).findByKeyAndFetch(fetchAll(TgCategory.class), "Cat6");
        assertEquals(Integer.valueOf(2), cat6.getRefCount());

        final MetaProperty<TgCategory> mpCategory = sys1.getProperty("category");
        assertEquals(cat1, mpCategory.getValue());
        assertEquals(cat1, mpCategory.getPrevValue());
        assertEquals(cat1, mpCategory.getOriginalValue());
        
        sys1.setCategory(cat6); // intermediate category, which will be replaced before saving changes
        assertEquals(cat6, mpCategory.getValue());
        assertEquals(cat1, mpCategory.getPrevValue());
        assertEquals(cat1, mpCategory.getOriginalValue());

        sys1.setCategory(cat5);
        assertEquals(cat5, mpCategory.getValue());
        assertEquals(cat6, mpCategory.getPrevValue());
        assertEquals(cat1, mpCategory.getOriginalValue());

        final TgSystem savedSys1 = save(sys1);
        final MetaProperty<TgCategory> savedMpCategory = savedSys1.getProperty("category");
        assertEquals(cat5, savedMpCategory.getValue());
        assertEquals(cat5, savedMpCategory.getPrevValue());
        assertEquals(cat5, savedMpCategory.getOriginalValue());
 
        // how about refCount values?
        assertEquals(cat1.getRefCount() - 1, co$(TgCategory.class).findByKey("Cat1").getRefCount() + 0);
        assertEquals(cat5.getRefCount() + 1, co$(TgCategory.class).findByKey("Cat5").getRefCount() + 0);
        assertEquals("RefCount should not have been changed for this intermediate value.", cat6.getRefCount() + 0, co$(TgCategory.class).findByKey("Cat6").getRefCount() + 0);
    }

    @Test
    public void recomputation_of_refCount_after_mutiple_changes_of_activatable_property_with_dereferencing_is_performed_for_correct_instances() {
        final TgSystem sys1 = co$(TgSystem.class).findByKeyAndFetch(fetchAll(TgSystem.class), "Sys1");

        final TgCategory cat1 = co$(TgCategory.class).findByKeyAndFetch(fetchAll(TgCategory.class), "Cat1");
        assertEquals(Integer.valueOf(2), cat1.getRefCount());
        final TgCategory cat6 = co$(TgCategory.class).findByKeyAndFetch(fetchAll(TgCategory.class), "Cat6");
        assertEquals(Integer.valueOf(2), cat6.getRefCount());

        final MetaProperty<TgCategory> mpCategory = sys1.getProperty("category");
        assertEquals(cat1, mpCategory.getValue());
        assertEquals(cat1, mpCategory.getPrevValue());
        assertEquals(cat1, mpCategory.getOriginalValue());
        
        sys1.setCategory(cat6); // intermediate category, which will be dereferenced before saving changes
        assertEquals(cat6, mpCategory.getValue());
        assertEquals(cat1, mpCategory.getPrevValue());
        assertEquals(cat1, mpCategory.getOriginalValue());

        sys1.setCategory(null);
        assertNull(mpCategory.getValue());
        assertEquals(cat6, mpCategory.getPrevValue());
        assertEquals(cat1, mpCategory.getOriginalValue());

        final TgSystem savedSys1 = save(sys1);
        final MetaProperty<TgCategory> savedMpCategory = savedSys1.getProperty("category");
        assertNull(savedMpCategory.getValue());
        assertNull(savedMpCategory.getPrevValue());
        assertNull(savedMpCategory.getOriginalValue());
 
        // how about refCount values?
        assertEquals(cat1.getRefCount() - 1, co$(TgCategory.class).findByKey("Cat1").getRefCount() + 0);
        assertEquals("RefCount should not have been changed for this intermediate value.", cat6.getRefCount() + 0, co$(TgCategory.class).findByKey("Cat6").getRefCount() + 0);
    }

    @Test
    public void recomputation_of_refCount_after_mutiple_changes_of_activatable_property_that_had_no_value_before_is_performed_for_correct_instances() {
        final TgSystem sys2 = co$(TgSystem.class).findByKeyAndFetch(fetchAll(TgSystem.class), "Sys2");
        assertNull(sys2.getCategory());

        final TgCategory cat1 = co$(TgCategory.class).findByKeyAndFetch(fetchAll(TgCategory.class), "Cat1");
        assertEquals(Integer.valueOf(2), cat1.getRefCount());
        final TgCategory cat6 = co$(TgCategory.class).findByKeyAndFetch(fetchAll(TgCategory.class), "Cat6");
        assertEquals(Integer.valueOf(2), cat6.getRefCount());

        final MetaProperty<TgCategory> mpCategory = sys2.getProperty("category");
        assertNull(mpCategory.getValue());
        assertNull(mpCategory.getPrevValue());
        assertNull(mpCategory.getOriginalValue());

        sys2.setCategory(cat6); // intermediate category, which will be dereferenced before saving changes
        assertEquals(cat6, mpCategory.getValue());
        assertNull(mpCategory.getPrevValue());
        assertNull(mpCategory.getOriginalValue());

        sys2.setCategory(cat1);
        assertEquals(cat1, mpCategory.getValue());
        assertEquals(cat6, mpCategory.getPrevValue());
        assertNull(mpCategory.getOriginalValue());

        final TgSystem savedSys2 = save(sys2);
        final MetaProperty<TgCategory> savedMpCategory = savedSys2.getProperty("category");
        assertEquals(cat1, savedMpCategory.getValue());
        assertEquals(cat1, savedMpCategory.getPrevValue());
        assertEquals(cat1, savedMpCategory.getOriginalValue());

        // how about refCount values?
        assertEquals(cat1.getRefCount() + 1, co$(TgCategory.class).findByKey("Cat1").getRefCount() + 0);
        assertEquals("RefCount should not have been changed for this intermediate value.", cat6.getRefCount() + 0, co$(TgCategory.class).findByKey("Cat6").getRefCount() + 0);
    }

    @Test
    public void refCount_for_dereferenced_entity_is_decremented_and_incremented_for_referened_entity_only_once_upon_concurrent_modification_of_the_same_entity() {
        final TgSystem sys1User1 = co$(TgSystem.class).findByKeyAndFetch(fetchAll(TgSystem.class), "Sys1");
        final TgSystem sys1User2 = co$(TgSystem.class).findByKeyAndFetch(fetchAll(TgSystem.class), "Sys1");

        final TgCategory cat1 = co$(TgCategory.class).findByKeyAndFetch(fetchAll(TgCategory.class), "Cat1");
        assertEquals(Integer.valueOf(2), cat1.getRefCount());
        final TgCategory cat5 = co$(TgCategory.class).findByKeyAndFetch(fetchAll(TgCategory.class), "Cat5");
        assertEquals(Integer.valueOf(0), cat5.getRefCount());

        save(sys1User1.setCategory(cat5));
        save(sys1User2.setCategory(cat5)); // concurrent, non-conflicting change

        // how about refCount values?
        assertEquals(cat1.getRefCount() - 1, co$(TgCategory.class).findByKey("Cat1").getRefCount() + 0);
        assertEquals(cat5.getRefCount() + 1, co$(TgCategory.class).findByKey("Cat5").getRefCount() + 0);
    }

    @Test
    public void refCount_for_dereferenced_entity_is_decremented_only_once_upon_concurrent_dereferencing_from_the_same_entity() {
        final TgSystem sys1User1 = co$(TgSystem.class).findByKeyAndFetch(fetchAll(TgSystem.class), "Sys1");
        final TgSystem sys1User2 = co$(TgSystem.class).findByKeyAndFetch(fetchAll(TgSystem.class), "Sys1");

        final TgCategory cat1 = co$(TgCategory.class).findByKeyAndFetch(fetchAll(TgCategory.class), "Cat1");
        assertEquals(Integer.valueOf(2), cat1.getRefCount());

        save(sys1User1.setCategory(null));
        save(sys1User2.setCategory(null)); // concurrent, non-conflicting change

        // how about refCount values?
        assertEquals(cat1.getRefCount() - 1, co$(TgCategory.class).findByKey("Cat1").getRefCount() + 0);
    }

    @Test
    public void refCount_for_referenced_entity_is_incremented_only_once_upon_concurrent_modification_of_the_same_entity() {
        final TgSystem sys2User1 = co$(TgSystem.class).findByKeyAndFetch(fetchAll(TgSystem.class), "Sys2");
        final TgSystem sys2User2 = co$(TgSystem.class).findByKeyAndFetch(fetchAll(TgSystem.class), "Sys2");

        final TgCategory cat1 = co$(TgCategory.class).findByKeyAndFetch(fetchAll(TgCategory.class), "Cat1");
        assertEquals(Integer.valueOf(2), cat1.getRefCount());

        assertNull(sys2User1.getCategory());
        save(sys2User1.setCategory(cat1));
        assertNull(sys2User2.getCategory());
        save(sys2User2.setCategory(cat1)); // concurrent, non-conflicting change

        // how about refCount values?
        assertEquals(cat1.getRefCount() + 1, co$(TgCategory.class).findByKey("Cat1").getRefCount() + 0);
    }

    @Test
    public void new_entity_is_validated_upon_activation() {
        final TgCategory cat = save(new_(TgCategory.class, "InactiveCat").setActive(false));
        final TgSystem sys = new_(TgSystem.class, "Sys").setCategory(cat);
        assertFalse(sys.isActive());
        assertTrue(sys.isValid().isSuccessful());

        sys.setActive(true);
        final MetaProperty<Boolean> mpActive = sys.getProperty(ACTIVE);
        assertFalse(mpActive.isValid());
        assertEquals("Property [Category] in Tg System [Sys] references inactive Tg Category [InactiveCat].",
                     mpActive.getFirstFailure().getMessage());
    }

    @Test
    public void stale_entity_can_be_deactivated() {
        final var cat7 = co$(TgCategory.class).findByKeyAndFetch(fetchAll(TgCategory.class), "Cat7");
        assertTrue(cat7.isActive());
        final var cat7_1 = save(cat7.setDesc(cat7.getDesc() + " some change."));
        assertThat(cat7.getVersion()).isLessThan(cat7_1.getVersion());
        assertThat(cat7_1.getRefCount()).isEqualTo(0);

        // deactivate stale instance
        final var cat7_2 = save(cat7.setActive(false));
        assertFalse(cat7_2.isActive());
    }

    @Test
    public void refCount_of_referenced_active_entity_remains_unchanged_after_inactive_entity_dereferences_it_and_new_value_is_null() {
        final var co$Category = co$(TgCategory.class);

        {
            final var catA = save(new_(TgCategory.class, "A").setActive(true));
            save(new_(TgCategory.class, "B").setActive(true).setParent(catA));
            save(new_(TgCategory.class, "C").setActive(false).setParent(catA));
        }

        {
            final var catA = co$Category.findByKey("A");
            assertTrue(catA.isActive());
            assertThat(catA.getRefCount()).isEqualTo(1);
        }

        save(co$Category.findByKey("C").setParent(null));

        {
            final var catA = co$Category.findByKey("A");
            assertTrue(catA.isActive());
            assertThat(catA.getRefCount()).isEqualTo(1);
        }
    }

    @Test
    public void refCount_of_referenced_active_entity_remains_unchanged_after_inactive_entity_dereferences_it_and_new_value_is_not_null() {
        final var co$Category = co$(TgCategory.class);

        {
            final var catA = save(new_(TgCategory.class, "A").setActive(true));
            save(new_(TgCategory.class, "B").setActive(true).setParent(catA));
            save(new_(TgCategory.class, "C").setActive(false).setParent(catA));
        }

        {
            final var catA = co$Category.findByKey("A");
            assertTrue(catA.isActive());
            assertThat(catA.getRefCount()).isEqualTo(1);
        }

        save(co$Category.findByKeyAndFetch(fetch(TgCategory.class).with("parent"), "C")
                     .setParent(co$Category.findByKey("B")));

        {
            final var catA = co$Category.findByKey("A");
            assertTrue(catA.isActive());
            assertThat(catA.getRefCount()).isEqualTo(1);
        }
    }

    @Test
    public void dereferencing_with_concurrent_deactivation_of_the_referencing_entity_decrements_refCount_only_once_when_new_value_is_null() {
        final var co$Category = co$(TgCategory.class);

        {
            final var catA = save(new_(TgCategory.class, "A").setActive(true));
            save(new_(TgCategory.class, "B").setActive(true).setParent(catA));
            save(new_(TgCategory.class, "C").setActive(true).setParent(catA));
        }

        {
            final var catA = co$Category.findByKey("A");
            assertTrue(catA.isActive());
            assertThat(catA.getRefCount()).isEqualTo(2);
        }

        final var catB_1 = co$Category.findByKey("B").setActive(false);
        final var catB_2 = co$Category.findByKey("B").setParent(null);

        {
            final var catB_1_saved = save(catB_1);
            assertFalse(catB_1_saved.isActive());
            final var catA = co$Category.findByKey("A");
            assertTrue(catA.isActive());
            assertThat(catA.getRefCount()).isEqualTo(1);
            assertEquals(catB_1_saved.getParent(), catA);
        }

        {
            final var catB_2_saved = save(catB_2);
            assertFalse(catB_2_saved.isActive());
            assertNull(catB_2_saved.getParent());
            final var catA = co$Category.findByKey("A");
            assertTrue(catA.isActive());
            assertThat(catA.getRefCount()).isEqualTo(1);
        }
    }

    @Test
    public void dereferencing_with_concurrent_deactivation_of_the_referencing_entity_decrements_refCount_only_once_when_new_value_is_not_null() {
        final var co$Category = co$(TgCategory.class);

        final TgCategory catC;
        {
            final var catA = save(new_(TgCategory.class, "A").setActive(true));
            save(new_(TgCategory.class, "B").setActive(true).setParent(catA));
            catC = save(new_(TgCategory.class, "C").setActive(true).setParent(catA));
            assertThat(catC.getRefCount()).isEqualTo(0);
        }

        {
            final var catA = co$Category.findByKey("A");
            assertTrue(catA.isActive());
            assertThat(catA.getRefCount()).isEqualTo(2);
        }

        final var catB_1 = co$Category.findByKey("B").setActive(false);
        final var catB_2 = co$Category.findByKeyAndFetch(fetch(TgCategory.class).with("parent"), "B")
                .setParent(catC);

        {
            final var catB_1_saved = save(catB_1);
            assertFalse(catB_1_saved.isActive());
            final var catA = co$Category.findByKey("A");
            assertTrue(catA.isActive());
            assertThat(catA.getRefCount()).isEqualTo(1);
            assertEquals(catB_1_saved.getParent(), catA);
        }

        {
            final var catB_2_saved = save(catB_2);
            assertFalse(catB_2_saved.isActive());
            assertEquals(catC, catB_2_saved.getParent());
            final var catA = co$Category.findByKey("A");
            assertTrue(catA.isActive());
            assertThat(catA.getRefCount()).isEqualTo(1);
            final var catC_after_being_referenced = co$Category.findByKey("C");
            assertTrue(catC_after_being_referenced.isActive());
            // catB_2 referenced catC while active, but saving happened after saving catB_1, which deactivated "B"
            // Hence, no change to refCount of "C" is expected.
            assertThat(catC_after_being_referenced.getRefCount()).isEqualTo(0);
        }
    }

    @Test
    public void if_entity_B_is_concurrently_deactivated_before_it_begins_referencing_entity_A_then_refCount_of_A_is_not_affected() {
        final var co$Category = co$(TgCategory.class);

        final var catA = save(new_(TgCategory.class, "A").setActive(true));
        save(new_(TgCategory.class, "B").setActive(true));

        final var catB_1 = co$Category.findByKey("B").setActive(false);
        final var catB_2 = co$Category.findByKeyAndFetch(fetchAll(TgCategory.class).with("parent"), "B")
                .setParent(co$Category.findByKey("A"));

        {
            final var catB_1_saved = save(catB_1);
            assertFalse(catB_1_saved.isActive());
            assertNull(catB_1_saved.getParent());
        }

        {
            final var catB_2_saved = save(catB_2);
            assertFalse(catB_2_saved.isActive());
            final var catA_v1 = co$Category.findByKey("A");
            assertEquals(catA_v1, catB_2_saved.getParent());
            assertTrue(catA_v1.isActive());
            assertEquals(catA.getRefCount(), catA_v1.getRefCount());
        }
    }

    @Test
    public void when_a_new_entity_A_begins_referencing_entity_B_refCount_of_B_is_incremented_for_each_reference_from_A() {
        final var catA = save(new_(TgCategory.class, "A").setActive(true));
        final var sysA = save(new_(TgSystem.class, "A").setActive(true).setCategory(catA).setFirstCategory(catA));
        final var coCategory = co$(TgCategory.class);
        assertEquals(Integer.valueOf(2), coCategory.findByKey("A").getRefCount());
    }

    @Test
    public void when_a_modified_entity_A_begins_referencing_entity_B_refCount_of_B_is_incremented_for_each_reference_from_A() {
        final var catA = save(new_(TgCategory.class, "A").setActive(true));
        final var sysA = save(save(new_(TgSystem.class, "A").setActive(true))
                                      .setCategory(catA).setFirstCategory(catA));
        final var coCategory = co$(TgCategory.class);
        assertEquals(Integer.valueOf(2), coCategory.findByKey("A").getRefCount());
    }

    @Test
    public void when_entity_A_is_activated_refCount_of_B_is_incremented_for_each_reference_from_A() {
        final var co$Category = co$(TgCategory.class);

        final var catA = save(new_(TgCategory.class, "A").setActive(true));
        final var sysA = save(new_(TgSystem.class, "A").setActive(false).setCategory(catA).setFirstCategory(catA));
        assertThat(co$Category.findByKey("A").getRefCount()).isEqualTo(0);
        save(sysA.setActive(true));
        assertThat(co$Category.findByKey("A").getRefCount()).isEqualTo(2);
    }

    @Test
    public void when_entity_A_is_deactivated_refCount_of_B_is_decremented_for_each_reference_from_A() {
        final var co$Category = co$(TgCategory.class);

        final var catA = save(new_(TgCategory.class, "A").setActive(true));
        save(new_(TgCategory.class, "X").setActive(true).setParent(catA));
        final var sysA = save(new_(TgSystem.class, "A").setActive(true).setCategory(catA).setFirstCategory(catA));
        assertThat(co$Category.findByKey("A").getRefCount()).isEqualTo(3);
        save(sysA.setActive(false));
        assertThat(co$Category.findByKey("A").getRefCount()).isEqualTo(1);
    }

    @Override
    protected void populateDomain() {
        super.populateDomain();
        
        // set up logged in person, which is needed for TgSubSystem
        final String loggedInUser = "LOGGED_IN_USER";
        final IUser coUser = co$(User.class);
        final User lUser = coUser.save(new_(User.class, loggedInUser).setBase(true).setEmail(loggedInUser + "@unit-test.software").setActive(true));

        // associate the admin role with lUser
        final UserRole admin = co$(UserRole.class).findByKey(UNIT_TEST_ROLE);
        save(new_composite(UserAndRoleAssociation.class, lUser, admin));

        save(new_(TgPerson.class, loggedInUser).setUser(lUser));

        final IUserProvider up = getInstance(IUserProvider.class);
        up.setUsername(loggedInUser, getInstance(IUser.class));

        // now the test data
        TgCategory cat1 = save(new_(TgCategory.class, "Cat1").setActive(true));
        cat1 = save(cat1.setParent(cat1));
        final TgCategory cat2 = save(new_(TgCategory.class, "Cat2").setActive(true).setParent(cat1));
        final TgCategory cat3 = save(new_(TgCategory.class, "Cat3").setActive(false).setParent(cat1));
        save(new_(TgCategory.class, "Cat4").setActive(false).setParent(cat3));
        final TgCategory cat5 = save(new_(TgCategory.class, "Cat5").setActive(true));
        save(cat5.setParent(cat5));

        save(new_(TgSystem.class, "Sys1").setActive(true).setCategory(cat1));
        final TgCategory cat6 = save(new_(TgCategory.class, "Cat6").setActive(true));
        final TgCategory cat7 = save(new_(TgCategory.class, "Cat7").setActive(true));
        save(new_(TgSubSystem.class, "SubSys1").setFirstCategory(cat6).setSecondCategory(cat6));

        save(new_(TgSystem.class, "Sys2").setActive(true).setFirstCategory(cat6).setSecondCategory(cat6));
    }

}
