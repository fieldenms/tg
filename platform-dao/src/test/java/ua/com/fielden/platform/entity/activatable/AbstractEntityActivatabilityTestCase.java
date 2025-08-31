package ua.com.fielden.platform.entity.activatable;

import org.junit.Test;
import ua.com.fielden.platform.dao.exceptions.EntityCompanionException;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.ActivatableAbstractEntity;
import ua.com.fielden.platform.entity.annotation.SkipEntityExistsValidation;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.*;
import static ua.com.fielden.platform.entity.ActivatableAbstractEntity.ACTIVE;
import static ua.com.fielden.platform.entity.ActivatableAbstractEntity.REF_COUNT;
import static ua.com.fielden.platform.entity.activatable.WithActivatabilityTestUtils.setProperties;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;
import static ua.com.fielden.platform.entity.query.fluent.fetch.FetchCategory.ALL;
import static ua.com.fielden.platform.entity.validation.ActivePropertyValidator.ERR_INACTIVE_REFERENCES;
import static ua.com.fielden.platform.entity.validation.ActivePropertyValidator.ERR_SHORT_ENTITY_HAS_ACTIVE_DEPENDENCIES;
import static ua.com.fielden.platform.entity.validation.EntityExistsValidator.ERR_ENTITY_EXISTS_BUT_NOT_ACTIVE;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitleAndDesc;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getTitleAndDesc;
import static ua.com.fielden.platform.utils.EntityUtils.isActivatableEntityType;

public abstract class AbstractEntityActivatabilityTestCase extends AbstractDaoTestCase implements WithActivatabilityTestUtils {

    /// * `A` and `B` are activatable entity types.
    /// * `A` references `B` via 3 properties: `b1`, `b2`, `b3`, `b4`.
    /// * `A.b3` is annotated with [SkipEntityExistsValidation] and [SkipEntityExistsValidation#skipActiveOnly()] is `true`.
    /// * `A.b4` is annotated with [SkipEntityExistsValidation] and [SkipEntityExistsValidation#skipActiveOnly()] is `false`.
    /// * `A.a1` is a property whose type is `A` (possibility for self-reference).
    ///
    protected interface Spec1<A extends ActivatableAbstractEntity<?>, B extends ActivatableAbstractEntity<?>>
        extends ICanSetProperty
    {
        A newA();
        default A newA(CharSequence prop1, Object val1, Object... rest) {
            return setProperties(this, newA(), prop1, val1, rest);
        }
        B newB();
        default B newB(CharSequence prop1, Object val1, Object... rest) {
            return setProperties(this, newB(), prop1, val1, rest);
        }
        Class<A> aType();
        Class<B> bType();
        CharSequence A_b1();
        CharSequence A_b2();
        CharSequence A_b3();
        CharSequence A_b4();
        CharSequence A_a1();
        A setB1(A a, B b);
        A setB2(A a, B b);
        A setB3(A a, B b);
        A setB4(A a, B b);
        A setA1(A a, A a1);
    }

    protected abstract <A extends ActivatableAbstractEntity<?>, B extends ActivatableAbstractEntity<?>> Spec1<A, B> spec1();

    /// * `A` is an activatable entity type.
    /// * `B` is not an activatable entity type.
    /// * `A` references `B` via `b1`.
    ///
    protected interface Spec2<A extends ActivatableAbstractEntity<?>, B extends AbstractEntity<?>>
            extends ICanSetProperty
    {
        A newA();
        default A newA(CharSequence prop1, Object val1, Object... rest) {
            return setProperties(this, newA(), prop1, val1, rest);
        }
        B newB();
        default B newB(CharSequence prop1, Object val1, Object... rest) {
            return setProperties(this, newB(), prop1, val1, rest);
        }
        Class<A> aType();
        Class<B> bType();
        CharSequence A_b1();
        A setB1(A a, B b);
    }

    protected abstract <A extends ActivatableAbstractEntity<?>, B extends AbstractEntity<?>> Spec2<A, B> spec2();

    @Test
    public <A extends ActivatableAbstractEntity<?>, B extends ActivatableAbstractEntity<?>> void
    a_verification_of_spec1() {
        final Spec1<A, B> spec = spec1();
        assertTrue(isActivatableEntityType(spec.aType()));
        assertTrue(isActivatableEntityType(spec.bType()));
        // TODO Verify references, which may be immediate or union references.
    }

    @Test
    public <A extends ActivatableAbstractEntity<?>, B extends ActivatableAbstractEntity<?>> void
    a_verification_of_spec2() {
        final Spec2<A, B> spec = spec2();
        assertTrue(isActivatableEntityType(spec.aType()));
        assertFalse(isActivatableEntityType(spec.bType()));
        // TODO Verify references, which may be immediate or union references.
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : It is not possible for an active entity to reference an inactive one (under normal circumstances).
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    //
    @Test
    public <A extends ActivatableAbstractEntity<?>, B extends ActivatableAbstractEntity<?>> void
    new_active_A_that_references_active_B_cannot_be_saved_if_B_is_concurrently_deactivated() {
        final Spec1<A, B> spec = spec1();

        final B b = save(spec.newB(ACTIVE, true));

        final A a = spec.newA(ACTIVE, true, spec.A_b1(), b);

        // Concurrent deactivation of B just referenced by A.
        final var b1 = refetch$(b, ALL);
        b1.set(ACTIVE, false);
        save(b1);

        assertThatThrownBy(() -> save(a))
                .hasMessage(ERR_ENTITY_EXISTS_BUT_NOT_ACTIVE.formatted(getEntityTitleAndDesc(spec.bType()).getKey(), b));
    }


    @Test
    public <A extends ActivatableAbstractEntity<?>, B extends ActivatableAbstractEntity<?>> void
    persisted_active_A_that_references_active_B_cannot_be_saved_if_B_is_concurrenly_deactivated() {
        final Spec1<A, B> spec = spec1();
        final B b = save(spec.newB(ACTIVE, true));

        final A a = save(spec.newA(ACTIVE, true));
        spec.setB1(a, b);

        // Concurrent deactivation of B just referenced by A.
        final var b1 = refetch$(b, ALL);
        b1.set(ACTIVE, false);
        save(b1);

        assertThatThrownBy(() -> save(a))
                .hasMessage(ERR_ENTITY_EXISTS_BUT_NOT_ACTIVE.formatted(getEntityTitleAndDesc(spec.bType()).getKey(), b));
    }

    @Test
    public <A extends ActivatableAbstractEntity<?>, B extends ActivatableAbstractEntity<?>> void
    if_A_is_activated_while_referencing_inactive_B_then_validation_fails() {
        final Spec1<A, B> spec = spec1();
        final B b = save(spec.newB(ACTIVE, false));
        final A a = save(spec.newA(ACTIVE, false, spec.A_b1(), b));

        a.set(ACTIVE, true);

        assertNotNull(a.getProperty(ACTIVE).getFirstFailure());
        assertEquals(ERR_INACTIVE_REFERENCES.formatted(
                            getTitleAndDesc(spec.A_b1(), a.getType()).getKey(),
                            getEntityTitleAndDesc(a.getType()).getKey(),
                            a,
                            getEntityTitleAndDesc(b.getType()).getKey(),
                            b),
                     a.getProperty(ACTIVE).getFirstFailure().getMessage());
    }

    @Test
    public <A extends ActivatableAbstractEntity<?>, B extends ActivatableAbstractEntity<?>> void
    if_A_is_activated_while_referencing_inactive_B_via_proxied_property_then_validation_succeeds_but_saving_A_fails() {
        final Spec1<A, B> spec = spec1();
        final B b = save(spec.newB(ACTIVE, false));
        final A a;
        {
            final A a1 = save(spec.newA(ACTIVE, false, spec.A_b1(), b));
            a = refetch$(a1, fetchAll(spec.aType()).without(spec.A_b1()));
        }

        assertFalse(a.isActive());
        assertTrue(a.getPropertyIfNotProxy(spec.A_b1().toString()).isEmpty());

        a.set(ACTIVE, true);
        assertTrue(a.isValid().isSuccessful());

        assertThatThrownBy(() -> save(a))
                .isInstanceOf(EntityCompanionException.class)
                .hasMessage(ERR_INACTIVE_REFERENCES.formatted(
                                   getTitleAndDesc(spec.A_b1(), a.getType()).getKey(),
                                   getEntityTitleAndDesc(a.getType()).getKey(),
                                   a,
                                   getEntityTitleAndDesc(b.getType()).getKey(),
                                   b));
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : References from inactive entities and from non-activatable entities do not affect `refCount` of referenced activatable entities.
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    @Test
    public <A extends ActivatableAbstractEntity<?>, B extends ActivatableAbstractEntity<?>> void
    if_inactive_A_dereferences_active_B_then_refCount_of_B_is_not_affected() {
        final Spec1<A, B> spec = spec1();

        final B b = save(spec.newB(ACTIVE, true, REF_COUNT, 10));
        final A a = save(spec.newA(ACTIVE, false, spec.A_b1(), b));

        assertRefCount(10, b);

        save(spec.setB1(a, null));
        assertRefCount(10, b);
    }

    @Test
    public <A extends ActivatableAbstractEntity<?>, B extends ActivatableAbstractEntity<?>> void
    if_inactive_A_dereferences_active_B1_and_references_active_B2_then_refCounts_of_Bs_are_not_affected() {
        final Spec1<A, B> spec = spec1();

        final B b1 = save(spec.newB(ACTIVE, true, REF_COUNT, 10));
        final A a = save(spec.newA(ACTIVE, false, spec.A_b1(), b1));

        assertRefCount(10, b1);

        final B b2 = save(spec.newB(ACTIVE, true, REF_COUNT, 20));
        save(spec.setB1(a, b2));
        assertRefCount(10, b1);
        assertRefCount(20, b2);
    }

    @Test
    public <A extends ActivatableAbstractEntity<?>, B extends ActivatableAbstractEntity<?>> void
    if_active_A_is_concurrently_deactivated_before_it_begins_referencing_active_B_then_refCount_of_B_is_not_affected() {
        final Spec1<A, B> spec = spec1();

        final B b = save(spec.newB(ACTIVE, true, REF_COUNT, 10));
        final A a = save(spec.newA(ACTIVE, true));

        final A a_v1 = (A) refetch$(a).set(ACTIVE, false);
        final A a_v2 = spec.setB1(refetch$(a), b);

        save(a_v1);
        assertRefCount(10, b);

        save(a_v2);
        assertRefCount(10, b);
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Changing the active status of an entity affects `refCount` of referenced active entities only once.
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    @Test
    public <A extends ActivatableAbstractEntity<?>, B extends ActivatableAbstractEntity<?>> void
    concurrent_deactivation_of_the_same_entity_decrements_refCount_of_referenced_active_entities_only_once() {
        final Spec1<A, B> spec = spec1();

        final B b = save(spec.newB(ACTIVE, true, REF_COUNT, 10));
        final A a = save(spec.newA(ACTIVE, true, spec.A_b1(), b));
        assertRefCount(11, b);

        final A a_v1 = (A) refetch$(a).set(ACTIVE, false);
        final A a_v2 = (A) refetch$(a).set(ACTIVE, false);

        save(a_v1);
        assertRefCount(10, b);

        save(a_v2);
        assertRefCount(10, b);
    }

    @Test
    public <A extends ActivatableAbstractEntity<?>, B extends ActivatableAbstractEntity<?>> void
    concurrent_activation_of_the_same_entity_increments_refCount_of_referenced_active_entities_only_once() {
        final Spec1<A, B> spec = spec1();

        final B b = save(spec.newB(ACTIVE, true, REF_COUNT, 10));
        final A a = save(spec.newA(ACTIVE, false, spec.A_b1(), b));
        assertRefCount(10, b);

        final A a_v1 = (A) refetch$(a).set(ACTIVE, true);
        final A a_v2 = (A) refetch$(a).set(ACTIVE, true);

        save(a_v1);
        assertRefCount(11, b);

        save(a_v2);
        assertRefCount(11, b);
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Concurrent operations
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    @SuppressWarnings("unchecked")
    @Test
    public <A extends ActivatableAbstractEntity<?>, B extends ActivatableAbstractEntity<?>> void
    dereferencing_with_concurrent_deactivation_of_the_referencing_entity_decrements_refCount_only_once_when_new_value_is_null() {
        final Spec1<A, B> spec = spec1();

        final B b = save(spec.newB(ACTIVE, true, REF_COUNT, 10));
        final A a = save(spec.newA(ACTIVE, true, spec.A_b1(), b));

        assertRefCount(11, b);

        final A a_v1 = (A) refetch$(a).set(ACTIVE, false);
        final A a_v2 = spec.setB1(refetch$(a), null);

        save(a_v1);
        assertRefCount(10, b);

        save(a_v2);
        assertRefCount(10, b);
    }

    @Test
    public <A extends ActivatableAbstractEntity<?>, B extends ActivatableAbstractEntity<?>> void
    dereferencing_with_concurrent_deactivation_of_the_referencing_entity_decrements_refCount_only_once_when_new_value_is_not_null() {
        final Spec1<A, B> spec = spec1();

        final B b = save(spec.newB(ACTIVE, true, REF_COUNT, 10));
        final A a = save(spec.newA(ACTIVE, true, spec.A_b1(), b));

        assertRefCount(11, b);

        final A a_v1 = (A) refetch$(a).set(ACTIVE, false);
        final B b2 = save(spec.newB(ACTIVE, true));
        final A a_v2 = spec.setB1(refetch$(a), b2);

        save(a_v1);
        assertRefCount(10, b);

        save(a_v2);
        assertRefCount(10, b);
    }

    @Test
    public <A extends ActivatableAbstractEntity<?>, B extends ActivatableAbstractEntity<?>> void
    refCount_for_dereferenced_entity_is_decremented_only_once_upon_concurrent_dereferencing_from_the_same_entity() {
        final Spec1<A, B> spec = spec1();

        final B b = save(spec.newB(ACTIVE, true, REF_COUNT, 10));
        final A a = save(spec.newA(ACTIVE, true, spec.A_b1(), b));

        assertRefCount(11, b);

        final A a_v1 = spec.setB1(refetch$(a), null);
        final A a_v2 = spec.setB1(refetch$(a), null);

        save(a_v1);
        assertRefCount(10, b);
        save(a_v2);
        assertRefCount(10, b);
    }

    @Test
    public <A extends ActivatableAbstractEntity<?>, B extends ActivatableAbstractEntity<?>> void
    refCount_for_referenced_entity_is_incremented_only_once_upon_concurrent_modification_of_the_same_entity() {
        final Spec1<A, B> spec = spec1();

        final B b = save(spec.newB(ACTIVE, true, REF_COUNT, 10));
        final A a = save(spec.newA(ACTIVE, true));

        final A a_v1 = spec.setB1(refetch$(a), b);
        final A a_v2 = spec.setB1(refetch$(a), b);

        final A saved_a_v1 = save(a_v1);
        assertEquals(1, saved_a_v1.getVersion().intValue());
        assertRefCount(11, b);

        final A saved_a_v2 = save(a_v2);
        assertEquals(2, saved_a_v2.getVersion().intValue());
        assertRefCount(11, b);
    }

    @Test
    public <A extends ActivatableAbstractEntity<?>, B extends ActivatableAbstractEntity<?>> void
    refCount_for_dereferenced_entity_is_decremented_and_incremented_for_referened_entity_only_once_upon_concurrent_modification_of_the_same_entity() {
        final Spec1<A, B> spec = spec1();

        final B b1 = save(spec.newB(ACTIVE, true, REF_COUNT, 10));
        final B b2 = save(spec.newB(ACTIVE, true, REF_COUNT, 20));

        final A a = save(spec.newA(ACTIVE, true, spec.A_b1(), b1));

        assertRefCount(11, b1);

        final A a_v1 = spec.setB1(refetch$(a, ALL), b2);
        final A a_v2 = spec.setB1(refetch$(a, ALL), b2);

        final A saved_a_v1 = save(a_v1);
        assertEquals(1, saved_a_v1.getVersion().intValue());
        assertRefCount(10, b1);
        assertRefCount(21, b2);

        final A saved_a_v2 = save(a_v2);
        assertEquals(2, saved_a_v2.getVersion().intValue());
        assertRefCount(10, b1);
        assertRefCount(21, b2);
    }

    @Test
    public <A extends ActivatableAbstractEntity<?>, B extends ActivatableAbstractEntity<?>> void
    activation_that_is_concurrent_with_referencing_leads_to_increment_of_refCount_01() {
        final Spec1<A, B> spec = spec1();

        final A a = save(spec.newA(ACTIVE, false));
        final B b = save(spec.newB(ACTIVE, true, REF_COUNT, 10));

        final A a_v1 = (A) refetch$(a).set(ACTIVE, true);
        final A a_v2 = spec.setB1(refetch$(a), b);

        save(a_v1);
        assertRefCount(10, b);

        save(a_v2);
        assertRefCount(11, b);
    }

    @Test
    public <A extends ActivatableAbstractEntity<?>, B extends ActivatableAbstractEntity<?>> void
    activation_that_is_concurrent_with_referencing_leads_to_increment_of_refCount_02() {
        final Spec1<A, B> spec = spec1();

        final A a = save(spec.newA(ACTIVE, false));
        final B b = save(spec.newB(ACTIVE, true, REF_COUNT, 10));

        // Inactive A begins referencing active B.
        final A a_v1 = setProperties(spec, refetch$(a), spec.A_b1(), b);
        // Concurrently, A is activated and begins referencing active B.
        final A a_v2 = setProperties(spec, refetch$(a), spec.A_b1(), b, ACTIVE, true);

        save(a_v1);
        assertRefCount(10, b);

        save(a_v2);
        assertRefCount(11, b);
    }

    @SuppressWarnings("unchecked")
    @Test
    public <A extends ActivatableAbstractEntity<?>, B extends ActivatableAbstractEntity<?>> void
    concurrent_activation_and_dereferencing_both_change_refCount_with_cumulative_effect_of_0() {
        final Spec1<A, B> spec = spec1();

        final B b = save(spec.newB(ACTIVE, true, REF_COUNT, 10));
        final A a = save(spec.newA(ACTIVE, false, spec.A_b1(), b));

        assertRefCount(10, b);

        final A a_v1 = (A) refetch$(a).set(ACTIVE, true);
        final A a_v2 = spec.setB1(refetch$(a), null);

        save(a_v1);
        assertRefCount(11, b);

        save(a_v2);
        assertRefCount(10, b);
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Each property counts as a separate reference.
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    @Test
    public
    <A extends ActivatableAbstractEntity<?>, B extends ActivatableAbstractEntity<?>> void
    when_a_new_entity_A_begins_referencing_entity_B_then_refCount_of_B_is_incremented_for_each_reference_from_A() {
        final Spec1<A, B> spec = spec1();

        final B b = save(spec.newB(ACTIVE, true));
        final A a = save(spec.newA(ACTIVE, true, spec.A_b1(), b, spec.A_b2(), b));

        assertRefCount(2, b);
    }

    @Test
    public
    <A extends ActivatableAbstractEntity<?>, B extends ActivatableAbstractEntity<?>> void
    when_a_modified_entity_A_begins_referencing_entity_B_then_refCount_of_B_is_incremented_for_each_reference_from_A() {
        final Spec1<A, B> spec = spec1();

        final B b = save(spec.newB(ACTIVE, true));
        {
            final A a = save(spec.newA(ACTIVE, true));
            spec.setB1(a, b);
            spec.setB2(a, b);
            save(a);
        }

        assertRefCount(2, b);
    }

    @Test
    public
    <A extends ActivatableAbstractEntity<?>, B extends ActivatableAbstractEntity<?>> void
    when_entity_A_is_activated_then_refCount_of_B_is_incremented_for_each_reference_from_A() {
        final Spec1<A, B> spec = spec1();

        final B b = save(spec.newB(ACTIVE, true));
        final A a = save(spec.newA(ACTIVE, false, spec.A_b1(), b, spec.A_b2(), b));
        assertRefCount(0, b);

        save(a.set(ACTIVE, true));
        assertRefCount(2, b);
    }

    @Test
    public <A extends ActivatableAbstractEntity<?>, B extends ActivatableAbstractEntity<?>> void
    when_entity_A_is_deactivated_then_refCount_of_B_is_decremented_for_each_reference_from_A() {
        final Spec1<A, B> spec = spec1();

        final B b1 = save(spec.newB(ACTIVE, true, REF_COUNT, 10));
        final A a = save(spec.newA(ACTIVE, true, spec.A_b1(), b1, spec.A_b2(), b1));

        assertRefCount(12, b1);

        save(a.set(ACTIVE, false));
        assertRefCount(10, b1);
    }

    @Test
    public <A extends ActivatableAbstractEntity<?>, B extends ActivatableAbstractEntity<?>> void
    refCount_value_is_equal_to_number_of_references_from_entities_in_different_properties_rather_than_to_number_of_such_entities() {
        final Spec1<A, B> spec = spec1();

        final B b = save(spec.newB(ACTIVE, true, REF_COUNT, 10));

        // Set properties one by one and assert refCount increasing.
        A a = save(spec.newA(ACTIVE, true, spec.A_b1(), b));
        assertRefCount(11, b);

        a = save(spec.setB2(a, b));

        assertRefCount(12, b);

        // Unset properties one by one and assert refCount decreasing.
        a = save(spec.setB1(a, null));
        assertRefCount(11, b);

        a = save(spec.setB2(a, null));
        assertRefCount(10, b);
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : `refCount` of inactive entities is not affected by references from any entities.
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    @Test
    public <A extends ActivatableAbstractEntity<?>, B extends ActivatableAbstractEntity<?>> void
    activating_entity_that_was_referencing_inactive_activatable_does_not_change_refCount_of_that_activatable() {
        final Spec1<A, B> spec = spec1();

        final B b = save(spec.newB(ACTIVE, false, REF_COUNT, 10));
        final A a = save(spec.newA(ACTIVE, false, spec.A_b1(), b));

        save(setProperties(spec, a, spec.A_b1(), null, ACTIVE, true));

        assertRefCount(10, b);
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    @Test
    public <A extends ActivatableAbstractEntity<?>, B extends ActivatableAbstractEntity<?>> void
    active_entity_cannot_be_deactivated_while_referenced_by_other_active_entities() {
        final Spec1<A, B> spec = spec1();

        final B b = save(spec.newB(ACTIVE, true));
        final A a = save(spec.newA(ACTIVE, true, spec.A_b1(), b));

        b.set(ACTIVE, false);
        final var mpActive = b.getProperty(ACTIVE);
        assertFalse(mpActive.isValid());
        assertThat(mpActive.getFirstFailure().getMessage())
                .startsWith(ERR_SHORT_ENTITY_HAS_ACTIVE_DEPENDENCIES.formatted(getEntityTitleAndDesc(b).getKey(), b, 1, "dependency"));
    }

    @Test
    public <A extends ActivatableAbstractEntity<?>, B extends ActivatableAbstractEntity<?>> void
    activating_entity_referencing_inactive_values_in_properties_with_SkipEntityExistsValidation_where_skipActiveOnly_eq_false_is_not_permitted() {
        final Spec1<A, B> spec = spec1();

        final B b = save(spec.newB(ACTIVE, false));
        A a = save(spec.newA(ACTIVE, false, spec.A_b4(), b));

        a = (A) a.set(ACTIVE, true);
        assertNotNull(a.getProperty(ACTIVE).getFirstFailure());
        assertEquals(ERR_INACTIVE_REFERENCES.formatted(
                            getTitleAndDesc(spec.A_b4(), a.getType()).getKey(),
                            getEntityTitleAndDesc(a).getKey(),
                            a,
                            getEntityTitleAndDesc(b).getKey(),
                            b),
                     a.getProperty(ACTIVE).getFirstFailure().getMessage());
    }

    @Test
    public <A extends ActivatableAbstractEntity<?>, B extends ActivatableAbstractEntity<?>> void
    deactivation_and_saving_of_self_referenced_activatable_is_permissible_but_does_not_decrement_its_refCount() {
        final Spec1<A, B> spec = spec1();

        A a = save(spec.newA(ACTIVE, true, REF_COUNT, 10));
        a = save(spec.setA1(a, a));

        assertRefCount(10, a);

        a = save((A) a.set(ACTIVE, false));

        assertRefCount(10, a);
    }

    @Test
    public <A extends ActivatableAbstractEntity<?>, B extends ActivatableAbstractEntity<?>> void
    activating_entity_that_is_referenced_by_inactive_is_permitted_but_does_not_change_its_refCount_and_also_updates_referenced_not_dirty_active_activatables() {
        final Spec1<A, B> spec = spec1();

        final B b = save(spec.newB(ACTIVE, true, REF_COUNT, 10));
        A a1 = save(spec.newA(ACTIVE, false, spec.A_b1(), b, REF_COUNT, 20));
        final A a2 = save(spec.newA(ACTIVE, false, spec.A_a1(), a1));

        a1 = save((A) a1.set(ACTIVE, true));
        assertRefCount(20, a1);

        assertRefCount("refCount of the referenced non-dirty activatable should have increased by 1.", 11, b);
    }

    @Test
    public <A extends ActivatableAbstractEntity<?>, B extends ActivatableAbstractEntity<?>> void
    activating_entity_A_and_dereferencing_entity_B_does_not_affect_refCount_of_B() {
        final Spec1<A, B> spec = spec1();

        final B b = save(spec.newB(ACTIVE, true, REF_COUNT, 10));
        A a = save(spec.newA(ACTIVE, false, spec.A_b1(), b));

        assertRefCount(10, b);

        a = save(setProperties(spec, a, ACTIVE, true, spec.A_b1(), null));

        assertRefCount(10, b);
    }

    @Test
    public <A extends ActivatableAbstractEntity<?>, B extends ActivatableAbstractEntity<?>> void
    deactivation_of_an_entity_decrements_refCount_of_referenced_active_entities() {
        final Spec1<A, B> spec = spec1();

        final B b = save(spec.newB(ACTIVE, true, REF_COUNT, 10));
        final A a = save(spec.newA(ACTIVE, true, spec.A_b1(), b));

        assertRefCount(11, b);

        save(a.set(ACTIVE, false));
        assertRefCount(10, b);
    }

    @Test
    public <A extends ActivatableAbstractEntity<?>, B extends ActivatableAbstractEntity<?>> void
    changing_and_unsetting_activatable_properties_leads_to_decrement_of_dereferenced_instances_and_increment_of_just_referenced_ones() {
        final Spec1<A, B> spec = spec1();

        final B b1 = save(spec.newB(ACTIVE, true, REF_COUNT, 10));
        final A a = save(spec.newA(ACTIVE, true, spec.A_b1(), b1));

        assertRefCount(11, b1);

        final B b2 = save(spec.newB(ACTIVE, true, REF_COUNT, 20));
        save(setProperties(spec, a, spec.A_b1(), null, spec.A_b2(), b2));

        assertRefCount(10, b1);
        assertRefCount(21, b2);
    }

    @Test
    public <A extends ActivatableAbstractEntity<?>, B extends ActivatableAbstractEntity<?>> void
    deactivation_with_simultaneous_dereferencing_of_active_activatables_decrements_refCount_of_dereferenced_active_activatables() {
        final Spec1<A, B> spec = spec1();

        final B b = save(spec.newB(ACTIVE, true, REF_COUNT, 10));
        final A a = save(spec.newA(ACTIVE, true, spec.A_b1(), b));

        assertRefCount(11, b);

        save(setProperties(spec, a, ACTIVE, false, spec.A_b1(), null));
        assertRefCount(10, b);
    }

    @Test
    public <A extends ActivatableAbstractEntity<?>, B extends ActivatableAbstractEntity<?>> void
    recomputation_of_refCount_after_multiple_changes_of_activatable_property_with_referencing_is_performed_for_correct_instances() {
        final Spec1<A, B> spec = spec1();

        final B b1 = save(spec.newB(ACTIVE, true, REF_COUNT, 10));
        final B b2 = save(spec.newB(ACTIVE, true, REF_COUNT, 20));
        final B b3 = save(spec.newB(ACTIVE, true, REF_COUNT, 30));
        final A a = save(spec.newA(ACTIVE, true, spec.A_b1(), b1));

        assertRefCount(11, b1);

        // Intermediate value that will be replaced before saving changes.
        spec.setB1(a, b2);
        spec.setB1(a, b3);
        save(a);

        assertRefCount(10, b1);
        assertRefCount(20, b2);
        assertRefCount(31, b3);
    }

    @Test
    public <A extends ActivatableAbstractEntity<?>, B extends ActivatableAbstractEntity<?>> void
    recomputation_of_refCount_after_mutiple_changes_of_activatable_property_with_dereferencing_is_performed_for_correct_instances() {
        final Spec1<A, B> spec = spec1();

        final B b1 = save(spec.newB(ACTIVE, true, REF_COUNT, 10));
        final B b2 = save(spec.newB(ACTIVE, true, REF_COUNT, 20));
        final A a = save(spec.newA(ACTIVE, true, spec.A_b1(), b1));

        assertRefCount(11, b1);

        // Intermediate value that will be replaced before saving changes.
        spec.setB1(a, b2);
        spec.setB1(a, null);
        save(a);

        assertRefCount(10, b1);
        assertRefCount(20, b2);
    }

    @Test
    public <A extends ActivatableAbstractEntity<?>, B extends ActivatableAbstractEntity<?>> void
    recomputation_of_refCount_after_mutiple_changes_of_activatable_property_that_had_no_value_before_is_performed_for_correct_instances() {
        final Spec1<A, B> spec = spec1();

        final B b1 = save(spec.newB(ACTIVE, true, REF_COUNT, 10));
        final B b2 = save(spec.newB(ACTIVE, true, REF_COUNT, 20));
        final A a = save(spec.newA(ACTIVE, true));

        // Intermediate value that will be replaced before saving changes.
        spec.setB1(a, b1);
        spec.setB1(a, b2);
        save(a);

        assertRefCount(10, b1);
        assertRefCount(21, b2);
    }

    @Test
    public <A extends ActivatableAbstractEntity<?>, B extends AbstractEntity<?>> void
    activatable_entity_can_be_activated_while_referencing_only_non_activatable_entities() {
        final Spec2<A, B> spec = spec2();

        final B b = save(spec.newB());
        final A a = save(spec.newA(ACTIVE, false, spec.A_b1(), b));

        assertFalse(a.isActive());
        a.set(ACTIVE, true);
        assertNull(a.getProperty(ACTIVE).getFirstFailure());
        assertTrue(a.isActive());
    }

}
