package ua.com.fielden.platform.eql.stage2.elements;

import ua.com.fielden.platform.entity.AbstractEntity;

public class QrySource2BasedOnPersistentType extends AbstractQrySource2 {
    private final Class<? extends AbstractEntity<?>> sourceType;

    public QrySource2BasedOnPersistentType(final Class<? extends AbstractEntity<?>> sourceType) {
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
        if (!(obj instanceof QrySource2BasedOnPersistentType)) {
            return false;
        }
        final QrySource2BasedOnPersistentType other = (QrySource2BasedOnPersistentType) obj;
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