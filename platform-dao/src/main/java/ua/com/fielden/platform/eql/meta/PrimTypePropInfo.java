package ua.com.fielden.platform.eql.meta;

import java.util.Objects;

import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.eql.stage1.PropResolutionProgress;

public class PrimTypePropInfo<T> extends AbstractPropInfo<T> {
    private final Class<T> propType;

    public PrimTypePropInfo(final String name, final Object hibType, final Class<T> propType) {
        this(name, hibType, propType, null, false);
    }
    
    public PrimTypePropInfo(final String name, final Object hibType, final Class<T> propType, final ExpressionModel expression, final boolean implicit) {
        super(name, hibType, expression, implicit);
        this.propType = propType;
    }

    @Override
    public AbstractPropInfo<T> cloneWithoutExpression() {
        return new PrimTypePropInfo<T>(name, hibType, propType);
    }
    
    @Override
    public PropResolutionProgress resolve(final PropResolutionProgress context) {
        return context;
    }
    
    @Override
    public Class<T> javaType() {
        return propType;
    }
    
    @Override
    public String toString() {
        return String.format("%20s %20s", name, propType.getSimpleName());

    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + propType.hashCode();
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

        return Objects.equals(propType, other.propType);
    }
}