package ua.com.fielden.uds.designer.zui.component.generic.value;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import ua.com.fielden.uds.designer.zui.interfaces.IUpdater;
import ua.com.fielden.uds.designer.zui.interfaces.IValue;

/**
 * This class models a string value as an instance of IValue.
 * 
 * @author 01es
 * 
 */
public class StringValue implements IValue<String>, Serializable, Cloneable {
    private static final long serialVersionUID = -77144797559485527L;
    private String value;
    private Set<IUpdater<String>> updaters = new HashSet<IUpdater<String>>();

    private boolean emptyPermitted = true; // permits or denies an empty string as a vlue
    private String defaultValue = "cannot be empty"; // this is a default value if a value cannot be empty

    public StringValue() {
    }

    public StringValue(String value) {
	setValue(value);
    }

    public String getValue() {
	return value;
    }

    public void setValue(String value) {
	// handle empty value if necessary
	this.value = value;
	if ("".equals(value) && !isEmptyPermitted()) {
	    this.value = getDefaultValue();
	} else {
	    setDefaultValue(this.value);
	}
	for (IUpdater<String> updater : updaters) {
	    updater.update(this.value);
	}
    }

    public void registerUpdater(IUpdater<String> updater) {
	if (updater != null) {
	    // System.out.println("updater is registered");
	    updaters.add(updater);
	}
    }

    public void removeUpdater(IUpdater<String> updater) {
	if (updaters.remove(updater)) {
	    // System.out.println("updater is removed");
	}
    }

    @Override
    public boolean equals(Object obj) {
	if (!(obj instanceof IValue)) {
	    return false;
	}
	if (this == obj) {
	    return true;
	}

	return getValue().equals(((IValue) obj).getValue());
    }

    public Object clone() {
	return new StringValue(getValue());
    }

    public String getDefaultValue() {
	return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
	this.defaultValue = defaultValue;
    }

    public boolean isEmptyPermitted() {
	return emptyPermitted;
    }

    public void setEmptyPermitted(boolean emptyPermitted) {
	this.emptyPermitted = emptyPermitted;
    }
}
