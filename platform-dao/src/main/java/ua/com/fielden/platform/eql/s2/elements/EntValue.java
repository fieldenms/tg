package ua.com.fielden.platform.eql.s2.elements;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class EntValue implements ISingleOperand2 {
    private final Object value;
    private final boolean ignoreNull;
    private final String sqlParamName;

    public EntValue(final Object value, final String sqlParamName) {
	this(value, false, sqlParamName);
    }

    public EntValue(final Object value, final boolean ignoreNull, final String sqlParamName) {
	super();
	this.value = value;
	this.ignoreNull = ignoreNull;
	this.sqlParamName = sqlParamName;
	if (!ignoreNull && value == null) {
	    // TODO Uncomment when yieldNull() operator is implemented and all occurences of yield().val(null) are corrected.
//	    throw new IllegalStateException("Value can't be null"); //
	}
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
	return Arrays.asList(new EntValue[]{this});
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((value == null) ? 0 : value.hashCode());
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
	if (!(obj instanceof EntValue)) {
	    return false;
	}
	final EntValue other = (EntValue) obj;
	if (value == null) {
	    if (other.value != null) {
		return false;
	    }
	} else if (!value.equals(other.value)) {
	    return false;
	}
	return true;
    }

    @Override
    public boolean ignore() {
	return ignoreNull && value == null;
    }

    public Object getValue() {
        return value;
    }
}