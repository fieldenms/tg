package ua.com.fielden.platform.entity.query.model.elements;

import java.util.HashSet;
import java.util.Set;

import ua.com.fielden.platform.entity.query.model.elements.ISingleOperand;

public class SqlEntProp implements ISingleOperand {
    private final String name;

    public SqlEntProp(final String name) {
	super();
	this.name = name;
    }

    @Override
    public Set<String> getPropNames() {
	final Set<String> result = new HashSet<String>();
	result.add(name);
	return result;
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((name == null) ? 0 : name.hashCode());
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
	if (!(obj instanceof SqlEntProp)) {
	    return false;
	}
	final SqlEntProp other = (SqlEntProp) obj;
	if (name == null) {
	    if (other.name != null) {
		return false;
	    }
	} else if (!name.equals(other.name)) {
	    return false;
	}
	return true;
    }
}
