package ua.com.fielden.platform.web.app;

import java.io.InputStream;
import java.util.Optional;

/**
 * The contract for loading resources by their URIs.
 *
 * @author TG Team
 *
 */
public interface IWebResourceLoader {
    
    /**
     * Loads the text representation of the resource with the specified 'resourceUri'.
     * <p>
     * Please, note that the resources should be accessed through the '/resources' prefix or one of the prefixes for generated resources: '/app', '/master_ui' or '/centre_ui'.
     * <p>
     * An exception is thrown if the specified resource does not exist.
     *
     * @param resourceUri
     *
     * @return
     */
    Optional<String> loadSource(final String resourceUri);
    
    /**
     * Loads input stream for the resource identified with {@code resourceUri}.
     * <p>
     * An exception is thrown if the specified resource does not exist.
     *
     * @param resourceUri
     *
     * @return
     */
    InputStream loadStream(final String resourceUri);
    
    /**
     * Loads checksum for resource if available. Otherwise, returns empty {@link Optional}.
     * <p>
     * Checksums are available for static resources in deployment mode. 'startup-resources-vulcanized.js' file is primary in this category.
     * Client-side Service Worker script intercepts requests to get checksum first to compare whether resource has changed.
     * If that is true then full resource will be re-downloaded and re-cached on the client side.
     * Otherwise the cached resource will be used straight away.
     * 
     * @param resourceUri
     * @return
     */
    Optional<String> checksum(final String resourceUri);
    
}