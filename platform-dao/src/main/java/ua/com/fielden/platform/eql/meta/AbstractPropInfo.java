package ua.com.fielden.platform.eql.meta;

public abstract class AbstractPropInfo implements IResolvable {
    private final String name;
    private final IResolvable parent;

    @Override
    public String toString() {
        return parent + "." + name;
    }

    public AbstractPropInfo(final String name, final IResolvable parent) {
	this.name = name;
	this.parent = parent;
    }

    protected String getName() {
        return name;
    }

    protected IResolvable getParent() {
        return parent;
    }

    public abstract Object resolve(String dotNotatedSubPropName);
}