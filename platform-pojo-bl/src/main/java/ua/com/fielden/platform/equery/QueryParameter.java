package ua.com.fielden.platform.equery;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.types.Money;

public final class QueryParameter {
    private String paramName;
    private Object paramValue;

    /**
     * Mainly used for serialisation.
     */
    protected QueryParameter() {
    }

    public QueryParameter(final String paramName, final Object... paramValues) {
	this.paramName = paramName;

	if (paramValues == null || paramValues.length == 0) {
	    this.paramValue = null;
	} else if (paramValues.length == 1) {
	    this.paramValue = convertParamValue(paramValues[0]);
	} else if (paramValues.length > 1) {
	    final List<Object> values = new ArrayList<Object>();
	    for (final Object object : paramValues) {
		values.add(convertParamValue(object));
	    }
	    this.paramValue = values;
	}
    }

    /** Ensures that values of special types such as {@link Class} or {@link PropertyDescriptor} are converted to String. */
    private Object convertParamValue(final Object paramValue) {
	if (paramValue instanceof PropertyDescriptor || paramValue instanceof Class) {
	    return paramValue.toString();
	} else if (paramValue instanceof AbstractEntity) {
	    return ((AbstractEntity) paramValue).getId();
	} else if (paramValue instanceof Money) {
	    return ((Money) paramValue).getAmount();
	} else {
	    return paramValue;
	}
    }

    public QueryParameter clon() {
	return new QueryParameter(getParamName(), paramValue);
    }

    public String getParamName() {
	return paramName;
    }

    public void setParamName(final String paramName) {
	this.paramName = paramName;
    }

    public Object getParamValue() {
	return paramValue;
    }

    public void setParamValue(final Object paramValue) {
	this.paramValue = paramValue;
    }

    public String getValue() {
	return ":" + paramName;
    }

    @Override
    public boolean equals(final Object obj) {
	if (this == obj) {
	    return true;
	}
	if (!(obj instanceof QueryParameter)) {
	    return false;
	}

	final QueryParameter cmp = (QueryParameter) obj;
	return getParamName().equals(cmp.getParamName());
    }

    @Override
    public String toString() {
	return paramValue != null ? paramValue.toString() : ":" + paramName;
    }

}
