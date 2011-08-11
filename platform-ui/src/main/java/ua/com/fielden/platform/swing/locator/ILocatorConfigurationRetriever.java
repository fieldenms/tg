package ua.com.fielden.platform.swing.locator;

import ua.com.fielden.platform.swing.review.DynamicCriteriaPersistentObjectUi;
import ua.com.fielden.platform.ui.config.api.interaction.ILocatorConfigurationManager;

/**
 * Contract for anything that returns or updates locator configuration.
 * 
 * @author oleh
 * 
 */
public interface ILocatorConfigurationRetriever extends ILocatorConfigurationManager {

    /**
     * Returns the Locator configuration for the specified key.
     * 
     * @param key
     * @return
     */
    DynamicCriteriaPersistentObjectUi getLocatorConfiguration(String key);

    /**
     * Set locators configuration for specified key.
     * 
     * @param key
     * @param locatorConfiguration
     */
    void setLocatorConfiguration(String key, DynamicCriteriaPersistentObjectUi locatorConfiguration);
}
