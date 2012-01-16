package ua.com.fielden.platform.entity.query.model.elements;

import java.util.Collections;
import java.util.List;
import java.util.Set;


public class EntValue implements ISingleOperand {
    private final Object value;
    private final boolean ignoreNull;

    public EntValue(final Object value) {
	super();
	this.value = value;
	this.ignoreNull = false;
    }

    public EntValue(final Object value, final boolean ignoreNull) {
	super();
	this.value = value;
	this.ignoreNull = ignoreNull;
    }

    @Override
    public Set<String> getPropNames() {
	return Collections.emptySet();
    }

    @Override
    public List<EntProp> getProps() {
	return Collections.emptyList();
    }

    @Override
    public List<EntQuery> getSubqueries() {
	return Collections.emptyList();
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
}