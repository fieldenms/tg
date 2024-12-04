package ua.com.fielden.platform.entity.activatable;

import org.junit.Test;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.ActivatableAbstractEntity;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.sample.domain.*;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.utils.Validators;

import java.util.List;

import static java.util.stream.Collectors.toSet;
import static org.junit.Assert.*;
import static ua.com.fielden.platform.entity.ActivatableAbstractEntity.ACTIVE;

public class HandlingOfDeactivatableDependenciesTest extends AbstractDaoTestCase {

    @Test
    public void active_person_with_active_authoriser_and_originator_deactivatable_dependencies_and_one_tangenital_dependency_has_one_ref_count() {
        final TgPerson person = co$(TgPerson.class).findByKey("P1");

        assertEquals(Integer.valueOf(1), person.getRefCount());
    }

    @Test
    public void active_person_with_active_authoriser_and_inactive_originator_deactivatable_dependencies_has_zero_ref_count() {
        final TgPerson person = co$(TgPerson.class).findByKey("P2");

        assertEquals(Integer.valueOf(0), person.getRefCount());
    }

    @Test
    public void deactivating_authoriser_that_references_active_person_does_not_change_its_ref_count() {
        final TgPerson person = co$(TgPerson.class).findByKey("P1");
        assertEquals(Integer.valueOf(1), person.getRefCount());
        final TgAuthoriser auth = co$(TgAuthoriser.class).findByKey(person);

        save(auth.setActive(false));

        assertEquals(Integer.valueOf(1), co$(TgPerson.class).findByKey("P1").getRefCount());
    }

    @Test
    public void there_suppose_to_be_two_deactivatable_dependencies_for_person_P1() {
        final TgPerson p1 = co$(TgPerson.class).findByKey("P1");

        final List<? extends ActivatableAbstractEntity<?>> deps = Validators.findActiveDeactivatableDependencies(p1, getInstance(ICompanionObjectFinder.class));

        assertEquals(2, deps.size());
        assertTrue(deps.stream().map(AbstractEntity::getId).collect(toSet()).contains(co$(TgAuthoriser.class).findByKey(p1).getId()));
        assertTrue(deps.stream().map(AbstractEntity::getId).collect(toSet()).contains(co$(TgOriginator.class).findByKey(p1).getId()));

        final TgPerson p3 = co$(TgPerson.class).findByKey("P3");
        assertFalse(deps.stream().map(AbstractEntity::getId).collect(toSet()).contains(co$(TgOriginator.class).findByKey(p3).getId()));
    }

    @Test
    public void there_suppose_to_be_one_deactivatable_dependencies_for_person_P2() {
        final TgPerson person = co$(TgPerson.class).findByKey("P2");

        final List<? extends ActivatableAbstractEntity<?>> deps = Validators.findActiveDeactivatableDependencies(person, getInstance(ICompanionObjectFinder.class));

        assertEquals(1, deps.size());
        assertTrue(deps.stream().map(AbstractEntity::getId).collect(toSet()).contains(co$(TgAuthoriser.class).findByKey(person).getId()));
    }

    @Test
    public void deactivation_of_person_with_only_active_authoriser_and_originator_deactivatable_dependencies_is_permitted() {
        final TgPerson person = co$(TgPerson.class).findByKey("P2");

        final TgPerson savedPerson = save(person.setActive(false));
        assertFalse(savedPerson.isActive());
    }

    @Test
    public void deactivation_of_person_with_deactivatable_dependencies_that_is_not_part_of_a_key_is_prevented() {
        final TgPerson person = co$(TgPerson.class).findByKey("P1");

        try {
            save(person.setActive(false));
            fail();
        } catch (final Result ex) {
            assertTrue(ex.getMessage().startsWith("Tg Person [P1] has 1 active dependency."));
        }
    }

    @Test
    public void deactivating_originator_that_references_two_active_persons_should_not_change_ref_count_for_one_where_it_is_dependable_and_changes_for_the_other() {
        final TgPerson p3 = co$(TgPerson.class).findByKey("P3");
        assertEquals(Integer.valueOf(0), p3.getRefCount());
        final TgOriginator orig = co$(TgOriginator.class).findByKey(p3);

        save(orig.setActive(false));

        assertEquals("P1 was referenced by just deactivated originator as an assistant, and should have its refCount decremented.", Integer.valueOf(0), co$(TgPerson.class).findByKey("P1").getRefCount());
        assertEquals("P3 was referenced by just deactivated originator as one of key members, and should not have its refCount effected.",Integer.valueOf(0), co$(TgPerson.class).findByKey("P3").getRefCount());
    }

    @Test
    public void activating_originator_that_references_two_active_persons_does_not_change_ref_count_for_one_where_it_is_dependable_and_does_change_for_the_other() {
        final TgPerson p2 = co$(TgPerson.class).findByKey("P2");
        final TgOriginator orig = co$(TgOriginator.class).findByKey(p2);

        save(orig.setActive(true));

        assertEquals("P1 is referenced by just activated originator as an assistant, and should have its refCount incremented.", Integer.valueOf(2), co$(TgPerson.class).findByKey("P1").getRefCount());
        assertEquals("P2 is referenced by just activated originator as one of key members, and should not have its refCount effected.",Integer.valueOf(0), co$(TgPerson.class).findByKey("P2").getRefCount());
    }

    @Test
    public void deactivation_of_person_p3_with_originators_assistant_as_p1_does_decrement_p1s_ref_count() {
        final TgPerson p1 = co$(TgPerson.class).findByKey("P1");
        assertEquals("Test pre condition should validate", Integer.valueOf(1), p1.getRefCount());
        final TgPerson p3 = save(co$(TgPerson.class).findByKey("P3").setActive(false));
        assertFalse(co$(TgAuthoriser.class).findByKey(p3).isActive());
        assertFalse(co$(TgOriginator.class).findByKey(p3).isActive());
        assertEquals(Integer.valueOf(0), co$(TgPerson.class).findByKey("P1").getRefCount());
    }

    @Test
    public void activation_of_inactive_authoriser_increases_ref_count_for_referenced_category_and_decreases_it_upon_automatic_deactivation() {
        final TgPerson p3 = co$(TgPerson.class).findByKey("P3");
        final TgAuthoriser auth = co$(TgAuthoriser.class).findByKey(p3);
        assertEquals(Integer.valueOf(0), co$(TgCategory.class).findByKey("CAT1").getRefCount());

        save(auth.setActive(true));
        assertEquals(Integer.valueOf(1), co$(TgCategory.class).findByKey("CAT1").getRefCount());

        save(p3.setActive(false));
        assertFalse(co$(TgAuthoriser.class).findByKey(p3).isActive());
        assertEquals(Integer.valueOf(0), co$(TgCategory.class).findByKey("CAT1").getRefCount());
    }

    @Test
    public void transitive_deactivatable_dependencies_are_considered_when_validating_property_active() {
        final var part1 = co$(TgInventoryPart.class).findByKey("Part1");
        assertNotNull(part1);
        part1.setActive(false);
        assertTrue(part1.isActive());
        final MetaProperty<Boolean> mpActive = part1.getProperty(ACTIVE);
        assertFalse(mpActive.isValid());
        assertEquals("""
                        Inventory Part [Part1] has 3 active dependencies.<extended/>Inventory Part [Part1] has 3 active dependencies:
                        
                        <br><br><tt>Entity              Qty Property       </tt><hr>
                        <br><tt>Tg Inventory Issue    2 Inventory Bin  </tt>
                        <br><tt>Tg Inventory Issue    1 Tg Inventory   </tt>\
                        """,
                        mpActive.validationResult().getMessage());
        // let's now deactivate the culprits and try deactivating the part again
        final var issue1Part1 = co$(TgInventoryIssue.class).findByKey("Part1 01/12/2024 13:30");
        assertNotNull(issue1Part1);
        save(issue1Part1.setActive(false));
        final var issue2Part1 = co$(TgInventoryIssue.class).findByKey("Part1 04/12/2024 13:30");
        assertNotNull(issue2Part1);
        save(issue2Part1.setActive(false));

        part1.setActive(false);
        assertFalse(part1.isActive());
        final var inactivePart1 = save(part1);
        assertFalse(inactivePart1.isActive());
    }

    @Override
    protected void populateDomain() {
        super.populateDomain();
        
        final TgPerson p1 = save(new_(TgPerson.class, "P1").setActive(true));
        save(new_composite(TgAuthoriser.class, p1).setActive(true));
        save(new_composite(TgOriginator.class, p1).setActive(true));

        final TgPerson p2 = save(new_(TgPerson.class, "P2").setActive(true));
        save(new_composite(TgAuthoriser.class, p2).setActive(true));
        save(new_composite(TgOriginator.class, p2).setAssistant(p1).setActive(false));

        final TgPerson p3 = save(new_(TgPerson.class, "P3").setActive(true));
        save(new_composite(TgOriginator.class, p3).setAssistant(p1).setActive(true));
        save(new_composite(TgAuthoriser.class, p3).setActive(false).setCategory(save(new_(TgCategory.class, "CAT1").setActive(true))));

        final var part1 = save(new_composite(TgInventoryPart.class, "Part1").setActive(true).setDesc("Part 1 description"));
        final var invPart1 = save(new_composite(TgInventory.class, part1).setActive(true));
        final var invBinPart1 = save(new_composite(TgInventoryBin.class, invPart1).setActive(true));
        final var issue1Part1 = save(new_composite(TgInventoryIssue.class, invBinPart1, date("2024-12-01 13:30:00")).setQty(1).setSupersededInventory(invPart1).setActive(true));
        final var issue2Part1 = save(new_composite(TgInventoryIssue.class, invBinPart1, date("2024-12-04 13:30:00")).setQty(10).setActive(true));
    }

}
