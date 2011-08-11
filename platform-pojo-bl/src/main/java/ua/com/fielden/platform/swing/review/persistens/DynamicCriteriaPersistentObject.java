package ua.com.fielden.platform.swing.review.persistens;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.swing.review.PropertyPersistentObject;

/**
 * Instances of this class should encapsulate all criteria-related data that needs to be persisted.
 * 
 * @author yura, Oleh
 * 
 */
public class DynamicCriteriaPersistentObject {

    /**
     * Holds current version of dynamic criteria persistence logic. Increment this number each time you believe persistence logic has changed and old report files should be
     * discarded.
     */
    public static final int realVersion = 1;

    private final int version;

    private final int columnsCount;

    private final boolean provideSuggestions;

    private final List<String> tableHeaders = new ArrayList<String>();

    private final List<String> properties = new ArrayList<String>();

    private final List<String> excludeProperties = new ArrayList<String>();

    private final Map<String, PropertyPersistentObject> criteriaMappings = new HashMap<String, PropertyPersistentObject>();

    public DynamicCriteriaPersistentObject(final List<String> tableHeaders, final List<String> properties, final List<String> excludeProperties, final Map<String, PropertyPersistentObject> criteriaMappings, final int columnsCount, final boolean provideSuggestions) {
	this.tableHeaders.addAll(tableHeaders != null ? tableHeaders : new ArrayList<String>());
	this.properties.addAll(properties != null ? properties : new ArrayList<String>());
	this.excludeProperties.addAll(excludeProperties != null ? excludeProperties : new ArrayList<String>());

	this.columnsCount = columnsCount;
	this.provideSuggestions = provideSuggestions;
	this.criteriaMappings.putAll(criteriaMappings != null ? criteriaMappings : new HashMap<String, PropertyPersistentObject>());
	this.version = realVersion;
    }

    public List<String> getTableHeaders() {
	return Collections.unmodifiableList(tableHeaders);
    }

    public Map<String, PropertyPersistentObject> getCriteriaMappings() {
	return Collections.unmodifiableMap(criteriaMappings);
    }

    public List<String> getPersistentProperties() {
	return Collections.unmodifiableList(properties);
    }

    public List<String> getExcludeProperties() {
	return Collections.unmodifiableList(excludeProperties != null ? excludeProperties : new ArrayList<String>());
    }

    public int getColumnsCount() {
	return columnsCount;
    }

    public boolean isProvideSuggestions() {
	return provideSuggestions;
    }

    public int getVersion() {
	return version;
    }

    /**
     * Returns true if this object has valid version. If not, this object cannot be used.
     * 
     * @return
     */
    public boolean hasValidVersion() {
	return version == realVersion;
    }

    public static class InvalidReportsFileVersionException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	private final String fileName;

	public InvalidReportsFileVersionException(final String fileName) {
	    this.fileName = fileName;
	}

	public String getFileName() {
	    return fileName;
	}
    }

    /**
     * Determines whether this {@link DynamicCriteriaPersistentObject} is different from the given one.
     * 
     * @param pObj
     * @return
     */
    public boolean isChanged(final Object obj) {
	if (this == obj) {
	    return false;
	}
	if (!(obj instanceof DynamicCriteriaPersistentObject)) {
	    return true;
	}
	final DynamicCriteriaPersistentObject pObj = (DynamicCriteriaPersistentObject) obj;
	if (getVersion() != pObj.getVersion()) {
	    return true;
	}
	if (getColumnsCount() != pObj.getColumnsCount()) {
	    return true;
	}
	if (isProvideSuggestions() != pObj.isProvideSuggestions()) {
	    return true;
	}
	if (!getTableHeaders().equals(pObj.getTableHeaders())) {
	    return true;
	}
	if (!getExcludeProperties().equals(pObj.getExcludeProperties())) {
	    return true;
	}
	if (!getPersistentProperties().equals(pObj.getPersistentProperties())) {
	    return true;
	}
	if (!getCriteriaMappings().equals(pObj.getCriteriaMappings())) {
	    return true;
	}
	return false;
    }

    public void updatePersistentObjectFrom(final DynamicCriteriaPersistentObject obj) {

    }

}
