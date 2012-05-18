package ua.com.fielden.platform.persistence.types;

import java.io.Serializable;
import java.util.Map;

import org.hibernate.engine.SessionImplementor;
import org.hibernate.usertype.CompositeUserType;

public abstract class AbstractCompositeUserType implements CompositeUserType {

    @Override
    public boolean isMutable() {
	return false;
    }

    @Override
    public Object deepCopy(final Object value) {
	return value;
    }

    @Override
    public Serializable disassemble(final Object value, final SessionImplementor session) {
	return (Serializable) value;
    }

    @Override
    public Object assemble(final Serializable cached, final SessionImplementor session, final Object owner) {
	return cached;
    }

    @Override
    public Object replace(final Object original, final Object target, final SessionImplementor session, final Object owner) {
	return original;
    }

    @Override
    public boolean equals(final Object x, final Object y) {
	if (x == y) {
	    return true;
	}
	if (x == null || y == null) {
	    return false;
	}
	return x.equals(y);
    }

    @Override
    public int hashCode(final Object x) {
	return x.hashCode();
    }

    @Override
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