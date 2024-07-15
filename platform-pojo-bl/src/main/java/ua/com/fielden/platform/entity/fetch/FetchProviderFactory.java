package ua.com.fielden.platform.entity.fetch;

import static ua.com.fielden.platform.entity.query.fluent.fetch.FetchCategory.ID_AND_VERSION;
import static ua.com.fielden.platform.entity.query.fluent.fetch.FetchCategory.KEY_AND_DESC;
import static ua.com.fielden.platform.entity.query.fluent.fetch.FetchCategory.NONE;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.fetch.FetchCategory;
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
    
    private FetchProviderFactory() {}

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
        return new FetchProvider<>(entityType, ID_AND_VERSION, instrumented);
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
        return new FetchProvider<>(entityType, KEY_AND_DESC, instrumented);
    }
    
    /**
     * A factory method to create a fetch provider for specified <code>entityType</code> with no properties.
     * <p>
     * IMPORTANT: please do not use this method, use {@link EntityUtils#fetchNone(Class)} method instead.
     *
     * @param entityType -- the type of the property
     * @param instrumented -- indicates whether fetched instances should be instrumented
     *
     * @return
     */
    public static <T extends AbstractEntity<?>> FetchProvider<T> createEmptyFetchProvider(final Class<T> entityType, final boolean instrumented) {
        // empty fetch provider -- no properties -- fetchNone analog
        return new FetchProvider<>(entityType, NONE, instrumented);
    }
    
}