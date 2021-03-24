package ua.com.fielden.platform.eql.stage1.sources;

import java.util.Objects;

import ua.com.fielden.platform.eql.stage1.TransformationContext;
import ua.com.fielden.platform.eql.stage2.sources.ISource2;

public abstract class AbstractSource1<S2 extends ISource2<?>> implements ISource1<S2> {

    /**
     * Business name for query source. Can be also dot.notated, but should stick to property alias naming rules (e.g. no dots in beginning/end).
     */
    protected final String alias;
    public final int id;
    
    public AbstractSource1(final String alias, final int id) {
        this.id = id; // id is not taken into consideration in hashCode() and equals(..) methods on purpose -- Stage1 elements have no need to reference uniquely one another.
        this.alias = alias;
    }

    @Override
    public String getAlias() {
        return alias;
    }
    
    public String transformId(final TransformationContext context) {
        return context.sourceIdPrefix == null ? Integer.toString(id) : context.sourceIdPrefix + "_" + Integer.toString(id);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((alias == null) ? 0 : alias.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof AbstractSource1)) {
            return false;
        }
        
        final AbstractSource1<?> other = (AbstractSource1<?>) obj;
        
        return Objects.equals(alias, other.alias);
    }
}