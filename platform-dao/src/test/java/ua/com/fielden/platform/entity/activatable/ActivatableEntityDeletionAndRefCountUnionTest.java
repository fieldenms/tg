package ua.com.fielden.platform.entity.activatable;

import org.junit.Test;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.activatable.test_entities.*;
import ua.com.fielden.platform.meta.EntityMetadata;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.meta.PropertyMetadata;
import ua.com.fielden.platform.meta.PropertyMetadataUtils;

import static org.junit.Assert.assertFalse;

public class ActivatableEntityDeletionAndRefCountUnionTest extends AbstractActivatableEntityDeletionAndRefCountTestCase {

    private final Spec1 spec1 = new Spec1<ActivatableUnionOwner, Member1> () {

        private int ownerKeyCounter = 1;
        private int memberKeyCounter = 1;
        private final IDomainMetadata domainMetadata = getInstance(IDomainMetadata.class);
        private final PropertyMetadataUtils pmUtils = domainMetadata.propertyMetadataUtils();

        @Override
        public ActivatableUnionOwner newA() {
            return new_(ActivatableUnionOwner.class, "Owner%s".formatted(ownerKeyCounter++));
        }

        @Override
        public Member1 newB() {
            return new_(Member1.class, "Member%s".formatted(memberKeyCounter++));
        }

        @Override
        public Class<ActivatableUnionOwner> aType() {
            return ActivatableUnionOwner.class;
        }

        @Override
        public Class<Member1> bType() {
            return Member1.class;
        }

        @Override
        public CharSequence A_b1() {
            return "union";
        }

        @Override
        public CharSequence A_b2() {
            return "member1";
        }

        @Override
        public ActivatableUnionOwner setB1(final ActivatableUnionOwner owner, final Member1 member1) {
            return owner.setUnion(member1 == null ? null : new_(Union.class).setMember1(member1));
        }

        @Override
        public ActivatableUnionOwner setB2(final ActivatableUnionOwner owner, final Member1 member1) {
            return owner.setMember1(member1);
        }

        @SuppressWarnings("unchecked")
        @Override
        public <E extends AbstractEntity<?>> E setProperty(
                final E entity,
                final CharSequence prop,
                final Object value)
        {
            final PropertyMetadata pm;
            if (value != null
                && entity instanceof ActivatableUnionOwner owner
                && pmUtils.isPropEntityType((pm = domainMetadata.forProperty(entity.getType(), prop)), EntityMetadata::isUnion)
                && !(value instanceof AbstractUnionEntity))
            {
                final var propType = (Class<? extends AbstractUnionEntity>) pm.type().javaType();
                return (E) owner.set(prop.toString(), new_(propType).setUnionProperty((AbstractEntity<?>) value));
            }
            else {
                return Spec1.super.setProperty(entity, prop, value);
            }
        }
    };

    private final Spec2 spec2 = new Spec2<ActivatableUnionOwner, Member2> () {

        private int ownerKeyCounter = 1;
        private int memberKeyCounter = 1;
        private final IDomainMetadata domainMetadata = getInstance(IDomainMetadata.class);
        private final PropertyMetadataUtils pmUtils = domainMetadata.propertyMetadataUtils();

        @Override
        public ActivatableUnionOwner newA() {
            return new_(ActivatableUnionOwner.class, "Owner%s".formatted(ownerKeyCounter++));
        }

        @Override
        public Member2 newB() {
            return new_(Member2.class, "Member%s".formatted(memberKeyCounter++));
        }

        @Override
        public Class<ActivatableUnionOwner> aType() {
            return ActivatableUnionOwner.class;
        }

        @Override
        public Class<Member2> bType() {
            return Member2.class;
        }

        @Override
        public CharSequence A_b1() {
            return "union2";
        }

        @Override
        public ActivatableUnionOwner setB1(final ActivatableUnionOwner owner, final Member2 member2) {
            return owner.setUnion2(member2 == null ? null : new_(Union.class).setMember2(member2));
        }

        @SuppressWarnings("unchecked")
        @Override
        public <E extends AbstractEntity<?>> E setProperty(
                final E entity,
                final CharSequence prop,
                final Object value)
        {
            final PropertyMetadata pm;
            if (value != null
                && entity instanceof ActivatableUnionOwner owner
                && pmUtils.isPropEntityType((pm = domainMetadata.forProperty(entity.getType(), prop)), EntityMetadata::isUnion)
                && !(value instanceof AbstractUnionEntity))
            {
                final var propType = (Class<? extends AbstractUnionEntity>) pm.type().javaType();
                return (E) owner.set(prop.toString(), new_(propType).setUnionProperty((AbstractEntity<?>) value));
            }
            else {
                return Spec2.super.setProperty(entity, prop, value);
            }
        }
    };

    @Override
    protected Spec1 spec1() {
        return spec1;
    }

    @Override
    protected Spec2 spec2() {
        return spec2;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void delete(final AbstractEntity<?> entity) {
        final var co$ = (IEntityDao<AbstractEntity<?>>) co$(entity.getType());
        co$.delete(entity);
    }

    @Test
    public void deletion_of_active_A_that_references_active_B_via_union_does_not_affect_refCount_of_B_if_skipActiveOnly_is_true_on_both_levels() {
        final var b = save(new_(Member2.class, "M2").setActive(true).setRefCount(10));
        final var a = save(new_(ActivatableUnionOwner.class, "O1").setActive(true).setUnion2(new_(Union.class).setMember2(b)));

        assertRefCount(10, b);

        delete(a);

        assertFalse(co$(ActivatableUnionOwner.class).entityExists(a));
        assertRefCount(10, b);
    }

    @Test
    public void deletion_of_active_A_that_references_active_B_via_union_does_not_affect_refCount_of_B_if_SkipActivatableTracking_is_present_on_both_levels() {
        final var b = save(new_(Member4.class, "M4").setActive(true).setRefCount(10));
        final var a = save(new_(ActivatableUnionOwner.class, "O1").setActive(true).setUnion4(new_(Union.class).setMember4(b)));

        assertRefCount(10, b);

        delete(a);

        assertFalse(co$(ActivatableUnionOwner.class).entityExists(a));
        assertRefCount(10, b);
    }

    @Test
    public void deletion_of_active_A_that_references_active_B_via_union_decrements_refCount_of_B_if_skipActiveOnly_is_true_only_for_the_union_typed_property() {
        final var b = save(new_(Member1.class, "M1").setActive(true).setRefCount(10));
        final var a = save(new_(ActivatableUnionOwner.class, "O1").setActive(true).setUnion2(new_(Union.class).setMember1(b)));

        assertRefCount(11, b);

        delete(a);

        assertFalse(co$(ActivatableUnionOwner.class).entityExists(a));
        assertRefCount(10, b);
    }

    @Test
    public void deletion_of_active_A_that_references_active_B_via_union_decrements_refCount_of_B_if_skipActiveOnly_is_true_only_for_the_union_member_property() {
        final var b = save(new_(Member2.class, "M2").setActive(true).setRefCount(10));
        final var a = save(new_(ActivatableUnionOwner.class, "O1").setActive(true).setUnion(new_(Union.class).setMember2(b)));

        assertRefCount(11, b);

        delete(a);

        assertFalse(co$(ActivatableUnionOwner.class).entityExists(a));
        assertRefCount(10, b);
    }

    @Test
    public void deletion_of_active_A_that_references_active_B_via_union_decrements_refCount_of_B_if_SkipActivatableTracking_is_present_only_for_the_union_typed_property() {
        final var b = save(new_(Member1.class, "M1").setActive(true).setRefCount(10));
        final var a = save(new_(ActivatableUnionOwner.class, "O1").setActive(true).setUnion4(new_(Union.class).setMember1(b)));

        assertRefCount(11, b);

        delete(a);

        assertFalse(co$(ActivatableUnionOwner.class).entityExists(a));
        assertRefCount(10, b);
    }

    @Test
    public void deletion_of_active_A_that_references_active_B_via_union_decrements_refCount_of_B_if_SkipActivatableTracking_is_present_only_for_the_union_member_property() {
        final var b = save(new_(Member4.class, "M4").setActive(true).setRefCount(10));
        final var a = save(new_(ActivatableUnionOwner.class, "O1").setActive(true).setUnion(new_(Union.class).setMember4(b)));

        assertRefCount(11, b);

        delete(a);

        assertFalse(co$(ActivatableUnionOwner.class).entityExists(a));
        assertRefCount(10, b);
    }

}
