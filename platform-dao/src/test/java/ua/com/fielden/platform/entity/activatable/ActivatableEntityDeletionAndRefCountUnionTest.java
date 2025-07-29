package ua.com.fielden.platform.entity.activatable;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.activatable.test_entities.ActivatableUnionOwner;
import ua.com.fielden.platform.entity.activatable.test_entities.Member1;
import ua.com.fielden.platform.entity.activatable.test_entities.Union;
import ua.com.fielden.platform.meta.EntityMetadata;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.meta.PropertyMetadata;
import ua.com.fielden.platform.meta.PropertyMetadataUtils;

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

    @Override
    protected Spec1 spec1() {
        return spec1;
    }

}
