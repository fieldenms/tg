package ua.com.fielden.platform.entity.activatable;

import org.junit.Test;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.activatable.test_entities.Member1;
import ua.com.fielden.platform.entity.activatable.test_entities.Member5;
import ua.com.fielden.platform.entity.activatable.test_entities.MemberDetails;
import ua.com.fielden.platform.entity.activatable.test_entities.Union;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.sample.domain.*;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.utils.Validators;

import static java.lang.String.format;
import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static ua.com.fielden.platform.entity.ActivatableAbstractEntity.ACTIVE;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetch;
import static ua.com.fielden.platform.entity.validation.ActivePropertyValidator.ERR_INACTIVE_REFERENCES;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitleAndDesc;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getTitleAndDesc;

public class HandlingOfDeactivatableDependenciesTest extends AbstractDaoTestCase implements WithActivatabilityTestUtils {

    @Test
    public void active_person_with_active_authoriser_and_originator_deactivatable_dependencies_and_one_tangential_dependency_has_one_ref_count() {
        final var person = co$(TgPerson.class).findByKey("P1");
        assertRefCount(1, person);
    }

    @Test
    public void active_person_with_active_authoriser_and_inactive_originator_deactivatable_dependencies_has_zero_ref_count() {
        final var person = co$(TgPerson.class).findByKey("P2");
        assertRefCount(0, person);
    }

    @Test
    public void deactivating_authoriser_that_references_active_person_does_not_change_its_ref_count() {
        final var person = co$(TgPerson.class).findByKey("P1");
        assertRefCount(1, person);
        final var auth = co$(TgAuthoriser.class).findByKey(person);

        save(auth.setActive(false));

        assertRefCount(1, person);
    }

    @Test
    public void deactivating_B_that_is_a_deactivatable_dependency_of_A_and_references_active_A_via_union_does_not_change_refCount_of_A() {
        final int expectedRefCount = 10;
        final var member1 = save(new_(Member1.class, "Member1").setActive(true).setRefCount(expectedRefCount));
        final var member1Det = save(new_(MemberDetails.class).setUnion(new_(Union.class).setMember1(member1)).setActive(true));

        // A sanity check to ensure that `member1.refCount` did not increase after saving a new active details record.
        assertRefCount(expectedRefCount, member1);

        // Deactivate the details record.
        save(member1Det.setActive(false));

        // Assert that refCount did not change.
        assertRefCount(expectedRefCount, member1);
    }

    @Test
    public void there_suppose_to_be_two_deactivatable_dependencies_for_person_P1() {
        final var p1 = co$(TgPerson.class).findByKey("P1");

        final var deps = Validators.findActiveDeactivatableDependencies(p1, getInstance(ICompanionObjectFinder.class));

        assertEquals(2, deps.size());
        assertTrue(deps.stream().map(AbstractEntity::getId).collect(toSet()).contains(co$(TgAuthoriser.class).findByKey(p1).getId()));
        assertTrue(deps.stream().map(AbstractEntity::getId).collect(toSet()).contains(co$(TgOriginator.class).findByKey(p1).getId()));

        final var p3 = co$(TgPerson.class).findByKey("P3");
        assertFalse(deps.stream().map(AbstractEntity::getId).collect(toSet()).contains(co$(TgOriginator.class).findByKey(p3).getId()));
    }

    @Test
    public void there_suppose_to_be_one_deactivatable_dependencies_for_person_P2() {
        final var person = co$(TgPerson.class).findByKey("P2");

        final var deps = Validators.findActiveDeactivatableDependencies(person, getInstance(ICompanionObjectFinder.class));

        assertEquals(1, deps.size());
        assertTrue(deps.stream().map(AbstractEntity::getId).collect(toSet()).contains(co$(TgAuthoriser.class).findByKey(person).getId()));
    }

    @Test
    public void deactivation_of_person_with_only_active_authoriser_and_originator_deactivatable_dependencies_is_permitted() {
        final var person = co$(TgPerson.class).findByKey("P2");

        final var savedPerson = save(person.setActive(false));
        assertFalse(savedPerson.isActive());
    }

    @Test
    public void deactivation_of_person_with_deactivatable_dependencies_that_is_not_part_of_a_key_is_prevented() {
        final var person = co$(TgPerson.class).findByKey("P1");
        person.setActive(false);
        assertThat(person.getProperty(ACTIVE).getFirstFailure())
                .hasMessageStartingWith("Tg Person [P1] has 1 active dependency.");
    }

    @Test
    public void deactivating_originator_that_references_two_active_persons_should_not_change_ref_count_for_one_where_it_is_dependable_and_changes_for_the_other() {
        final var p3 = co$(TgPerson.class).findByKey("P3");
        assertRefCount(0, p3);
        final var orig = co$(TgOriginator.class).findByKey(p3);
        final var p1 = co$(TgPerson.class).findByKey("P1");
        assertRefCount(1, p1);

        save(orig.setActive(false));

        assertRefCount("P1 was referenced by just deactivated originator as an assistant, and should have its refCount decremented.", 0, p1);
        assertRefCount("P3 was referenced by just deactivated originator as one of key members, and should not have its refCount affected.", 0, p3);
    }

    @Test
    public void deactivating_B_that_is_a_deactivatable_dependency_of_A_and_references_active_A_via_a_key_member_and_a_non_key_member_decrements_refCount_of_A_once() {
        final var member1 = save(new_(Member1.class, "Member1").setActive(true).setRefCount(10));
        final var member1Det = save(new_(MemberDetails.class)
                                            .setUnion(new_(Union.class).setMember1(member1))
                                            .setUnion2(new_(Union.class).setMember1(member1))
                                            .setActive(true));
        // And this covers activation of B -- refCount of A is incremented once.
        assertRefCount(11, member1);

        save(member1Det.setActive(false));

        assertRefCount(10, member1);
    }

    @Test
    public void activating_originator_that_references_two_active_persons_does_not_change_ref_count_for_one_where_it_is_dependable_and_does_change_for_the_other() {
        final var p2 = co$(TgPerson.class).findByKey("P2");
        assertRefCount(0, p2);
        final var orig = co$(TgOriginator.class).findByKey(p2);
        final var p1 = co$(TgPerson.class).findByKey("P1");
        assertRefCount(1, p1);

        save(orig.setActive(true));

        assertRefCount("P1 is referenced by just activated originator as an assistant, and should have its refCount incremented.", 2, p1);
        assertRefCount("P2 is referenced by just activated originator as one of key members, and should not have its refCount effected.", 0, p2);
    }

    @Test
    public void deactivation_of_person_p3_with_originators_assistant_as_p1_does_decrement_p1s_ref_count() {
        final var p1 = co$(TgPerson.class).findByKey("P1");
        assertRefCount(1, p1);
        final var p3 = save(co$(TgPerson.class).findByKey("P3").setActive(false));
        assertFalse(co$(TgAuthoriser.class).findByKey(p3).isActive());
        assertFalse(co$(TgOriginator.class).findByKey(p3).isActive());
        assertRefCount(0, p1);
    }

    @Test
    public void activation_of_inactive_authoriser_increments_ref_count_for_referenced_category_and_decrements_it_upon_automatic_deactivation() {
        final TgPerson p3 = co$(TgPerson.class).findByKey("P3");
        final TgAuthoriser auth = co$(TgAuthoriser.class).findByKey(p3);
        assertRefCount(0, TgCategory.class, "CAT1");

        save(auth.setActive(true));
        assertRefCount(1, TgCategory.class, "CAT1");

        save(p3.setActive(false));
        assertFalse(co$(TgAuthoriser.class).findByKey(p3).isActive());
        assertRefCount(0, TgCategory.class, "CAT1");
    }

    @Test
    public void inactive_authoriser_cannot_be_activated_if_its_key_person_is_inactive() {
        final var p3 = save(co$(TgPerson.class).findByKey("P3").setActive(false));
        final var auth = co$(TgAuthoriser.class).findByKey(p3);
        assertFalse(auth.isActive());

        auth.setActive(true);
        assertThat(auth.getProperty(ACTIVE).getFirstFailure())
                        .hasMessage(format(ERR_INACTIVE_REFERENCES,
                                           getTitleAndDesc("person", TgAuthoriser.class).getKey(),
                                           getEntityTitleAndDesc(auth).getKey(),
                                           auth,
                                           getEntityTitleAndDesc(p3).getKey(),
                                           p3));
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
                        
                        <br><br><span style='font-family:monospace'>Entity              Qty Property       </span><hr>
                        <br><span style='font-family:monospace'>Tg Inventory Issue    2 Inventory Bin  </span>
                        <br><span style='font-family:monospace'>Tg Inventory Issue    1 Tg Inventory   </span>""",
                        mpActive.validationResult().getMessage().replace("\r", ""));
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

    @Test
    public void deactivation_of_an_entity_deactivates_its_active_deactivatable_dependencies_that_reference_it_via_union() {
        final var member1 = save(new_(Member1.class, "Member1").setActive(true));
        final var member1Det = save(new_(MemberDetails.class).setUnion(new_(Union.class).setMember1(member1)).setActive(true));
        final var member5 = save(new_(Member5.class, "Member5").setActive(true));
        final var member5Det = save(new_(MemberDetails.class).setUnion(new_(Union.class).setMember5(member5)).setActive(true));

        // Refetch member1 after having used it (increment of its `refCount` and a concurrent deactivation is a conflicting change).
        save(refetch$(member1).setActive(false));
        assertFalse(co(MemberDetails.class).findByEntityAndFetch(fetch(MemberDetails.class), member1Det).isActive());
        assertTrue(co(MemberDetails.class).findByEntityAndFetch(fetch(MemberDetails.class), member5Det).isActive());
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
