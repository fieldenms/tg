package ua.com.fielden.platform.eql.meta;

import ua.com.fielden.platform.eql.s1.elements.Expression1;

public class PrimTypePropInfo extends AbstractPropInfo {
    private final Class propType;

    @Override
    public String toString() {
        return super.toString() + ": " + propType.getSimpleName();
    }

    public PrimTypePropInfo(final String name, final EntityInfo parent, final Class propType, final Expression1 expression) {
        super(name, parent, expression, true);
        this.propType = propType;
    }

    protected Class getPropType() {
        return propType;
    }

    @Override
    public AbstractPropInfo resolve(final String dotNotatedSubPropName) {
        if (dotNotatedSubPropName != null) {
            System.out.println("name = " + getName() + "; propType = " + getPropType() + "; resolving NOT NULL -- " + dotNotatedSubPropName);
            throw new IllegalStateException("Resolve method should get [null] as parameter");
        }
        return this;
    }

    @Override
    public Class javaType() {
        return getPropType();
    }
}