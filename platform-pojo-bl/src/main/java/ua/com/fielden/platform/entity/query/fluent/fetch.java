package ua.com.fielden.platform.entity.query.fluent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ua.com.fielden.platform.entity.AbstractEntity;

public class fetch<T extends AbstractEntity<?>> {
    private final Class<T> entityType;
    private final Map<String, fetch<? extends AbstractEntity<?>>> includedPropsWithModels = new HashMap<String, fetch<? extends AbstractEntity<?>>>();
    private final Set<String> incudedProps = new HashSet<String>();
    private final Set<String> excludedProps = new HashSet<String>();
    private final boolean allIncluded;

    /**
     * Used mainly for serialisation.
     */
    protected fetch() {
	this.entityType = null;
	this.allIncluded = false;
    }

    public fetch(final Class<T> entityType) {
	this(entityType, false);
    }

    protected fetch(final Class<T> entityType, final boolean allIncluded) {
	this.entityType = entityType;
	this.allIncluded = allIncluded;
    }

    public fetch<T> with(final String propName) {
	incudedProps.add(propName);
	return this;
    }

    public fetch<T> without(final String propName) {
	excludedProps.add(propName);
	return this;
    }

    public fetch<T> with(final String propName, final fetch<? extends AbstractEntity<?>> fetchModel) {
	includedPropsWithModels.put(propName, fetchModel);
	return this;
    }

    public Class<T> getEntityType() {
	return entityType;
    }

    @Override
    public String toString() {
	return getString("     ");
    }

    private String getString(final String offset) {
	final StringBuffer sb = new StringBuffer();
	sb.append("\n");
	for (final Map.Entry<String, fetch<?>> fetchModel : includedPropsWithModels.entrySet()) {
	    sb.append(offset + fetchModel.getKey() + fetchModel.getValue().getString(offset + "   "));
	}

	return sb.toString();
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((entityType == null) ? 0 : entityType.hashCode());
	result = prime * result + ((includedPropsWithModels == null) ? 0 : includedPropsWithModels.hashCode());
	return result;
    }

    @Override
    public boolean equals(final Object obj) {
	if (this == obj) {
	    return true;
	}
	if (!(obj instanceof fetch)) {
	    return false;
	}

	final fetch that = (fetch) obj;
	if (entityType == null) {
	    if (that.entityType != null) {
		return false;
	    }
	} else if (!entityType.equals(that.entityType)) {
	    return false;
	}
	if (includedPropsWithModels == null) {
	    if (that.includedPropsWithModels != null) {
		return false;
	    }
	} else if (!includedPropsWithModels.equals(that.includedPropsWithModels)) {
	    return false;
	}
	return true;
    }

    public boolean isAllIncluded() {
        return allIncluded;
    }

    public Map<String, fetch<? extends AbstractEntity<?>>> getIncludedPropsWithModels() {
        return includedPropsWithModels;
    }

    public Set<String> getIncudedProps() {
        return incudedProps;
    }

    public Set<String> getExcludedProps() {
        return excludedProps;
    }
}