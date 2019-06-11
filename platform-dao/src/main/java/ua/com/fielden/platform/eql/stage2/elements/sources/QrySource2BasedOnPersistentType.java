package ua.com.fielden.platform.eql.stage2.elements.sources;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.meta.EntityInfo;
import ua.com.fielden.platform.eql.stage2.elements.AbstractElement2;

public class QrySource2BasedOnPersistentType extends AbstractElement2 implements IQrySource2 {
    private final Class<? extends AbstractEntity<?>> sourceType;
    private final EntityInfo entityInfo;
    private final String alias;

    public QrySource2BasedOnPersistentType(final Class<? extends AbstractEntity<?>> sourceType, final EntityInfo entityInfo, final String alias, final int contextId) {
        super(contextId);
        this.sourceType = sourceType;
        this.entityInfo = entityInfo;
        this.alias = alias;               
    }

    public QrySource2BasedOnPersistentType(final Class<? extends AbstractEntity<?>> sourceType, final EntityInfo entityInfo, final int contextId) {
        this(sourceType, entityInfo, null, contextId);               
    }

    @Override
    public Class<? extends AbstractEntity<?>> sourceType() {
        return sourceType;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((sourceType == null) ? 0 : sourceType.hashCode());
        result = prime * result + ((alias == null) ? 0 : alias.hashCode());
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