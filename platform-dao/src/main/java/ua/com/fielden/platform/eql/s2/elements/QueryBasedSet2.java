package ua.com.fielden.platform.eql.s2.elements;

import java.util.List;


public class QueryBasedSet2 implements ISetOperand2 {
    private final EntQuery2 model;

    public QueryBasedSet2(final EntQuery2 model) {
	super();
	this.model = model;
    }

    @Override
    public List<EntValue2> getAllValues() {
	return model.getAllValues();
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
	if (!(obj instanceof QueryBasedSet2)) {
	    return false;
	}
	final QueryBasedSet2 other = (QueryBasedSet2) obj;
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