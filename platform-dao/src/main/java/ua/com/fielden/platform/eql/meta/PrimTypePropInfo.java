package ua.com.fielden.platform.eql.meta;

public class PrimTypePropInfo extends AbstractPropInfo {
    private final Class propType;

    @Override
    public String toString() {
        return super.toString() + ": " + propType.getSimpleName();
    }

    public PrimTypePropInfo(final String name, final EntityInfo parent, final Class propType) {
	super(name, parent);
	this.propType = propType;
    }

    protected Class getPropType() {
        return propType;
    }

    @Override
    public Object resolve(final String dotNotatedSubPropName) {
	assert(dotNotatedSubPropName == null);
	return this;
    }
}