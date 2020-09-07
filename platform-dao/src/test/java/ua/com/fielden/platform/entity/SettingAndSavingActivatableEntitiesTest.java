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

import org.junit.Test;

import ua.com.fielden.platform.dao.exceptions.EntityCompanionException;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.sample.domain.TgCategory;
import ua.com.fielden.platform.sample.domain.TgPerson;
import ua.com.fielden.platform.sample.domain.TgSubSystem;
import ua.com.fielden.platform.sample.domain.TgSystem;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.security.user.UserAndRoleAssociation;
import ua.com.fielden.platform.security.user.UserRole;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

public class SettingAndSavingActivatableEntitiesTest extends AbstractDaoTestCase {

    @Test
    public void active_entity_with_active_references_cannot_be_deactivated() {
        final TgCategory cat1 = co$(TgCategory.class).findByKey("Cat1");
        cat1.setActive(false);

        final MetaProperty<Boolean> activeProperty = cat1.getProperty("active");

        assertFalse(activeProperty.isValid());
        final String entityTitle = TitlesDescsGetter.getEntityTitleAndDesc(cat1.getType()).getKey();
        assertTrue(activeProperty.getFirstFailure().getMessage().startsWith(format("%s [%s] has %s active dependencies", entityTitle, cat1, 2)));
    }

    @Test
    public void making_inactive_activatable_entity_referencing_inactive_activatables_active_again_does_not_pass_property_validation() {
        final TgCategory cat4 = co$(TgCategory.class).findByKeyAndFetch(fetchAll(TgCategory.class), "Cat4");
        assertFalse(cat4.getParent().isActive());

        cat4.setActive(true);

        assertNotNull(cat4.getProperty(ACTIVE).getFirstFailure());
        assertEquals("Property [Selfy] in Tg Category [Cat4] references inactive Tg Category [Cat3].", 
                cat4.getProperty(ACTIVE).getFirstFailure().getMessage());
    }

    @Test
    public void inactive_activatable_entity_referencing_inactive_activatables_with_attempt_to_become_active_again_cannot_be_save() {
        final TgCategory cat4 = co$(TgCategory.class).findByKeyAndFetch(fetchAll(TgCategory.class).without("parent"), "Cat4");
        cat4.setActive(true);
        assertTrue(cat4.isValid().isSuccessful());

        try {
            co$(TgCategory.class).save(cat4);
            fail("Should have failed");
        } catch (final EntityCompanionException ex) {
            final TgCategory cat4Full = co$(TgCategory.class).findByKeyAndFetch(fetchAll(TgCategory.class), "Cat4");
            assertEquals(format("Tg Category [%s] has a reference to already inactive Tg Category [%s].", cat4Full, cat4Full.getParent()),
                    ex.getMessage());
        }
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
    public void activating_entity_that_gets_its_active_activatable_property_dereferenced_decrements_ref_counts_of_the_dereferenced_entity() {
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
    public void deactivating_entity_leads_to_decrementing_of_the_referenced_activatables() {
        final TgCategory cat2 = co$(TgCategory.class).findByKeyAndFetch(fetchAll(TgCategory.class), "Cat2");
        assertTrue(cat2.isActive());
        final TgCategory oldParent = cat2.getParent();

        final TgCategory savedCat2 = save(cat2.setActive(false));
        assertFalse(savedCat2.isActive());
        assertEquals(oldParent.getRefCount() - 1, savedCat2.getParent().getRefCount() + 0);
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
    public void deactivation_with_simultaneous_derefernesing_of_active_actiavatables_is_supported() {
        final TgCategory cat2 = co$(TgCategory.class).findByKeyAndFetch(fetchAll(TgCategory.class), "Cat2");
        final TgCategory cat1 = cat2.getParent();
        final TgCategory savedCat2 = save(cat2.setParent(null).setActive(false));

        assertFalse(savedCat2.isActive());
        assertNull(savedCat2.getParent());
        assertEquals(cat1.getRefCount() - 1, co$(TgCategory.class).findByKey("Cat1").getRefCount() + 0);
    }


    @Test
    public void concurrent_referencing_of_activatable_that_has_just_became_inactive_is_prevented() {
        final TgCategory cat7 = co$(TgCategory.class).findByKeyAndFetch(fetchAll(TgCategory.class), "Cat7");
        final TgSystem sys3 = new_(TgSystem.class, "Sys3").setActive(true).setFirstCategory(cat7);

        // let's make concurrent deactivation of just referenced cat7
        save(cat7.setActive(false));

        try {
            save(sys3);
            fail("An attempt to save successfully associated, but alread inactive activatable should fail.");
        } catch (final Result ex) {
            assertEquals("Tg Category [Cat7] exists, but is not active.", ex.getMessage());
        }
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
    public void recomputation_of_refCount_after_mutiple_changes_of_activatable_property_with_referencing_is_performed_for_correct_instances() {
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
