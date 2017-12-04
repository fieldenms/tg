package ua.com.fielden.platform.eql.meta;

import ua.com.fielden.platform.entity.query.exceptions.EqlException;

public class ComponentTypePropInfo extends AbstractPropInfo {
    private final EntityInfo propEntityInfo;

    @Override
    public String toString() {
        return super.toString() + ": " + propEntityInfo;
    }

    public ComponentTypePropInfo(final String name, final EntityInfo parent, final EntityInfo propEntityInfo) {
        super(name, parent);
        //TODO this class should be refactored to support components.
        throw new EqlException("Not yet.");
//        this.propEntityInfo = propEntityInfo;
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