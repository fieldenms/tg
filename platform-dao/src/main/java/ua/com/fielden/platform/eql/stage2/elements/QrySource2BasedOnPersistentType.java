package ua.com.fielden.platform.eql.stage2.elements;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.meta.EntityInfo;

public class QrySource2BasedOnPersistentType extends AbstractElement2 implements IQrySource2 {
    private final EntityInfo entityInfo;
    private final String alias;

    public QrySource2BasedOnPersistentType(final EntityInfo entityInfo, final String alias, final int contextId) {
        super(contextId);
        this.entityInfo = entityInfo;
        this.alias = alias;   
    }

    public QrySource2BasedOnPersistentType(final EntityInfo entityInfo, final int contextId) {
        this(entityInfo, null, contextId);               
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((entityInfo == null) ? 0 : entityInfo.javaType().hashCode());
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
        if (entityInfo == null) {
            if (other.entityInfo != null) {
                return false;
            }
        } else if (!entityInfo.javaType().equals(other.entityInfo.javaType())) {
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
    public EntityInfo entityInfo() {
        return entityInfo;
    }

    @Override
    public String alias() {
        return alias;
    }
}