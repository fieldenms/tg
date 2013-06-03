package ua.com.fielden.platform.eql.meta;

import ua.com.fielden.platform.eql.s1.elements.Expression1;

public abstract class AbstractPropInfo implements IResolvable {
    private final String name;
    private final IResolvable parent;
    private final Expression1 expression;

    @Override
    public String toString() {
        return parent + "." + name;
    }

    public AbstractPropInfo(final String name, final IResolvable parent, final Expression1 expression) {
	this.name = name;
	this.parent = parent;
	this.expression = expression;
    }

    protected String getName() {
        return name;
    }

    protected IResolvable getParent() {
        return parent;
    }

    public abstract Object resolve(String dotNotatedSubPropName);
}