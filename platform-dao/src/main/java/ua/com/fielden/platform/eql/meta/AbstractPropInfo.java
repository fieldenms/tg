package ua.com.fielden.platform.eql.meta;

import ua.com.fielden.platform.eql.s1.elements.Expression1;

public abstract class AbstractPropInfo implements IResolvable {
    private final String name;
    private final EntityInfo parent;
    private final Expression1 expression;
    private final boolean leaf;

    @Override
    public String toString() {
        return parent + "." + name + (expression == null ? " no expr " : " has expr ");
    }

    public AbstractPropInfo(final String name, final EntityInfo parent, final Expression1 expression, final boolean leaf) {
        this.name = name;
        this.parent = parent;
        this.expression = expression;
        this.leaf = leaf;
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