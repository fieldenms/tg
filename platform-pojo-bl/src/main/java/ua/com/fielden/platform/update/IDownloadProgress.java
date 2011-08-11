package ua.com.fielden.platform.update;

/**
 * A contract for indicating a download progress of a dependency.
 * 
 * @author TG Team
 * 
 */
public interface IDownloadProgress {
    /**
     * Updates progress indicator with a total number of bytes read at the time of method invocation.
     * 
     * @param totalNumOfBytesRead
     */
    void update(long totalNumOfBytesRead);
}
