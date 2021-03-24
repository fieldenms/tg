package ua.com.fielden.platform.eql.stage2.sources;

import java.util.Objects;

import ua.com.fielden.platform.eql.meta.EntityInfo;

public abstract class AbstractQrySource2 {
    public final String id;
    public final String alias;
    public final EntityInfo<?> entityInfo;
    
    protected AbstractQrySource2(final String id, final String alias, final EntityInfo<?> entityInfo) {
        this.id = Objects.requireNonNull(id);
        this.alias = alias;
        this.entityInfo = Objects.requireNonNull(entityInfo);
    }
    
    public String alias() {
        return alias;
    }
    
    public String id() {
        return id;
    }
    
    public EntityInfo<?> entityInfo() {
        return entityInfo;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id.hashCode();
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

        return Objects.equals(id, other.id) && Objects.equals(alias, other.alias) && Objects.equals(entityInfo, other.entityInfo);
   }
}