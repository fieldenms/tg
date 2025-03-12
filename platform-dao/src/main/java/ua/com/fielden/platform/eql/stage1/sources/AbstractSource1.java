package ua.com.fielden.platform.eql.stage1.sources;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.stage2.sources.ISource2;
import ua.com.fielden.platform.utils.ToString;

import java.util.Objects;

public abstract class AbstractSource1<T extends ISource2<?>> implements ISource1<T>, ToString.IFormattable {

    private final Class<? extends AbstractEntity<?>> sourceType;
    /**
     * Business name for query source. Can be also dot.notated, but should stick to property alias naming rules (e.g. no dots in beginning/end).
     * Can be {@code null}.
     */
    protected final String alias;
    public final Integer id;

    /**
     * @param alias  the alias of this source or {@code null}
     */
    public AbstractSource1(final Class<? extends AbstractEntity<?>> sourceType, final String alias, final Integer id) {
        this.id = id;
        this.alias = alias;
        this.sourceType = Objects.requireNonNull(sourceType);
    }

    @Override
    public Class<? extends AbstractEntity<?>> sourceType() {
        return sourceType;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id.hashCode();
        result = prime * result + ((alias == null) ? 0 : alias.hashCode());
        result = prime * result + sourceType.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj
               || obj instanceof AbstractSource1 that
                  && Objects.equals(id, that.id)
                  && Objects.equals(alias, that.alias)
                  && Objects.equals(sourceType, that.sourceType);
    }

    @Override
    public String toString() {
        return toString(ToString.separateLines);
    }

    protected ToString addToString(final ToString toString) {
        return toString;
    }

    @Override
    public String toString(final ToString.IFormat format) {
        return format.toString(this)
                .add("source", sourceType)
                .addIfNotNull("alias", alias)
                .add("id", id)
                .pipe(this::addToString)
                .$();
    }

}
