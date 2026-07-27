package ua.com.fielden.platform.entity.activatable;

import org.junit.Test;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.activatable.test_entities.*;
import ua.com.fielden.platform.meta.EntityMetadata;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.meta.PropertyMetadata;
import ua.com.fielden.platform.meta.PropertyMetadataUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.entity.ActivatableAbstractEntity.ACTIVE;
import static ua.com.fielden.platform.entity.validation.ActivePropertyValidator.ERR_INACTIVE_REFERENCES;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitleAndDesc;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getTitleAndDesc;

public class EntityActivatabilityUnionTest extends AbstractEntityActivatabilityTestCase {

    private final Spec1<ActivatableUnionOwner, Member1> spec1 = new Spec1<> () {

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
        public CharSequence A_b3() {
            return "union2";
        }

        @Override
        public CharSequence A_b4() {
            return "union3";
        }

        @Override
        public CharSequence A_a1() {
            return "union";
        }

        @Override
        public ActivatableUnionOwner setB1(final ActivatableUnionOwner owner, final Member1 member1) {
            return owner.setUnion(member1 == null ? null : new_(Union.class).setMember1(member1));
        }

        @Override
        public ActivatableUnionOwner setB2(final ActivatableUnionOwner owner, final Member1 member1) {
            return owner.setMember1(member1);
        }

        @Override
        public ActivatableUnionOwner setB3(final ActivatableUnionOwner owner, final Member1 member1) {
            return owner.setUnion2(member1 == null ? null : new_(Union.class).setMember1(member1));
        }

        @Override
        public ActivatableUnionOwner setB4(final ActivatableUnionOwner owner, final Member1 member1) {
            return owner.setUnion3(member1 == null ? null : new_(Union.class).setMember1(member1));
        }

        @Override
        public ActivatableUnionOwner setA1(final ActivatableUnionOwner owner, final ActivatableUnionOwner a1) {
            return owner.setUnion(a1 == null ? null : new_(Union.class).setOwner(a1));
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

    private final Spec2<ActivatableUnionOwner, Member3> spec2 = new Spec2<> () {

        private int ownerKeyCounter = 1;
        private int memberKeyCounter = 1;
        private final IDomainMetadata domainMetadata = getInstance(IDomainMetadata.class);
        private final PropertyMetadataUtils pmUtils = domainMetadata.propertyMetadataUtils();

        @Override
        public ActivatableUnionOwner newA() {
            return new_(ActivatableUnionOwner.class, "Owner%s".formatted(ownerKeyCounter++));
        }

        @Override
        public Member3 newB() {
            return new_(Member3.class, "Member%s".formatted(memberKeyCounter++));
        }

        @Override
        public Class<ActivatableUnionOwner> aType() {
            return ActivatableUnionOwner.class;
        }

        @Override
        public Class<Member3> bType() {
            return Member3.class;
        }

        @Override
        public CharSequence A_b1() {
            return "union";
        }

        @Override
        public ActivatableUnionOwner setB1(final ActivatableUnionOwner owner, final Member3 member3) {
            return owner.setUnion(member3 == null ? null : new_(Union.class).setMember3(member3));
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

    @SuppressWarnings("unchecked")
    @Override
    protected Spec1<ActivatableUnionOwner, Member1> spec1() {
        return spec1;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Spec2<ActivatableUnionOwner, Member3> spec2() {
        return spec2;
    }

    @Test
    public void saving_an_activated_A_referencing_inactive_B_via_union_succeeds_if_skipActiveOnly_is_true_on_both_levels() {
        final var b = save(new_(Member2.class, "M2").setActive(false));
        var a = save(new_(ActivatableUnionOwner.class, "O1").setActive(false).setUnion2(new_(Union.class).setMember2(b)));

        a = a.setActive(true);
        assertTrue(a.getProperty(ACTIVE).isValid());
        final var savedA = save(a);
        assertTrue(savedA.isActive());
    }

    @Test
    public void entity_can_be_activated_while_referencing_an_inactive_via_union_if_skipActiveOnly_is_true_on_both_levels() {
        final var member = save(new_(Member2.class, "M2").setActive(false));
        var owner = save(new_(ActivatableUnionOwner.class, "O1").setActive(false).setUnion2(new_(Union.class).setMember2(member)));

        owner = owner.setActive(true);
        assertTrue(owner.getProperty(ACTIVE).isValid());
    }

    @Test
    public void entity_can_be_activated_while_referencing_an_inactive_via_union_if_SkipActivatableTracking_is_present_on_both_levels() {
        final var member = save(new_(Member4.class, "M4").setActive(false));
        var owner = save(new_(ActivatableUnionOwner.class, "O1").setActive(false).setUnion4(new_(Union.class).setMember4(member)));

        owner = owner.setActive(true);
        assertTrue(owner.getProperty(ACTIVE).isValid());
    }

    @Test
    public void entity_cannot_be_activated_while_referencing_an_inactive_via_union_if_skipActiveOnly_is_true_only_for_the_union_typed_property() {
        final var member = save(new_(Member1.class, "M1").setActive(false));
        var owner = save(new_(ActivatableUnionOwner.class, "O1").setActive(false).setUnion2(new_(Union.class).setMember1(member)));

        owner = owner.setActive(true);
        assertThat(owner.getProperty(ACTIVE).getFirstFailure())
                .hasMessage(ERR_INACTIVE_REFERENCES.formatted(
                                   getTitleAndDesc("union2", ActivatableUnionOwner.class).getKey(),
                                   getEntityTitleAndDesc(owner).getKey(),
                                   owner,
                                   getEntityTitleAndDesc(member).getKey(),
                                   member));
    }

    @Test
    public void entity_cannot_be_activated_while_referencing_an_inactive_via_union_if_skipActiveOnly_is_true_only_for_the_union_member_property() {
        final var member = save(new_(Member2.class, "M2").setActive(false));
        var owner = save(new_(ActivatableUnionOwner.class, "O1").setActive(false).setUnion(new_(Union.class).setMember2(member)));

        owner = owner.setActive(true);
        assertThat(owner.getProperty(ACTIVE).getFirstFailure())
                .hasMessage(ERR_INACTIVE_REFERENCES.formatted(
                                   getTitleAndDesc("union", ActivatableUnionOwner.class).getKey(),
                                   getEntityTitleAndDesc(owner).getKey(),
                                   owner,
                                   getEntityTitleAndDesc(member).getKey(),
                                   member));
    }

    @Test
    public void entity_cannot_be_activated_while_referencing_an_inactive_via_union_if_SkipActivatableTracking_is_present_only_for_the_union_typed_property() {
        final var member = save(new_(Member1.class, "M1").setActive(false));
        var owner = save(new_(ActivatableUnionOwner.class, "O1").setActive(false).setUnion4(new_(Union.class).setMember1(member)));

        owner = owner.setActive(true);
        assertThat(owner.getProperty(ACTIVE).getFirstFailure())
                .hasMessage(ERR_INACTIVE_REFERENCES.formatted(
                                   getTitleAndDesc("union4", ActivatableUnionOwner.class).getKey(),
                                   getEntityTitleAndDesc(owner).getKey(),
                                   owner,
                                   getEntityTitleAndDesc(member).getKey(),
                                   member));
    }

    @Test
    public void entity_cannot_be_activated_while_referencing_an_inactive_via_union_if_SkipActivatableTracking_is_present_only_for_the_union_member_property() {
        final var member = save(new_(Member4.class, "M4").setActive(false));
        var owner = save(new_(ActivatableUnionOwner.class, "O1").setActive(false).setUnion(new_(Union.class).setMember4(member)));

        owner = owner.setActive(true);
        assertThat(owner.getProperty(ACTIVE).getFirstFailure())
                .hasMessage(ERR_INACTIVE_REFERENCES.formatted(
                                   getTitleAndDesc("union", ActivatableUnionOwner.class).getKey(),
                                   getEntityTitleAndDesc(owner).getKey(),
                                   owner,
                                   getEntityTitleAndDesc(member).getKey(),
                                   member));
    }

}
