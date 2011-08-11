package ua.com.fielden.platform.swing.review;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.swing.locator.ILocatorConfigurationRetriever;
import ua.com.fielden.platform.ui.config.api.interaction.IConfigurationManager;

/**
 * Persistent object for locators configuration. Locators configuration are mapped to the property name.
 * 
 * @author TG Team
 * 
 */
public class LocatorPersistentObject implements ILocatorConfigurationRetriever {

    private final Map<String, DynamicCriteriaPersistentObjectUi> locators = new HashMap<String, DynamicCriteriaPersistentObjectUi>();

    private transient IConfigurationManager baseConfigurationManager;

    private transient String baseConfiguratonKey;

    /**
     * Default constructor. Clears all previous locator configurations.
     */
    public LocatorPersistentObject() {
	locators.clear();
	baseConfigurationManager = null;
	baseConfiguratonKey = null;
    }

    /**
     * Copy constructor. Copies all locator configurations form given {@link LocatorPersistentObject} instance to this one.
     * 
     * @param persistentObject
     */
    public LocatorPersistentObject(final LocatorPersistentObject persistentObject) {
	this();
	if (persistentObject != null) {
	    locators.putAll(persistentObject.locators);
	    baseConfigurationManager = persistentObject.baseConfigurationManager;
	    baseConfiguratonKey = persistentObject.baseConfiguratonKey;
	}
    }

    public void setBaseConfigurationManager(final String baseKey, final IConfigurationManager baseManager) {
	this.baseConfiguratonKey = baseKey;
	this.baseConfigurationManager = baseManager;
    }

    /**
     * Returns the locator configuration for the specified property name.
     * 
     * @param propertyName
     *            - property name for which locator configuration will be returned.
     * @return
     */
    @Override
    public DynamicCriteriaPersistentObjectUi getLocatorConfiguration(final String propertyName) {
	return locators.get(propertyName);
    }

    /**
     * Set the locator configuration for specified property name.
     * 
     * @param propertyName
     *            - the property name for which locator configuration should be set.
     * @param locatorConfiguration
     *            - locator configuration.
     */
    @Override
    public void setLocatorConfiguration(final String propertyName, final DynamicCriteriaPersistentObjectUi locatorConfiguration) {
	locators.put(propertyName, locatorConfiguration);
    }

    /**
     * Determines whether this {@link LocatorPersistentObject} instance is different from the given one.
     * 
     * @param locatorPersistentObject
     * @return
     */
    public boolean isChanged(final LocatorPersistentObject locatorPersistentObject) {
	if (this == locatorPersistentObject) {
	    return false;
	}
	if (!isLocatorConfigurationEqual(locatorPersistentObject.locators)) {
	    return true;
	}
	return false;
    }

    //Determines whether specified map of property names and DynamicCriteriaPersistentObject is equal to the locators map.
    private boolean isLocatorConfigurationEqual(final Map<String, DynamicCriteriaPersistentObjectUi> anotherLocators) {
	if (anotherLocators == locators) {
	    return true;
	}

	if (anotherLocators.size() != locators.size()) {
	    return false;
	}

	try {
	    final Iterator<Entry<String, DynamicCriteriaPersistentObjectUi>> i = locators.entrySet().iterator();
	    while (i.hasNext()) {
		final Entry<String, DynamicCriteriaPersistentObjectUi> e = i.next();
		final String key = e.getKey();
		final DynamicCriteriaPersistentObjectUi value = e.getValue();
		if (value == null) {
		    if (!(anotherLocators.get(key) == null && anotherLocators.containsKey(key))) {
			return false;
		    }
		} else {
		    if (value.isChanged(anotherLocators.get(key))) {
			return false;
		    }
		}
	    }
	} catch (final ClassCastException unused) {
	    return false;
	} catch (final NullPointerException unused) {
	    return false;
	}

	return true;
    }

    @Override
    public Result canSave(final String locatorKey) {
	if (baseConfigurationManager == null || baseConfiguratonKey == null) {
	    return new Result(new IllegalArgumentException("The base configurations can not be null"));
	}
	return baseConfigurationManager.canSave(baseConfiguratonKey);
    }

    @Override
    public Result canConfigure(final String locatorKey) {
	if (baseConfigurationManager == null || baseConfiguratonKey == null) {
	    return new Result(new IllegalArgumentException("The base configurations can not be null"));
	}
	return baseConfigurationManager.canConfigure(baseConfiguratonKey);
    }

}
