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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((propType == null) ? 0 : propType.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof PrimTypePropInfo)) {
            return false;
        }
        final PrimTypePropInfo other = (PrimTypePropInfo) obj;
        if (propType == null) {
            if (other.propType != null) {
                return false;
            }
        } else if (!propType.equals(other.propType)) {
            return false;
        }
        return true;
    }
}