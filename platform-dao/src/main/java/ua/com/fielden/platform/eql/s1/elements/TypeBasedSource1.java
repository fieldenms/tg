package ua.com.fielden.platform.eql.s1.elements;

import ua.com.fielden.platform.dao.PersistedEntityMetadata;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.meta.TransformatorToS2;
import ua.com.fielden.platform.eql.s2.elements.TypeBasedSource2;

public class TypeBasedSource1 extends AbstractSource1<TypeBasedSource2> {
    private PersistedEntityMetadata<? extends AbstractEntity<?>> entityMetadata;

    public TypeBasedSource1(final PersistedEntityMetadata<? extends AbstractEntity<?>> entityMetadata, final String alias) {
        super(alias);
        this.entityMetadata = entityMetadata;
        if (entityMetadata == null) {
            throw new IllegalStateException("Missing entity persistence metadata for entity type: " + sourceType());
        }
    }

    @Override
    public TypeBasedSource2 transform(final TransformatorToS2 resolver) {
        return (TypeBasedSource2) resolver.getTransformedSource(this);
    }

    @Override
    public Class<? extends AbstractEntity<?>> sourceType() {
        return entityMetadata.getType();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((entityMetadata == null) ? 0 : entityMetadata.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof TypeBasedSource1)) {
            return false;
        }
        final TypeBasedSource1 other = (TypeBasedSource1) obj;
        if (entityMetadata == null) {
            if (other.entityMetadata != null) {
                return false;
            }
        } else if (!entityMetadata.equals(other.entityMetadata)) {
            return false;
        }
        return true;
    }

    public PersistedEntityMetadata<? extends AbstractEntity<?>> getEntityMetadata() {
        return entityMetadata;
    }
}