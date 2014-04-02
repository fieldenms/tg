package ua.com.fielden.platform.eql.s2.elements;

import ua.com.fielden.platform.entity.AbstractEntity;

public class TypeBasedSource2 extends AbstractSource2 {
    private final Class<? extends AbstractEntity<?>> sourceType;

    public TypeBasedSource2(final Class<? extends AbstractEntity<?>> sourceType) {
        this.sourceType = sourceType;
    }

    @Override
    public Class<? extends AbstractEntity<?>> sourceType() {
        return sourceType;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((sourceType == null) ? 0 : sourceType.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof TypeBasedSource2)) {
            return false;
        }
        final TypeBasedSource2 other = (TypeBasedSource2) obj;
        if (sourceType == null) {
            if (other.sourceType != null) {
                return false;
            }
        } else if (!sourceType.equals(other.sourceType)) {
            return false;
        }
        return true;
    }
}