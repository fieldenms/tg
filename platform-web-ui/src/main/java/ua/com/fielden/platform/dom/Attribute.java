package ua.com.fielden.platform.dom;

import org.apache.commons.lang.StringUtils;

public class Attribute<T> {

    public final String name;
    public final T value;

    protected final String nameValueSeparator;

    public Attribute(final String name, final T value, final String nameValueSeparator) {
	if (name == null || name.isEmpty()) {
	    throw new IllegalArgumentException("The attribute name can not be null or emptty.");
	}
	if (nameValueSeparator == null || nameValueSeparator.isEmpty()) {
	    throw new IllegalArgumentException("The name-value attribute separotr can not be null");
	}
	this.name = name;
	this.value = value;
	this.nameValueSeparator = nameValueSeparator;
    }

    @Override
    public String toString() {
        return (value == null || StringUtils.isEmpty(value.toString())) ? name : (name + nameValueSeparator + "\"" + value + "\"");
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((name == null) ? 0 : name.hashCode());
	result = prime * result + ((value == null) ? 0 : value.hashCode());
	result = prime * result + ((nameValueSeparator == null) ? 0 : nameValueSeparator.hashCode());
	return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(final Object obj) {
	if (this == obj) {
	    return true;
	}
	if (obj == null) {
	    return false;
	}
	if (getClass() != obj.getClass()) {
	    return false;
	}
	final Attribute<T> other = (Attribute<T>) obj;
	if (name == null && other.name != null || name != null && !name.equals(other.name)) {
	    return false;
	}
	if (value == null && other.value != null || value != null && !value.equals(other.value)) {
		return false;
	}
	if (nameValueSeparator == null && other.nameValueSeparator != null ||
		nameValueSeparator != null && !nameValueSeparator.equals(other.nameValueSeparator)) {
		return false;
	}
	return true;
    }


}
