package ua.com.fielden.platform.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.type.Type;

import ua.com.fielden.platform.entity.query.ICompositeUserTypeInstantiate;
import ua.com.fielden.platform.entity.query.IUserTypeInstantiate;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.utils.EntityUtils;

public class PropertyPersistenceInfo implements Comparable<PropertyPersistenceInfo> {
    private final String name;
    private final Class javaType;
    private final Object hibType;
    private final PropertyPersistenceType type;
    private final boolean nullable;

    private final List<PropertyColumn> columns;
    private final ExpressionModel expressionModel;
    private final boolean aggregatedExpression; // contains aggregation function on the root level (i.e. Totals in entity centre tree)
    private final boolean virtual; // this property is limited to eQuery only - it has no real property on entity (the case with virtual generation of composite entity key by concatenation of all members.

    public boolean isCalculated() {
	return expressionModel != null;
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
	return "\nname = " + name + " javaType = " + (javaType != null ? javaType.getSimpleName() : javaType) + " hibType = "
		+ (hibType != null ? hibType/*.getClass().getSimpleName()*/ : hibType) + " type = " + type + "\ncolumn(s) = " + columns + " nullable = " + nullable;
    }

    public boolean isCompositeProperty() {
	return getHibTypeAsCompositeUserType() != null;
    }

    public boolean isEntity() {
	return EntityUtils.isPersistedEntityType(javaType) && !isCollection();
    }

    public boolean isCollection() {
	return type.equals(PropertyPersistenceType.COLLECTIONAL);
    }

    public boolean isId() {
	return type.equals(PropertyPersistenceType.ID);
    }

    public boolean isOne2OneId() {
	return type.equals(PropertyPersistenceType.ONE2ONE_ID);
    }

    public boolean isVersion() {
	return type.equals(PropertyPersistenceType.VERSION);
    }

    public String getTypeString() {
	if (hibType != null) {
	    return hibType.getClass().getName();
	} else {
	    return null;
	}
    }

    @Override
    public int compareTo(final PropertyPersistenceInfo o) {
	final boolean areEqual = this.equals(o);
	final int nameComp = name.compareTo(o.name);
	return nameComp != 0 ? nameComp : (areEqual ? 0 : 1);
    }

    public Set<PropertyPersistenceInfo> getCompositeTypeSubprops() {
	final Set<PropertyPersistenceInfo> result = new HashSet<PropertyPersistenceInfo>();
	if (hibType instanceof ICompositeUserTypeInstantiate) {
	    final List<String> subprops = Arrays.asList(((ICompositeUserTypeInstantiate) hibType).getPropertyNames());
	    final List<Object> subpropsTypes = Arrays.asList(((ICompositeUserTypeInstantiate) hibType).getPropertyTypes());
	    int index = 0;
	    for (final String subpropName : subprops) {
		final PropertyColumn column = columns.get(index);
		final Object hibType = subpropsTypes.get(index);
		result.add(new PropertyPersistenceInfo.Builder(name + "." + subpropName, ((Type) hibType).getReturnedClass(), nullable).column(column).type(PropertyPersistenceType.COMPOSITE_DETAILS).hibType(hibType).build());
		index = index + 1;
	    }
	}
	return result;
    }

    private PropertyPersistenceInfo(final Builder builder) {
	type = builder.type;
	name = builder.name;
	javaType = builder.javaType;
	hibType = builder.hibType;
	columns = builder.columns;
	nullable = builder.nullable;
	expressionModel = builder.expressionModel;
	aggregatedExpression = builder.aggregatedExpression;
	virtual = builder.virtual;
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

    public PropertyPersistenceType getType() {
	return type;
    }

    public List<PropertyColumn> getColumns() {
	return columns;
    }

    public PropertyColumn getColumn() {
	return columns.size() > 0 ? columns.get(0) : null;
    }

    public static class Builder {
	private final String name;
	private final Class javaType;
	private final boolean nullable;

	private Object hibType;
	private List<PropertyColumn> columns = new ArrayList<PropertyColumn>();
	private PropertyPersistenceType type = PropertyPersistenceType.PROP;
	private ExpressionModel expressionModel;
	private boolean aggregatedExpression = false;
	private boolean virtual = false;

	public PropertyPersistenceInfo build() {
	    return new PropertyPersistenceInfo(this);

	}

	public Builder(final String name, final Class javaType, final boolean nullable) {
	    this.name = name;
	    this.javaType = javaType;
	    this.nullable = nullable;
	}

	public Builder hibType(final Object val) {
	    hibType = val;
	    return this;
	}

	public Builder expression(final ExpressionModel val) {
	    expressionModel = val;
	    return this;
	}

	public Builder type(final PropertyPersistenceType val) {
	    type = val;
	    return this;
	}

	public Builder column(final PropertyColumn column) {
	    columns.add(column);
	    return this;
	}

	public Builder aggregatedExpression(final boolean val) {
	    aggregatedExpression = val;
	    return this;
	}

	public Builder virtual(final boolean val) {
	    virtual = val;
	    return this;
	}

	public Builder columns(final List<String> columns) {
	    columns.addAll(columns);
	    return this;
	}
    }

    public boolean isNullable() {
	return nullable;
    }

    public static enum PropertyPersistenceType {
	PROP, COLLECTIONAL, ENTITY, ID, ONE2ONE_ID, VERSION, PRIMITIVE_KEY, ENTITY_KEY, ENTITY_MEMBER_OF_COMPOSITE_KEY, PRIMITIVE_MEMBER_OF_COMPOSITE_KEY, COMPOSITE_DETAILS;
    }

    public ExpressionModel getExpressionModel() {
        return expressionModel;
    }

    public boolean isAggregatedExpression() {
        return aggregatedExpression;
    }

    public boolean isVirtual() {
        return virtual;
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + (aggregatedExpression ? 1231 : 1237);
	result = prime * result + ((columns == null) ? 0 : columns.hashCode());
	result = prime * result + ((expressionModel == null) ? 0 : expressionModel.hashCode());
	result = prime * result + ((hibType == null) ? 0 : hibType.hashCode());
	result = prime * result + ((javaType == null) ? 0 : javaType.hashCode());
	result = prime * result + ((name == null) ? 0 : name.hashCode());
	result = prime * result + (nullable ? 1231 : 1237);
	result = prime * result + ((type == null) ? 0 : type.hashCode());
	result = prime * result + (virtual ? 1231 : 1237);
	return result;
    }

    @Override
    public boolean equals(final Object obj) {
	if (this == obj) {
	    return true;
	}
	if (obj == null) {
	    return false;
	}
	if (!(obj instanceof PropertyPersistenceInfo)) {
	    return false;
	}
	final PropertyPersistenceInfo other = (PropertyPersistenceInfo) obj;
	if (aggregatedExpression != other.aggregatedExpression) {
	    return false;
	}
	if (columns == null) {
	    if (other.columns != null) {
		return false;
	    }
	} else if (!columns.equals(other.columns)) {
	    return false;
	}
	if (expressionModel == null) {
	    if (other.expressionModel != null) {
		return false;
	    }
	} else if (!expressionModel.equals(other.expressionModel)) {
	    return false;
	}
	if (hibType == null) {
	    if (other.hibType != null) {
		return false;
	    }
	} else if (!hibType.equals(other.hibType)) {
	    return false;
	}
	if (javaType == null) {
	    if (other.javaType != null) {
		return false;
	    }
	} else if (!javaType.equals(other.javaType)) {
	    return false;
	}
	if (name == null) {
	    if (other.name != null) {
		return false;
	    }
	} else if (!name.equals(other.name)) {
	    return false;
	}
	if (nullable != other.nullable) {
	    return false;
	}
	if (type != other.type) {
	    return false;
	}
	if (virtual != other.virtual) {
	    return false;
	}
	return true;
    }

}