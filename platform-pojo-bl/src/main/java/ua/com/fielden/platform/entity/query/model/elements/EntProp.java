package ua.com.fielden.platform.entity.query.model.elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EntProp implements ISingleOperand {
    private final String name;
    private Class propType; //TODO ?

    @Override
    public String toString() {
        return name + " type: " + propType;
    }

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
    public List<EntProp> getProps() {
	final List<EntProp> result = new ArrayList<EntProp>();
	result.add(this);
	return result;
    }

    @Override
    public List<EntQuery> getSubqueries() {
	return Collections.emptyList();
    }

    public Class getPropType() {
        return propType;
    }

    public void setPropType(final Class propType) {
        this.propType = propType;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean ignore() {
	return false;
    }

    @Override
    public Class type() {
	return propType;
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