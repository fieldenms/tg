package ua.com.fielden.platform.update;

import java.util.Map;

import ua.com.fielden.platform.utils.Pair;

/**
 * A contract for obtaining a list of reference dependencies and downloading missing dependencies to the client.
 * 
 * @author TG Team
 * 
 */
public interface IReferenceDependancyController {
    /**
     * Should provide a map of dependencies (file names) together with corresponding checksums and sizes.
     * 
     * @return
     */
    Map<String, Pair<String, Long>> dependencyInfo();

    /**
     * Should download a dependency with the provided file name.
     * 
     * @param dependencyFileName
     * @return
     */
    byte[] download(final String dependencyFileName, final String expectedCehcksum, final IDownloadProgress progress);
}
