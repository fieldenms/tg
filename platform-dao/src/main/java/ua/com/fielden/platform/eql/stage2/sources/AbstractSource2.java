package ua.com.fielden.platform.eql.stage2.sources;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.meta.query.QuerySourceInfo;
import ua.com.fielden.platform.utils.ToString;

import java.util.Objects;

public abstract class AbstractSource2 implements ToString.IFormattable {
    public final Integer id;
    /** Alias or {@code null}. */
    public final String alias;
    public final QuerySourceInfo<?> querySourceInfo;
    public final boolean isExplicit; 
    public final boolean isPartOfCalcProp;
    
    protected AbstractSource2(final Integer id, final String alias, final QuerySourceInfo<?> querySourceInfo, final boolean isExplicit, boolean isPartOfCalcProp) {
        this.id = Objects.requireNonNull(id);
        this.alias = alias;
        this.querySourceInfo = Objects.requireNonNull(querySourceInfo);
        this.isExplicit = isExplicit;
        this.isPartOfCalcProp = isPartOfCalcProp;
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
    
    public boolean isExplicit() {
        return isExplicit;
    }
    
    public boolean isPartOfCalcProp() {
        return isPartOfCalcProp;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id.hashCode();
        result = prime * result + ((alias == null) ? 0 : alias.hashCode());
        result = prime * result + querySourceInfo.hashCode();
        result = prime * result + (isExplicit ? 1231 : 1237);
        result = prime * result + (isPartOfCalcProp ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj
               || obj instanceof AbstractSource2 that
                  && Objects.equals(id, that.id)
                  && Objects.equals(alias, that.alias)
                  && Objects.equals(querySourceInfo, that.querySourceInfo)
                  && isExplicit == that.isExplicit
                  && isPartOfCalcProp == that.isPartOfCalcProp;
   }

   @Override
   public String toString() {
       return toString(ToString.separateLines);
   }

   @Override
   public String toString(final ToString.IFormat format) {
       return format.toString(this)
               .add("id", id)
               .addIfNotNull("alias", alias)
               .add("querySourceInfo", querySourceInfo)
               .add("isExplicit", isExplicit)
               .add("isPartOfCalcProp", isPartOfCalcProp)
               .pipe(this::addToString)
               .$();
   }

   protected ToString addToString(final ToString toString) {
       return toString;
   }

}
