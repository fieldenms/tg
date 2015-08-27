package ua.com.fielden.platform.web.app;

import java.util.LinkedHashSet;

/**
 * The contract of getting preloaded resources URIs, that will be preloaded during web UI index.html loading.
 *
 * @author TG Team
 *
 */
public interface IPreloadedResources {
    /**
     * Returns app-specific preloaded resources.
     *
     * @return
     */
    LinkedHashSet<String> get();

    /**
     * Returns dependent resources URIs.
     *
     * @return
     */
    LinkedHashSet<String> get(final String resourceURI);

    /**
     * Returns dependent resources URIs including transitive.
     *
     * @return
     */
    LinkedHashSet<String> getAll(final String resourceURI);

    /**
     * Returns the text representation of the resource with the specified 'resourceURI'.
     * <p>
     * Please, note that the resources should be accessed through the '/resources' prefix or one of the prefixes for generated resources: '/app', '/master_ui' or '/centre_ui'.
     *
     * @param resourceURI
     * @return
     */
    String getSource(final String resourceURI);
}
