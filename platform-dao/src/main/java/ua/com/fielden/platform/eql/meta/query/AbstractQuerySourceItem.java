package ua.com.fielden.platform.eql.meta.query;

import jakarta.annotation.Nullable;
import ua.com.fielden.platform.eql.meta.CalcPropInfo;

import java.util.Objects;

/**
 * A structure that represents resolution-related info for a query source item of type {@code T} within a query source.
 *
 * @author TG Team
 */
public abstract class AbstractQuerySourceItem<T> implements IResolvable<T> {
    public final String name; //shouldn't contain dots
    public final @Nullable CalcPropInfo expression;
    public final @Nullable Object hibType;

    public AbstractQuerySourceItem(final String name, final @Nullable Object hibType, final @Nullable CalcPropInfo expression) {
        this.name = name;
        this.expression = expression;
        this.hibType = hibType;
    }

    public abstract AbstractQuerySourceItem<T> cloneWithoutExpression();

    /**
     * Represents a calculated property explicitly defined at the Entity level.
     * @return
     */
    public boolean isExplicitlyCalculated() {
    	throw new UnsupportedOperationException("TODO implement and use");
    }

    /**
     * Represents either a key for Composite Entity or a common property for a Union Entity.
     * @return
     */
    public boolean isImplicitlyCalculated() {
    	throw new UnsupportedOperationException("TODO implement and use");
    }

    public boolean hasExpression() {
        return expression != null;
    }

    public boolean isCalculatedPropertyThatCouldBeMaterialisedAsSqlColumn() {
        return expression != null && !expression.forTotals() && !expression.implicit();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + name.hashCode();
        result = prime * result + ((hibType == null) ? 0 : hibType.hashCode());
        result = prime * result + ((expression == null) ? 0 : expression.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof AbstractQuerySourceItem)) {
            return false;
        }

        final AbstractQuerySourceItem<?> other = (AbstractQuerySourceItem<?>) obj;

        return Objects.equals(name, other.name) && Objects.equals(hibType, other.hibType) && Objects.equals(expression, other.expression);
    }
}
