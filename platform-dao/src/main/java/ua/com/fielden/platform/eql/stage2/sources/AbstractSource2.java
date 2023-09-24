package ua.com.fielden.platform.eql.stage2.sources;

import java.util.Objects;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.meta.query.QuerySourceInfo;

public abstract class AbstractSource2 {
    public final Integer id;
    public final String alias;
    public final QuerySourceInfo<?> querySourceInfo;
    
    protected AbstractSource2(final Integer id, final String alias, final QuerySourceInfo<?> querySourceInfo) {
        this.id = Objects.requireNonNull(id);
        this.alias = alias;
        this.querySourceInfo = Objects.requireNonNull(querySourceInfo);
    }
    
    public String alias() {
        return alias;
    }
    
    public Integer id() {
        return id;
    }
    
    public QuerySourceInfo<?> querySourceInfo() {
        return querySourceInfo;
    }
    
    public Class<? extends AbstractEntity<?>> sourceType() {
        return querySourceInfo.javaType();
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id.hashCode();
        result = prime * result + ((alias == null) ? 0 : alias.hashCode());
        result = prime * result + querySourceInfo.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof AbstractSource2)) {
            return false;
        }
        
        final AbstractSource2 other = (AbstractSource2) obj;

        return Objects.equals(id, other.id) && Objects.equals(alias, other.alias) && Objects.equals(querySourceInfo, other.querySourceInfo);
   }
}