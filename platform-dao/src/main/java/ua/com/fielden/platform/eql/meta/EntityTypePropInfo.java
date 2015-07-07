package ua.com.fielden.platform.eql.meta;

import ua.com.fielden.platform.eql.s1.elements.Expression1;

public class EntityTypePropInfo extends AbstractPropInfo {
    private final EntityInfo propEntityInfo;

    @Override
    public String toString() {
        return super.toString() + propEntityInfo.javaType().getSimpleName();
    }

    public EntityTypePropInfo(final String name, final EntityInfo parent, final EntityInfo propEntityInfo, final Expression1 expression) {
        super(name, parent, expression);
        this.propEntityInfo = propEntityInfo;
    }

    protected EntityInfo getPropEntityInfo() {
        return propEntityInfo;
    }

    @Override
    public ResolutionPath resolve(final String dotNotatedSubPropName) {
        ResolutionPath result = new ResolutionPath();
        
        if (dotNotatedSubPropName != null) {
            result.add(getPropEntityInfo().resolve(dotNotatedSubPropName));
        } else {
            result.add(this);
        }

        return result;
    }

    @Override
    public Class javaType() {
        return propEntityInfo.javaType();
    }
}