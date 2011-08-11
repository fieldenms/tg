package ua.com.fielden.platform.swing.components.smart.autocompleter;

import ua.com.fielden.platform.entity.IBindingEntity;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.swing.dialogs.DialogWithDetails;
import ua.com.fielden.platform.swing.locator.ILocatorConfigurationRetriever;
import ua.com.fielden.platform.swing.review.DynamicCriteriaPersistentObjectUi;
import ua.com.fielden.platform.ui.config.api.interaction.IConfigurationController;
import ua.com.fielden.platform.ui.config.api.interaction.ILocatorConfigurationController;

/**
 * This {@link IConfigurationController} handles global and local configuration management.
 * 
 * @author oleh
 * 
 */
public class LocatorManager implements ILocatorConfigurationController {

    private final ILocatorConfigurationController locatorController;
    private final ILocatorConfigurationRetriever locatorRetriever;
    private final String localKey, defaultKey;
    private final Class<?> resultantEntityClass;

    public LocatorManager(final ILocatorConfigurationController locatorController, final ILocatorConfigurationRetriever locatorRetriever, final Class<?> resultantEntityClass, final Class<? extends IBindingEntity> entityType, final String propertyName) {
	this.locatorController = locatorController;
	this.locatorRetriever = locatorRetriever;
	this.resultantEntityClass = resultantEntityClass;
	this.localKey = locatorController.generateKeyForAutocompleterConfiguration(entityType, propertyName);
	this.defaultKey = locatorController.generateKeyForDefaultAutocompleterConfiguration(resultantEntityClass);
    }

    @Override
    public void save(final String key, final byte[] objectToSave) {
	if (key.equals(localKey)) {
	    DynamicCriteriaPersistentObjectUi locatorConfiguration = null;
	    try {
		locatorConfiguration = getSerialiser().deserialise(objectToSave, DynamicCriteriaPersistentObjectUi.class);
		locatorRetriever.setLocatorConfiguration(key, locatorConfiguration);
	    } catch (final Exception e) {
		new DialogWithDetails(null, "Exception while saving", e).setVisible(true);
	    }
	} else if (key.equals(defaultKey)) {
	    locatorController.save(key, objectToSave);
	} else {
	    throw new UnsupportedOperationException("It's imposible to save any other locator configuration except local or default. Key for local = " + localKey
		    + ", and key for default = " + defaultKey);
	}
    }

    @Override
    public byte[] load(final String key) {
	if (key.equals(localKey)) {
	    return getSerialiser().serialise(locatorRetriever.getLocatorConfiguration(key));
	} else if (key.equals(defaultKey)) {
	    return locatorController.load(key);
	} else {
	    throw new UnsupportedOperationException("It's imposible to save any other locator configuration except local or default. Key for local = " + localKey
		    + ", and key for default = " + defaultKey);
	}
    }

    @Override
    public ISerialiser getSerialiser() {
	return locatorController.getSerialiser();
    }

    @Override
    public boolean exists(final String key) {
	if (key.equals(localKey)) {
	    return locatorRetriever.getLocatorConfiguration(key) != null;
	} else if (key.equals(defaultKey)) {
	    return locatorController.exists(key);
	}
	return false;
    }

    @Override
    public void removeConfiguration(final String key) {
	throw new UnsupportedOperationException("Locatro's configuration can not be removed");
    }

    @Override
    public String generateKeyForAutocompleterConfiguration(final Class<?> entityType, final String propertyName) {
	throw new UnsupportedOperationException("Generating key for local locator configuration is unsupported.");
    }

    @Override
    public String generateKeyForDefaultAutocompleterConfiguration(final Class<?> forType) {
	throw new UnsupportedOperationException("Generating key for default locator configuration is unsupported.");
    }

    /**
     * Returns the look up class.
     * 
     * @return
     */
    public Class<?> getResultantEntityClass() {
	return resultantEntityClass;
    }

    /**
     * Returns key for local locator's configuration.
     * 
     * @return
     */
    public String getLocalKey() {
	return localKey;
    }

    /**
     * Returns key for default locator's configuration.
     * 
     * @return
     */
    public String getDefaultKey() {
	return defaultKey;
    }

    @Override
    public Result canRemove(final String centerKey) {
	return new Result(new UnsupportedOperationException("Locatro's configuration can not be removed"));
    }

    @Override
    public Result canConfigureAnalysis(final String centerKey) {
	return new Result(new UnsupportedOperationException("Locatro's analysis can not be configured"));
    }

    @Override
    public Result canSave(final String locatorKey) {
	if (locatorKey.equals(localKey)) {
	    return locatorRetriever.canSave(locatorKey);
	} else if (locatorKey.equals(defaultKey)) {
	    return locatorController.canSave(locatorKey);
	}
	return new Result(new IllegalArgumentException("The " + locatorKey + "is undefined"));
    }

    @Override
    public Result canConfigure(final String locatorKey) {
	if (locatorKey.equals(localKey)) {
	    return locatorRetriever.canConfigure(locatorKey);
	} else if (locatorKey.equals(defaultKey)) {
	    return locatorController.canConfigure(locatorKey);
	}
	return new Result(new IllegalArgumentException("The " + locatorKey + "is undefined"));
    }
}