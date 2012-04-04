package ua.com.fielden.platform.persistence.types;

import java.io.Serializable;
import java.util.Map;

import org.hibernate.engine.SessionImplementor;
import org.hibernate.usertype.CompositeUserType;

public abstract class AbstractCompositeUserType implements CompositeUserType {

    public boolean isMutable() {
	return false;
    }

    public Object deepCopy(final Object value) {
	return value;
    }

    public Serializable disassemble(final Object value, final SessionImplementor session) {
	return (Serializable) value;
    }

    public Object assemble(final Serializable cached, final SessionImplementor session, final Object owner) {
	return cached;
    }

    public Object replace(final Object original, final Object target, final SessionImplementor session, final Object owner) {
	return original;
    }

    public boolean equals(final Object x, final Object y) {
	if (x == y) {
	    return true;
	}
	if (x == null || y == null) {
	    return false;
	}
	return x.equals(y);
    }

    public int hashCode(final Object x) {
	return x.hashCode();
    }

    public void setPropertyValue(final Object component, final int property, final Object value) {
	throw new UnsupportedOperationException("This type is immutable");
    }

    protected boolean allArgumentsAreNull(final Map<String, Object> arguments) {
	for (final Object argumentValue : arguments.values()) {
	    if (argumentValue != null) {
		return false;
	    }
	}
	return true;
    }
}