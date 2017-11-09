package ua.com.fielden.platform.eql.s2.elements;

import org.hibernate.type.Type;

import ua.com.fielden.platform.entity.query.ICompositeUserTypeInstantiate;
import ua.com.fielden.platform.entity.query.IUserTypeInstantiate;
import static ua.com.fielden.platform.utils.EntityUtils.isPersistedEntityType;

public class ResultQueryYieldDetails2 implements Comparable<ResultQueryYieldDetails2> {

    private final String name;
    private final Class javaType;
    private final Object hibType;
    private final String column;
    private final boolean nullable;
    private final YieldDetailsType yieldDetailsType;

    public ResultQueryYieldDetails2(final String name, final Class javaType, final Object hibType, final String column, final boolean nullable, final YieldDetailsType yieldDetailsType) {
        super();
        this.name = name;
        this.javaType = javaType;
        this.hibType = hibType;
        this.column = column;
        this.nullable = nullable;
        this.yieldDetailsType = yieldDetailsType;
    }

    public ResultQueryYieldDetails2(final String name, final Class javaType, final Object hibType, final String column, final YieldDetailsType yieldDetailsType) {
        this(name, javaType, hibType, column, false, yieldDetailsType);
    }

    public Type getHibTypeAsType() {
        return hibType instanceof Type ? (Type) hibType : null;
    }

    public IUserTypeInstantiate getHibTypeAsUserType() {
        return hibType instanceof IUserTypeInstantiate ? (IUserTypeInstantiate) hibType : null;
    }

    public ICompositeUserTypeInstantiate getHibTypeAsCompositeUserType() {
        return hibType instanceof ICompositeUserTypeInstantiate ? (ICompositeUserTypeInstantiate) hibType : null;
    }

    @Override
    public String toString() {
        return "\nname = " + name + "\njavaType = " + (javaType != null ? javaType.getSimpleName() : javaType) + "\nhibType = "
                + (hibType != null ? hibType.getClass().getSimpleName() : hibType) + "\ncolumn(s) = " + column;
    }

    public boolean isCompositeProperty() {
        return yieldDetailsType.equals(YieldDetailsType.COMPOSITE_TYPE_HEADER);
    }

    public boolean isEntity() {
        return isPersistedEntityType(javaType) && yieldDetailsType.equals(YieldDetailsType.USUAL_PROP);
    }

    public boolean isUnionEntity() {
        return yieldDetailsType.equals(YieldDetailsType.UNION_ENTITY_HEADER);
    }

    public String getTypeString() {
        if (hibType != null) {
            return hibType.getClass().getName();
        } else {
            return null;
        }
    }

    @Override
    public int compareTo(final ResultQueryYieldDetails2 o) {
        return name.compareTo(o.name);
    }

    public String getName() {
        return name;
    }

    public Class getJavaType() {
        return javaType;
    }

    public Object getHibType() {
        return hibType;
    }

    public String getColumn() {
        return column;
    }

    public boolean isNullable() {
        return nullable;
    }

    public static enum YieldDetailsType {
        USUAL_PROP, //
        AGGREGATED_EXPRESSION, // as per PropertyMetadata property
        UNION_ENTITY_HEADER, //
        COMPOSITE_TYPE_HEADER;
    }

    public YieldDetailsType getYieldDetailsType() {
        return yieldDetailsType;
    }
}