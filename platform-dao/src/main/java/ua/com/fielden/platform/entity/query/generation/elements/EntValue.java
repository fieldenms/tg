package ua.com.fielden.platform.entity.query.generation.elements;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class EntValue implements ISingleOperand {
    private final Object value;
    private final boolean ignoreNull;
    private String paramName;

    @Override
    public String sql() {
	return paramName != null ? ":" + paramName : value.toString();
    }

    public EntValue(final Object value) {
	this(value, false);
    }

    public EntValue(final Object value, final boolean ignoreNull) {
	super();
	this.value = value;
	this.ignoreNull = ignoreNull;
    }

    @Override
    public List<EntProp> getLocalProps() {
	return Collections.emptyList();
    }

    @Override
    public List<EntQuery> getLocalSubQueries() {
	return Collections.emptyList();
    }

    @Override
    public List<EntValue> getAllValues() {
	return Arrays.asList(new EntValue[]{this});
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((value == null) ? 0 : value.hashCode());
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
	if (!(obj instanceof EntValue)) {
	    return false;
	}
	final EntValue other = (EntValue) obj;
	if (value == null) {
	    if (other.value != null) {
		return false;
	    }
	} else if (!value.equals(other.value)) {
	    return false;
	}
	return true;
    }

    @Override
    public boolean ignore() {
	return ignoreNull && value == null;
    }

    @Override
    public Class type() {
	return value != null ? value.getClass() : null;
    }

    public String getParamName() {
        return paramName;
    }

    public void setParamName(final String paramName) {
        this.paramName = paramName;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public Object hibType() {
	return null;
    }

    @Override
    public boolean isNullable() {
	return true;
    }
}