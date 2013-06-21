package ua.com.fielden.platform.eql.s1.elements;

import ua.com.fielden.platform.eql.meta.TransformatorToS2;
import ua.com.fielden.platform.eql.s2.elements.EntValue2;

public class EntParam1 implements ISingleOperand1<EntValue2> {
    private String name;
    private boolean ignoreNull;

    public EntParam1(final String name, final boolean ignoreNull) {
        this.name = name;
        this.ignoreNull = ignoreNull;
    }

    public EntParam1(final String name) {
        this(name, false);
    }

    @Override
    public String toString() {
        return name;
    }

    public EntValue2 transform(final TransformatorToS2 resolver) {
	return resolver.getTransformedParamToValue(this);
    }

    public String getName() {
        return name;
    }

    public boolean isIgnoreNull() {
        return ignoreNull;
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + (ignoreNull ? 1231 : 1237);
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
	if (!(obj instanceof EntParam1)) {
	    return false;
	}
	final EntParam1 other = (EntParam1) obj;
	if (ignoreNull != other.ignoreNull) {
	    return false;
	}
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