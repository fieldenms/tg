package ua.com.fielden.platform.eql.s1.elements;

import java.util.Collections;
import java.util.List;



abstract class ZeroOperandFunction extends AbstractFunction {

    private final String functionName;

    public ZeroOperandFunction(final String functionName) {
	this.functionName = functionName;
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
	result = prime * result + ((functionName == null) ? 0 : functionName.hashCode());
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
	if (!(obj instanceof ZeroOperandFunction)) {
	    return false;
	}
	final ZeroOperandFunction other = (ZeroOperandFunction) obj;
	if (functionName == null) {
	    if (other.functionName != null) {
		return false;
	    }
	} else if (!functionName.equals(other.functionName)) {
	    return false;
	}
	return true;
    }
}