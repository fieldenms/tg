package ua.com.fielden.platform.entity.fetch;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.utils.EntityUtils;

/**
 * A factory for fetch providers.
 * <p>
 * IMPORTANT: please do not use this factory, use {@link EntityUtils#fetch(Class)} method.
 *
 * @author TG Team
 *
 */
public class FetchProviderFactory {

    /**
     * A factory method to create a default fetch provider for specified <code>entityType</code>.
     * <p>
     * IMPORTANT: please do not use this method, use {@link EntityUtils#fetch(Class)} method instead.
     *
     * @param entityType
     *            -- the type of the property
     *
     * @return
     */
    public static <T extends AbstractEntity<?>> FetchProvider<T> createDefaultFetchProvider(final Class<T> entityType) {
        // empty fetch provider -- version and id -- fetchOnly analog
        return new FetchProvider<T>(entityType, false);
    }

    /**
     * A factory method to create a fetch provider for specified <code>entityType</code> with 'key' and 'desc'.
     * <p>
     * IMPORTANT: please do not use this method, use {@link EntityUtils#fetchWithKeyAndDesc(Class)} method instead.
     *
     * @param entityType
     *            -- the type of the property
     *
     * @return
     */
    public static <T extends AbstractEntity<?>> FetchProvider<T> createFetchProviderWithKeyAndDesc(final Class<T> entityType) {
        // empty fetch provider -- version and id -- fetchOnly analog
        return new FetchProvider<T>(entityType, true);
    }
}
