package ua.com.fielden.platform.entity.activatable;

import org.junit.Test;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.ActivatableAbstractEntity;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.entity.ActivatableAbstractEntity.ACTIVE;
import static ua.com.fielden.platform.entity.ActivatableAbstractEntity.REF_COUNT;
import static ua.com.fielden.platform.entity.activatable.WithActivatabilityTestUtils.setProperties;
import static ua.com.fielden.platform.utils.EntityUtils.isActivatableEntityType;

/// This test covers the effects of activatable entity deletion on `refCount`.
///
/// @see ActivatableEntityDeletionTest
///
public abstract class AbstractActivatableEntityDeletionAndRefCountTestCase extends AbstractDaoTestCase implements WithActivatabilityTestUtils {

    /// * `A` and `B` are activatable entity types.
    /// * `A` references `B` via 2 properties: `b1`, `b2`.
    /// * `A` supports deletion.
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
        A setB1(A a, B b);
        A setB2(A a, B b);
    }

    /// * `A` and `B` are activatable entity types.
    /// * `A` references `B` via 1 property: `b1`.
    /// * `A` supports deletion.
    /// * `A.b1` allows inactive values.
    ///
    protected interface Spec2<A extends ActivatableAbstractEntity<?>, B extends ActivatableAbstractEntity<?>>
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

    protected abstract <A extends ActivatableAbstractEntity<?>, B extends ActivatableAbstractEntity<?>> Spec1<A, B> spec1();

    protected abstract <A extends ActivatableAbstractEntity<?>, B extends ActivatableAbstractEntity<?>> Spec2<A, B> spec2();

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
        assertTrue(isActivatableEntityType(spec.bType()));
        // TODO Verify references, which may be immediate or union references.
    }

    @Test
    public <A extends ActivatableAbstractEntity<?>, B extends ActivatableAbstractEntity<?>> void
    deletion_of_active_A_that_references_active_B_decrements_refCount_of_B() {
        final Spec1<A, B> spec = spec1();

        final var b = save(spec.newB(ACTIVE, true, REF_COUNT, 10));
        final var a = save(spec.newA(ACTIVE, true, spec.A_b1(), b));

        assertRefCount(11, b);

        delete(a);

        assertFalse(co$(spec.aType()).entityExists(a));
        assertRefCount(10, b);
    }

    @Test
    public <A extends ActivatableAbstractEntity<?>, B extends ActivatableAbstractEntity<?>> void
    deletion_of_active_A_that_references_inactive_B_does_not_affect_refCount_of_B() {
        final Spec2<A, B> spec = spec2();

        final var b = save(spec.newB(ACTIVE, false, REF_COUNT, 10));
        final var a = save(spec.newA(ACTIVE, true, spec.A_b1(), b));

        assertRefCount(10, b);

        delete(a);

        assertFalse(co$(spec.aType()).entityExists(a));
        assertRefCount(10, b);
    }

    @Test
    public <A extends ActivatableAbstractEntity<?>, B extends ActivatableAbstractEntity<?>> void
    deletion_of_active_A_that_references_active_B_via_2_properties_decrements_refCount_of_B_by_2() {
        final Spec1<A, B> spec = spec1();

        final var b = save(spec.newB(ACTIVE, true, REF_COUNT, 10));
        final var a = save(spec.newA(ACTIVE, true, spec.A_b1(), b, spec.A_b2(), b));

        assertRefCount(12, b);

        delete(a);

        assertFalse(co$(spec.aType()).entityExists(a));
        assertRefCount(10, b);
    }

    @Test
    public <A extends ActivatableAbstractEntity<?>, B extends ActivatableAbstractEntity<?>> void
    deletion_of_active_A_that_references_2_different_active_Bs_decrements_refCount_of_both_by_1() {
        final Spec1<A, B> spec = spec1();

        final var b1 = save(spec.newB(ACTIVE, true, REF_COUNT, 10));
        final var b2 = save(spec.newB(ACTIVE, true, REF_COUNT, 20));
        final var a = save(spec.newA(ACTIVE, true, spec.A_b1(), b1, spec.A_b2(), b2));

        assertRefCount(11, b1);
        assertRefCount(21, b2);

        delete(a);

        assertFalse(co$(spec.aType()).entityExists(a));
        assertRefCount(10, b1);
        assertRefCount(20, b2);
    }

    @Test
    public <A extends ActivatableAbstractEntity<?>, B extends ActivatableAbstractEntity<?>> void
    deleting_inactive_A_that_references_active_B_does_not_affect_refCount_of_B() {
        final Spec1<A, B> spec = spec1();

        final var b = save(spec.newB(ACTIVE, true, REF_COUNT, 10));
        final var a = save(spec.newA(ACTIVE, false, spec.A_b1(), b));

        assertRefCount(10, b);

        delete(a);

        assertFalse(co$(spec.aType()).entityExists(a));
        assertRefCount(10, b);
    }

    @Test
    public <A extends ActivatableAbstractEntity<?>, B extends ActivatableAbstractEntity<?>> void
    deleting_concurrently_deactivated_A_that_references_active_B_does_not_affect_refCount_of_B() {
        final Spec1<A, B> spec = spec1();

        final var b = save(spec.newB(ACTIVE, true, REF_COUNT, 10));
        final var a_v0 = save(spec.newA(ACTIVE, true, spec.A_b1(), b));
        final var a_v1 = refetch$(a_v0);

        assertRefCount(11, b);

        save(a_v0.set(ACTIVE, false));

        assertRefCount(10, b);

        delete(a_v1);

        assertFalse(co$(spec.aType()).entityExists(a_v1));
        assertRefCount(10, b);
    }

    @Test
    public <A extends ActivatableAbstractEntity<?>, B extends ActivatableAbstractEntity<?>> void
    deleting_concurrently_activated_A_that_references_active_B_decrements_refCount_of_B() {
        final Spec1<A, B> spec = spec1();

        final var b = save(spec.newB(ACTIVE, true, REF_COUNT, 10));
        final var a_v0 = save(spec.newA(ACTIVE, false, spec.A_b1(), b));
        final var a_v1 = refetch$(a_v0);

        assertRefCount(10, b);

        final var a_v2 = (A) save(a_v1.set(ACTIVE, true));

        assertRefCount(11, b);

        delete(a_v2);

        assertFalse(co$(spec.aType()).entityExists(a_v2));
        assertRefCount(10, b);
    }

    @Test
    public <A extends ActivatableAbstractEntity<?>, B extends ActivatableAbstractEntity<?>> void
    deletion_of_active_A_that_concurrently_begins_referencing_another_active_B_decrements_refCount_only_of_that_B() {
        final Spec1<A, B> spec = spec1();

        final var b1 = save(spec.newB(ACTIVE, true, REF_COUNT, 10));
        final var b2 = save(spec.newB(ACTIVE, true, REF_COUNT, 20));
        final var a = save(spec.newA(ACTIVE, true, spec.A_b1(), b1));

        save(spec.setB1(refetch$(a), b2));

        assertRefCount(10, b1);
        assertRefCount(21, b2);

        delete(a);

        assertFalse(co$(spec.aType()).entityExists(a));
        assertRefCount(10, b1);
        assertRefCount(20, b2);
    }

    @Test
    public <A extends ActivatableAbstractEntity<?>, B extends ActivatableAbstractEntity<?>> void
    deletion_of_active_A_that_concurrently_begins_referencing_another_inactive_B_does_not_affect_refCount_of_that_B() {
        final Spec2<A, B> spec = spec2();

        final var b1 = save(spec.newB(ACTIVE, true, REF_COUNT, 10));
        final var b2 = save(spec.newB(ACTIVE, false, REF_COUNT, 20));
        final var a = save(spec.newA(ACTIVE, true, spec.A_b1(), b1));

        save(spec.setB1(refetch$(a), b2));

        assertRefCount(10, b1);
        assertRefCount(20, b2);

        delete(a);

        assertFalse(co$(spec.aType()).entityExists(a));
        assertRefCount(10, b1);
        assertRefCount(20, b2);
    }

    @Test
    public <A extends ActivatableAbstractEntity<?>, B extends ActivatableAbstractEntity<?>> void
    deletion_of_active_A_that_concurrently_dereferences_B_does_not_affect_refCount_of_B() {
        final Spec1<A, B> spec = spec1();

        final var b = save(spec.newB(ACTIVE, true, REF_COUNT, 10));
        final var a = save(spec.newA(ACTIVE, true, spec.A_b1(), b));

        save(spec.setB1(refetch$(a), null));

        assertRefCount(10, b);

        delete(a);

        assertFalse(co$(spec.aType()).entityExists(a));
        assertRefCount(10, b);
    }

    protected abstract void delete(AbstractEntity<?> entity);

}
