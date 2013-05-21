package ua.com.fielden.platform.eql.meta;

public abstract class AbstractPropInfo {
    private final String name;
    private final EntityInfo parent;

    @Override
    public String toString() {
        return parent + "." + name;
    }

    public AbstractPropInfo(final String name, final EntityInfo parent) {
	this.name = name;
	this.parent = parent;
    }

    protected String getName() {
        return name;
    }

    protected EntityInfo getParent() {
        return parent;
    }

    abstract Object resolve(String dotNotatedSubPropName);
}