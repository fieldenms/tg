package ua.com.fielden.platform.ui.config.api.interaction;

import ua.com.fielden.platform.serialisation.api.ISerialiser;

/**
 * Controller contract for saving and loading {@link DynamicCriteriaPersistentObjectUi}s using specified keys.
 * 
 * @author oleh
 * 
 */
public interface IConfigurationController extends IConfigurationManager {

    /**
     * Saves the specified instance of {@link DynamicCriteriaPersistentObjectUi}.
     * 
     * @param key
     *            - string that identifies objectToSave.
     * @param objectToSave
     *            - {@link DynamicCriteriaPersistentObjectUi} that must be saved.
     */
    void save(String key, byte[] objectToSave);

    /**
     * Returns {@link DynamicCriteriaPersistentObjectUi} for the specified key.
     * 
     * @param key
     *            - string that identifies object to load.
     * @return
     */
    byte[] load(String key);

    /**
     * Returns the {@link ISerialiser} instance used for {@link DynamicCriteriaPersistentObjectUi} serialisation process.
     * 
     * @return
     */
    public ISerialiser getSerialiser();

    /**
     * Returns value that indicates whether exists configuration for the specified key or not.
     * 
     * @param key
     *            - configuration key.
     * @return
     */
    public boolean exists(final String key);

    /**
     * Removes the configuration for the specified key.
     * 
     * @param key
     *            - specifies the configuration to remove.
     */
    public void removeConfiguration(String key);
}
