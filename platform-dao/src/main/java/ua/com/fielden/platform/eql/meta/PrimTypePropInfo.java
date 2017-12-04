package ua.com.fielden.platform.eql.meta;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.exceptions.EqlException;

public class PrimTypePropInfo<T, PARENT extends AbstractEntity<?>> extends AbstractPropInfo<T, PARENT> {
    private final Class<T> propType;

    public PrimTypePropInfo(final String name, final Class<T> propType, final EntityInfo<PARENT> parent) {
        super(name, parent);
        this.propType = propType;
    }

    @Override
    public AbstractPropInfo<?, ?> resolve(final String dotNotatedSubPropName) {
        if (dotNotatedSubPropName != null) {
            throw new EqlException("Resolve method should get [null] as parameter instead of [" + dotNotatedSubPropName + "].\nAdditional info: name = " + getName() + "; propType = " + propType + ";");
        }
        return this;
    }

    @Override
    public Class<T> javaType() {
        return propType;
    }
    
    @Override
    public String toString() {
        return super.toString() + ": " + propType.getSimpleName();
    }
}