package ua.com.fielden.platform.eql.meta;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.eql.s1.elements.Expression1;

public class ComponentTypePropInfo extends AbstractPropInfo {
    private final EntityInfo propEntityInfo;

    @Override
    public String toString() {
        return super.toString() + ": " + propEntityInfo;
    }

    public ComponentTypePropInfo(final String name, final EntityInfo parent, final EntityInfo propEntityInfo, final Expression1 expression) {
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

        //return dotNotatedSubPropName != null ? getPropEntityInfo().resolve(dotNotatedSubPropName) : this;

        return result;
    }

    @Override
    public Class javaType() {
        return propEntityInfo.javaType();
    }
}