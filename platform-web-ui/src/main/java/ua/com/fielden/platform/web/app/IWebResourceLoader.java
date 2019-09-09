package ua.com.fielden.platform.web.app;

import java.io.InputStream;
import java.util.Optional;

/**
 * The contract for loading resources by their URIs of filePaths.
 *
 * @author TG Team
 *
 */
public interface IWebResourceLoader {
    
    /**
     * Loads the text representation of the resource with the specified 'resourceURI'.
     * <p>
     * Please, note that the resources should be accessed through the '/resources' prefix or one of the prefixes for generated resources: '/app', '/master_ui' or '/centre_ui'.
     * <p>
     * An exception is thrown is the specified resources do not exists.
     *
     * @param resourceURI
     *
     * @return
     */
    String loadSource(final String resourceURI);
    
    /**
     * Loads input stream representation of the resource with the specified 'filePath'.
     *
     * @param resourceURI
     *
     * @return
     */
    InputStream loadStreamWithFilePath(final String filePath);
    
    /**
     * Loads checksum for resource if available. Otherwise, returns empty {@link Optional}.
     * <p>
     * Checksums are available for static resources in deployment mode. 'startup-resources-vulcanized.js' file is primary in this category.
     * Client-side Service Worker script intercepts requests to get checksum first to compare whether resource has changed.
     * If that is true then full resource will be re-downloaded and re-cached on the client side.
     * Otherwise the cached resource will be used straight away.
     * 
     * @param resourceURI
     * @return
     */
    Optional<String> checksum(final String resourceURI);
    
}