package ua.com.fielden.platform.eql.s2.elements;

import java.util.Arrays;
import java.util.List;


public class QueryBasedSet implements ISetOperand2 {
    private final EntQuery model;

    public QueryBasedSet(final EntQuery model) {
	super();
	this.model = model;
    }

    @Override
    public List<EntProp> getLocalProps() {
	return model.getLocalProps();
    }

    @Override
    public List<EntValue> getAllValues() {
	return model.getAllValues();
    }

    @Override
    public List<EntQuery> getLocalSubQueries() {
	return Arrays.asList(new EntQuery[]{model});
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((model == null) ? 0 : model.hashCode());
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
	if (!(obj instanceof QueryBasedSet)) {
	    return false;
	}
	final QueryBasedSet other = (QueryBasedSet) obj;
	if (model == null) {
	    if (other.model != null) {
		return false;
	    }
	} else if (!model.equals(other.model)) {
	    return false;
	}
	return true;
    }

    @Override
    public boolean ignore() {
	// TODO Auto-generated method stub
	return false;
    }
}