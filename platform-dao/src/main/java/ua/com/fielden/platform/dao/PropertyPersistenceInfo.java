package ua.com.fielden.platform.dao;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.type.Type;

import ua.com.fielden.platform.entity.query.ICompositeUserTypeInstantiate;
import ua.com.fielden.platform.entity.query.IUserTypeInstantiate;
import ua.com.fielden.platform.utils.EntityUtils;

public class PropertyPersistenceInfo {
    // TODO: when collectional - no information about hib types is provided

    private final String name;
    private final Class javaType;
    private final Type hibType;
    private final IUserTypeInstantiate hibUserType;
    private final ICompositeUserTypeInstantiate hibCompositeUserType;
    private final List<String> columns;
    private final boolean collection;
    private final Long length;

    public boolean isEntity() {
	return EntityUtils.isPersistedEntityType(javaType);
    }

    public boolean isCollection() {
	return collection;
    }

    public String getTypeString() {
	if (hibType != null) {
	    return hibType.getClass().getName();
	}
	if (hibUserType != null) {
	    return hibUserType.getClass().getName();
	}
	if (hibCompositeUserType != null) {
	    return hibCompositeUserType.getClass().getName();
	}

	return null;
    }

    private PropertyPersistenceInfo(final Builder builder) {
	collection = builder.collection;
	length = builder.length > 0 ? new Long(builder.length) : null;
	name = builder.name;
	javaType = builder.javaType;
	hibType = builder.hibType;
	hibUserType = builder.hibUserType;
	hibCompositeUserType = builder.hibCompositeUserType;
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

    public Type getHibType() {
        return hibType;
    }

    public IUserTypeInstantiate getHibUserType() {
        return hibUserType;
    }

    public ICompositeUserTypeInstantiate getHibCompositeUserType() {
        return hibCompositeUserType;
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

	private Type hibType;
	private IUserTypeInstantiate hibUserType;
	private ICompositeUserTypeInstantiate hibCompositeUserType;
	private List<String> columns = new ArrayList<String>();
	private boolean collection;
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

	public Builder hibType(final Type val) {
	    hibType = val;
	    return this;
	}

	public Builder hibUserType(final IUserTypeInstantiate val) {
	    hibUserType = val;
	    return this;
	}

	public Builder hibCompositeUserType(final ICompositeUserTypeInstantiate val) {
	    hibCompositeUserType = val;
	    return this;
	}

	public Builder collectional(final boolean val) {
	    collection = val;
	    return this;
	}

	public Builder column(final String column) {
	    columns.add(column);
	    return this;
	}
    }
}