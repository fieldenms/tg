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
     * A factory method to create a default fetch provider for specified <code>entityType</code> with instrumentation.
     * <p>
     * IMPORTANT: please do not use this method, use {@link EntityUtils#fetch(Class)} method instead.
     *
     * @param entityType -- the type of the property
     * @param instrumented -- indicates whether fetched instances should be instrumented
     *
     * @return
     */
    public static <T extends AbstractEntity<?>> FetchProvider<T> createDefaultFetchProvider(final Class<T> entityType, final boolean instrumented) {
        // empty fetch provider -- version and id -- fetchOnly analog
        return new FetchProvider<T>(entityType, false, instrumented);
    }

    /**
     * A factory method to create a fetch provider for specified <code>entityType</code> with 'key' and 'desc' and instrumentation.
     * <p>
     * IMPORTANT: please do not use this method, use {@link EntityUtils#fetchWithKeyAndDesc(Class)} method instead.
     *
     * @param entityType -- the type of the property
     * @param instrumented -- indicates whether fetched instances should be instrumented
     *
     * @return
     */
    public static <T extends AbstractEntity<?>> FetchProvider<T> createFetchProviderWithKeyAndDesc(final Class<T> entityType, final boolean instrumented) {
        // empty fetch provider -- version and id -- fetchOnly analog
        return new FetchProvider<T>(entityType, true, instrumented);
    }
}
