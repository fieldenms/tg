package ua.com.fielden.platform.eql.meta.query;

import java.util.Objects;

import ua.com.fielden.platform.entity.query.model.ExpressionModel;

/**
 * A structure that represents resolution-related info for a query source item of type {@code T} within a query source.
 * 
 * @author TG Team
 *
 */
public abstract class AbstractQuerySourceInfoItem<T> implements IResolvable<T> {
    public final String name; //shouldn't contain dots
    public final ExpressionModel expression;
    public final boolean implicit;
    public final Object hibType;

    public AbstractQuerySourceInfoItem(final String name, final Object hibType, final ExpressionModel expression, final boolean implicit) {
        this.name = name;
        this.expression = expression;
        this.hibType = hibType;
        this.implicit = implicit;
    }
    
    public AbstractQuerySourceInfoItem(final String name, final Object hibType, final ExpressionModel expression) {
        this(name, hibType, expression, false);
    }

    public abstract AbstractQuerySourceInfoItem<T> cloneWithoutExpression();
    
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

    public boolean hasAggregation() {
        return expression != null && expression.containsAggregations();
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + name.hashCode();
        result = prime * result + ((hibType == null) ? 0 : hibType.hashCode());
        result = prime * result + ((expression == null) ? 0 : expression.hashCode());
        result = prime * result + (implicit ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof AbstractQuerySourceInfoItem)) {
            return false;
        }

        final AbstractQuerySourceInfoItem<?> other = (AbstractQuerySourceInfoItem<?>) obj;
        
        return Objects.equals(name, other.name) && Objects.equals(hibType, other.hibType) && Objects.equals(expression, other.expression) && Objects.equals(implicit, other.implicit);
    }
}