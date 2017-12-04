package ua.com.fielden.platform.eql.meta;

import ua.com.fielden.platform.entity.AbstractEntity;

public class EntityTypePropInfo<T extends AbstractEntity<?>, PARENT extends AbstractEntity<?>> extends AbstractPropInfo<T, PARENT> {
    private final EntityInfo<T> propEntityInfo;

    @Override
    public String toString() {
        return super.toString() + ": " + propEntityInfo.javaType().getSimpleName();
    }

    public EntityTypePropInfo(final String name, final EntityInfo<PARENT> parent, final EntityInfo<T> propEntityInfo) {
        super(name, parent);
        this.propEntityInfo = propEntityInfo;
    }

    protected EntityInfo<T> getPropEntityInfo() {
        return propEntityInfo;
    }

    @Override
    public AbstractPropInfo<?, ?> resolve(final String dotNotatedSubPropName) {
        return dotNotatedSubPropName != null ? getPropEntityInfo().resolve(dotNotatedSubPropName) : this;
    }

    @Override
    public Class<T> javaType() {
        return propEntityInfo.javaType();
    }
}