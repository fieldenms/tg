package ua.com.fielden.platform.eql.stage1.sources;

import java.util.Objects;

import ua.com.fielden.platform.eql.stage1.PropsResolutionContext;
import ua.com.fielden.platform.eql.stage2.sources.IQrySource2;

public abstract class AbstractQrySource1<S2 extends IQrySource2<?>> implements IQrySource1<S2> {

    /**
     * Business name for query source. Can be also dot.notated, but should stick to property alias naming rules (e.g. no dots in beginning/end).
     */
    protected final String alias;
    public final int contextId;
    
    public AbstractQrySource1(final String alias, final int contextId) {
        this.contextId = contextId; // contextId is not taken into consideration in hashCode() and equals(..) methods on purpose -- Stage1 elements have no need to reference uniquely one another.
        this.alias = alias;
    }

    @Override
    public String getAlias() {
        return alias;
    }
    
    public String getTransformedContextId(final PropsResolutionContext context) {
        return context.sourceId == null ? Integer.toString(contextId) : context.sourceId + "_" + Integer.toString(contextId);
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

        if (!(obj instanceof AbstractQrySource1)) {
            return false;
        }
        
        final AbstractQrySource1<?> other = (AbstractQrySource1<?>) obj;
        
        return Objects.equals(alias, other.alias);
    }
}