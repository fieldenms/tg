package ua.com.fielden.platform.eql.meta;

public class ComponentTypePropInfo extends AbstractPropInfo {
    private final EntityInfo propEntityInfo;

    @Override
    public String toString() {
        return super.toString() + ": " + propEntityInfo;
    }

    public ComponentTypePropInfo(final String name, final EntityInfo parent, final EntityInfo propEntityInfo) {
        super(name, parent);
        this.propEntityInfo = propEntityInfo;
    }

    protected EntityInfo getPropEntityInfo() {
        return propEntityInfo;
    }

    @Override
    public AbstractPropInfo resolve(final String dotNotatedSubPropName) {
        return dotNotatedSubPropName != null ? getPropEntityInfo().resolve(dotNotatedSubPropName) : this;
    }

    @Override
    public Class javaType() {
        return propEntityInfo.javaType();
    }
}