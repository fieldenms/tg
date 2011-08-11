package ua.com.fielden.platform.ui.config.api.interaction;

/**
 * {@link IConfigurationController} for master's entity locators configuration.
 * 
 * @author oleh
 * 
 */
public interface IMasterConfigurationController extends IConfigurationController {

    /**
     * Returns key for the master's autocompleter configuration.
     * 
     * @param forType
     *            - the master model type.
     * @return
     */
    String generateKeyForMasterConfiguration(final Class<?> forType);

}
