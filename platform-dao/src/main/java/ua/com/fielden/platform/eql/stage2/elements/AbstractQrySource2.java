package ua.com.fielden.platform.eql.stage2.elements;

import java.util.Objects;

import ua.com.fielden.platform.eql.meta.EntityInfo;

public abstract class AbstractQrySource2 {
    public final String contextId;
    public final String alias;
    public final EntityInfo<?> entityInfo;
    
    protected AbstractQrySource2(final String contextId, final String alias, final EntityInfo<?> entityInfo) {
        this.contextId = contextId;
        this.alias = alias;
        this.entityInfo = Objects.requireNonNull(entityInfo);
    }
    
    public String alias() {
        return alias;
    }
    
    public String contextId() {
        return contextId;
    }
    
    public EntityInfo<?> entityInfo() {
        return entityInfo;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + contextId.hashCode();
        result = prime * result + ((alias == null) ? 0 : alias.hashCode());
        result = prime * result + entityInfo.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof AbstractQrySource2)) {
            return false;
        }
        
        final AbstractQrySource2 other = (AbstractQrySource2) obj;
        //System.out.println("equals");
        return Objects.equals(contextId, other.contextId) && Objects.equals(alias, other.alias) && Objects.equals(entityInfo, other.entityInfo);
   }
}