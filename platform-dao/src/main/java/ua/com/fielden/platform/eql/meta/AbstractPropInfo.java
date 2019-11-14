package ua.com.fielden.platform.eql.meta;

import java.util.Objects;

import ua.com.fielden.platform.eql.stage1.elements.operands.Expression1;

/**
 * A structure that captures a query source yield-able property resolution related info within a query source of type <code>PARENT</code>. 
 * 
 * @author TG Team
 *
 */
public abstract class AbstractPropInfo<T> implements IResolvable<T> {
    public final String name;
    public final Expression1 expression;

    /**
     * Principal constructor.
     * 
     * @param name - property yield alias or property name.
     */
    public AbstractPropInfo(final String name) {
        this(name, null);
    }

    public AbstractPropInfo(final String name, final Expression1 expression) {
        this.name = name;
        this.expression = expression;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + name.hashCode();
        result = prime * result + ((expression == null) ? 0 : expression.hashCode());
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
        
        return Objects.equals(name, other.name) && Objects.equals(expression, other.expression);
    }
}