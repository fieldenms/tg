package ua.com.fielden.platform.eql.s1.elements;

import ua.com.fielden.platform.eql.s2.elements.IElement2;



abstract class ZeroOperandFunction1<S2 extends IElement2> extends AbstractFunction1<S2> {

    private final String functionName;

    public ZeroOperandFunction1(final String functionName) {
	this.functionName = functionName;
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
	if (!(obj instanceof ZeroOperandFunction1)) {
	    return false;
	}
	final ZeroOperandFunction1 other = (ZeroOperandFunction1) obj;
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