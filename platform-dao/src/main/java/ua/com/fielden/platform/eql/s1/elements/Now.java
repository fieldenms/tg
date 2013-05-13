package ua.com.fielden.platform.eql.s1.elements;

import java.util.Collections;
import java.util.List;

import ua.com.fielden.platform.entity.query.generation.DbVersion;



public class Now implements ISingleOperand {
    private final DbVersion dbVersion;

    public Now(final DbVersion dbVersion) {
	this.dbVersion = dbVersion;
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
	return Collections.emptyList();
    }

    @Override
    public boolean ignore() {
	return false;
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((dbVersion == null) ? 0 : dbVersion.hashCode());
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
	if (!(obj instanceof Now)) {
	    return false;
	}
	final Now other = (Now) obj;
	if (dbVersion != other.dbVersion) {
	    return false;
	}
	return true;
    }

}