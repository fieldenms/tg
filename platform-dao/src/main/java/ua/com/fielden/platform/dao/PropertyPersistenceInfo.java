package ua.com.fielden.platform.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.type.Type;

import ua.com.fielden.platform.entity.query.ICompositeUserTypeInstantiate;
import ua.com.fielden.platform.entity.query.IUserTypeInstantiate;
import ua.com.fielden.platform.utils.EntityUtils;

public class PropertyPersistenceInfo implements Comparable<PropertyPersistenceInfo> {
    // TODO: when collectional - no information about hib types is provided

    private static final Map<String, Integer> sortOrder = new HashMap<String, Integer>();

    static {
	sortOrder.put("id", 1);
	sortOrder.put("version", 2);
	sortOrder.put("key", 3);
    }

    private final String name;
    private final Class javaType;
    private final Object hibType;
    private final List<String> columns;
    private final PropertyPersistenceType type;
    private final Long length;

    public Type getHibTypeAsType() {
	return hibType instanceof Type ? (Type) hibType : null;
    }

    public IUserTypeInstantiate getHibTypeAsUserType() {
	return hibType instanceof IUserTypeInstantiate ? (IUserTypeInstantiate) hibType : null;
    }

    @Override
    public String toString() {
        return "name = " + name + "\njavaType = " + (javaType != null ? javaType.getSimpleName() : javaType) + "\nhibType = " + (hibType != null ? hibType.getClass().getSimpleName() : hibType) + "\ntype = " + type + "\ncolumn(s) = " + columns;
    }

    public boolean isEntity() {
	return EntityUtils.isPersistedEntityType(javaType);
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
	if (sortOrder.containsKey(name)) {
	    if (sortOrder.containsKey(o.name)) {
		return sortOrder.get(name).compareTo(sortOrder.get(o.name));
	    } else {
		return -1;
	    }
	} else if (sortOrder.containsKey(o.name)) {
	    return 1;
	}

	return name.compareTo(o.name);
    }

    public List<PropertyPersistenceInfo> getCompositeTypeSubprops() {
	final List<PropertyPersistenceInfo> result = new ArrayList<PropertyPersistenceInfo>();
	if (hibType instanceof ICompositeUserTypeInstantiate) {
	    final List<String> subprops = Arrays.asList(((ICompositeUserTypeInstantiate) hibType).getPropertyNames());
	    final List<Object> subpropsTypes = Arrays.asList(((ICompositeUserTypeInstantiate) hibType).getPropertyTypes());
	    int index = 0;
	    for (final String subpropName : subprops) {
		final String column = columns.get(index);
		final Object hibType = subpropsTypes.get(index);
		result.add(new PropertyPersistenceInfo.Builder(name + "." +subpropName, ((Type) hibType).getReturnedClass()).column(column).type(PropertyPersistenceType.COMPOSITE_DETAILS).hibType(hibType).build());
		index = index + 1;
	    }
	}
	return result;
    }

    private PropertyPersistenceInfo(final Builder builder) {
	type = builder.type;
	length = builder.length > 0 ? new Long(builder.length) : null;
	name = builder.name;
	javaType = builder.javaType;
	hibType = builder.hibType;
	columns = builder.columns;
    }

    public Long getLength() {
	return length;
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

    public List<String> getColumns() {
        return columns;
    }

    public String getColumn() {
        return columns.get(0);
    }

    public static class Builder {
	private final String name;
	private final Class javaType;

	private Object hibType;
	private List<String> columns = new ArrayList<String>();
	private PropertyPersistenceType type = PropertyPersistenceType.PROP;
	private long length = 0;

	public PropertyPersistenceInfo build() {
	    return new PropertyPersistenceInfo(this);

	}

	public Builder(final String name, final Class javaType) {
	    this.name = name;
	    this.javaType = javaType;
	}

	public Builder length(final long val) {
	    length = val;
	    return this;
	}

	public Builder hibType(final Object val) {
	    hibType = val;
	    return this;
	}

	public Builder type(final PropertyPersistenceType val) {
	    type = val;
	    return this;
	}

	public Builder column(final String column) {
	    columns.add(column);
	    return this;
	}
    }
}