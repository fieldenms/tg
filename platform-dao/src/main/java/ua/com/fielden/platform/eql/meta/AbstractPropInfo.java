package ua.com.fielden.platform.eql.meta;

import java.util.Objects;

import ua.com.fielden.platform.entity.query.model.ExpressionModel;

/**
 * A structure that captures a query source yield-able property resolution related info within a query source of type <code>PARENT</code>. 
 * 
 * @author TG Team
 *
 */
public abstract class AbstractPropInfo<T> implements IResolvable<T> {
    public final String name; //shouldn't contain dots
    public final ExpressionModel expression;
    public final boolean implicit;
    public final Object hibType;

    public AbstractPropInfo(final String name, final Object hibType, final ExpressionModel expression, final boolean implicit) {
        this.name = name;
        this.expression = expression;
        this.hibType = hibType;
        this.implicit = implicit;
    }
    
    public AbstractPropInfo(final String name, final Object hibType, final ExpressionModel expression) {
        this(name, hibType, expression, false);
    }

    public abstract AbstractPropInfo<T> cloneRenamed(final String newName);
    
    public abstract AbstractPropInfo<T> cloneWithoutExpression();
    
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
        result = prime * result + hibType.hashCode();
        result = prime * result + ((expression == null) ? 0 : expression.hashCode());
        result = prime * result + (implicit ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof AbstractPropInfo)) {
            return false;
        }

        final AbstractPropInfo<?> other = (AbstractPropInfo<?>) obj;
        
        return Objects.equals(name, other.name) && Objects.equals(hibType, other.hibType) && Objects.equals(expression, other.expression) && Objects.equals(implicit, other.implicit);
    }
}