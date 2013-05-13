package ua.com.fielden.platform.eql.s1.elements;

import java.util.Collections;
import java.util.List;


public class CountAll implements ISingleOperand {

    private final String sql = "COUNT(*)";

    public CountAll() {
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
	result = prime * result + ((sql == null) ? 0 : sql.hashCode());
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
	if (!(obj instanceof CountAll)) {
	    return false;
	}
	final CountAll other = (CountAll) obj;
	if (sql == null) {
	    if (other.sql != null) {
		return false;
	    }
	} else if (!sql.equals(other.sql)) {
	    return false;
	}
	return true;
    }
}