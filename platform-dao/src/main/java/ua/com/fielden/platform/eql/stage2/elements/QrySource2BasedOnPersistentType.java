package ua.com.fielden.platform.eql.stage2.elements;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.meta.EntityInfo;

public class QrySource2BasedOnPersistentType implements IQrySource2 {
    private final Class<? extends AbstractEntity<?>> sourceType;
    private final EntityInfo entityInfo;
    private final String alias;

    public QrySource2BasedOnPersistentType(final Class<? extends AbstractEntity<?>> sourceType, final EntityInfo entityInfo, final String alias) {
        this.sourceType = sourceType;
        this.entityInfo = entityInfo;
        this.alias = alias;               
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
        result = prime * result + ((alias == null) ? 0 : alias.hashCode());
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
        if (alias == null) {
            if (other.alias != null) {
                return false;
            }
        } else if (!alias.equals(other.alias)) {
            return false;
        }

        
        return true;
    }

    @Override
    public EntityInfo<?> entityInfo() {
        return entityInfo;
    }

    @Override
    public String alias() {
        return alias;
    }
}