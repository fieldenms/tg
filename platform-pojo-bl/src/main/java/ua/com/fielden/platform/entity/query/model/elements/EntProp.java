package ua.com.fielden.platform.entity.query.model.elements;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EntProp implements ISingleOperand {
    private final String name;

    public EntProp(final String name) {
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
    public List<EntQuery> getSubqueries() {
	return Collections.emptyList();
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
	if (!(obj instanceof EntProp)) {
	    return false;
	}
	final EntProp other = (EntProp) obj;
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