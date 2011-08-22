package ua.com.fielden.platform.entity.query.model.elements;

import java.util.HashSet;
import java.util.Set;


public class EntValue implements ISingleOperand {
    private final Object value;

    public EntValue(final Object value) {
	super();
	this.value = value;
    }

    @Override
    public Set<String> getPropNames() {
	return new HashSet<String>();
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
}
