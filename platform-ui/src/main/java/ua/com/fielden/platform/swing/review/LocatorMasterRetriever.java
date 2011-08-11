package ua.com.fielden.platform.swing.review;

import org.apache.log4j.Logger;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.swing.locator.ILocatorConfigurationRetriever;
import ua.com.fielden.platform.ui.config.api.interaction.IMasterConfigurationController;

public class LocatorMasterRetriever implements ILocatorConfigurationRetriever {

    private final IMasterConfigurationController masterController;
    private final Class<? extends AbstractEntity> entityType;
    private final LocatorPersistentObject locatorPersistentObject;

    private final Logger logger;

    public LocatorMasterRetriever(final IMasterConfigurationController masterController, final Class<? extends AbstractEntity> entityType) {
	this.logger = Logger.getLogger(this.getClass());
	this.masterController = masterController;
	this.entityType = entityType;
	LocatorPersistentObject persistedObject = null;
	try {
	    persistedObject = new LocatorPersistentObject(masterController.getSerialiser().deserialise(masterController.load(masterController.generateKeyForMasterConfiguration(entityType)), LocatorPersistentObject.class));
	} catch (final Exception e) {
	    logger.debug(e.getMessage());
	}
	this.locatorPersistentObject = new LocatorPersistentObject(persistedObject);
	this.locatorPersistentObject.setBaseConfigurationManager(masterController.generateKeyForMasterConfiguration(entityType), masterController);
    }

    @Override
    public DynamicCriteriaPersistentObjectUi getLocatorConfiguration(final String key) {
	return locatorPersistentObject.getLocatorConfiguration(key);
    }

    @Override
    public void setLocatorConfiguration(final String key, final DynamicCriteriaPersistentObjectUi locatorConfiguration) {
	locatorPersistentObject.setLocatorConfiguration(key, locatorConfiguration);
	masterController.save(masterController.generateKeyForMasterConfiguration(entityType), masterController.getSerialiser().serialise(locatorPersistentObject));
    }

    @Override
    public Result canSave(final String locatorKey) {
	return locatorPersistentObject.canSave(locatorKey);
    }

    @Override
    public Result canConfigure(final String locatorKey) {
	return locatorPersistentObject.canConfigure(locatorKey);
    }

}
