package ua.com.fielden.platform.ui.config.api.interaction;

/**
 * {@link IConfigurationController} for entity locators.
 * 
 * @author oleh
 * 
 */
public interface ILocatorConfigurationController extends IConfigurationController {

    /**
     * Returns key for the autocompleter specified with property name.
     * 
     * @param entityType
     *            - entity type for property name.
     * @param propertyName
     *            - the property name for which autocompleter is created.
     * 
     * @return
     */
    String generateKeyForAutocompleterConfiguration(Class<?> entityType, final String propertyName);

    /**
     * Returns the key for default autocompleter configuration specified with autocompleter's look up class.
     * 
     * @param forType
     *            - the type for which key should be generated.
     * @return
     */
    String generateKeyForDefaultAutocompleterConfiguration(final Class<?> forType);

}
