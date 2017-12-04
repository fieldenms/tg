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
    public AbstractPropInfo resolve(final String dotNotatedSubPropName) {
        if (dotNotatedSubPropName != null) {
            throw new IllegalStateException("Resolve method should get [null] as parameter instead of [" + dotNotatedSubPropName + "].\nAdditional info: name = " + getName() + "; propType = " + getPropType() + ";");
        }
        return this;
    }

    @Override
    public Class javaType() {
        return getPropType();
    }
}