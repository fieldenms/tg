package ua.com.fielden.platform.eql.stage1.sources;

import java.util.Objects;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.stage2.sources.ISource2;

public abstract class AbstractSource1<S2 extends ISource2<?>> implements ISource1<S2> {

    /**
     * Business name for query source. Can be also dot.notated, but should stick to property alias naming rules (e.g. no dots in beginning/end).
     */
    protected final String alias;
    public final Integer id;
    private final Class<? extends AbstractEntity<?>> sourceType;

    public AbstractSource1(final Class<? extends AbstractEntity<?>> sourceType, final String alias, final Integer id) {
        this.id = id; // id is not taken into consideration in hashCode() and equals(..) methods on purpose -- Stage1 elements have no need to reference uniquely one another.
        this.alias = alias;
        this.sourceType = Objects.requireNonNull(sourceType);
    }

    @Override
    public String getAlias() {
        return alias;
    }
    
    @Override
    public Class<? extends AbstractEntity<?>> sourceType() {
        return sourceType;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((alias == null) ? 0 : alias.hashCode());
        result = prime * result + sourceType.hashCode();
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

        return Objects.equals(alias, other.alias) && Objects.equals(sourceType, other.sourceType);
    }
}