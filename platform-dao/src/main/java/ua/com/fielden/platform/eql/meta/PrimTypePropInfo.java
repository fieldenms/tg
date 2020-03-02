package ua.com.fielden.platform.eql.meta;

import java.util.Objects;

import ua.com.fielden.platform.eql.stage1.elements.operands.Expression1;

public class PrimTypePropInfo<T> extends AbstractPropInfo<T> {
    private final Class<T> propType;

    public PrimTypePropInfo(final String name, final Object hibType, final Class<T> propType) {
        this(name, hibType, propType, null);
    }
    
    public PrimTypePropInfo(final String name, final Object hibType, final Class<T> propType, final Expression1 expression) {
        super(name, hibType, expression);
        this.propType = propType;
    }

    @Override
    public ResolutionContext resolve(final ResolutionContext context) {
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