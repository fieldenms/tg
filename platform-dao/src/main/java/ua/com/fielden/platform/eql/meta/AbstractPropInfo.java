package ua.com.fielden.platform.eql.meta;

import ua.com.fielden.platform.eql.s1.elements.Expression1;

public abstract class AbstractPropInfo implements IResolvable {
    private final String name;
    private final EntityInfo parent;
    private final Expression1 expression;

    @Override
    public String toString() {
        return String.format("%-32s%-12s", parent + "." + name, (expression == null ? "no expr" : "has expr"));
    }

    public AbstractPropInfo(final String name, final EntityInfo parent, final Expression1 expression) {
        this.name = name;
        this.parent = parent;
        this.expression = expression;
        parent.getProps().put(name, this);
    }

    protected String getName() {
        return name;
    }

    protected EntityInfo getParent() {
        return parent;
    }

    public Expression1 getExpression() {
        return expression;
    }
}