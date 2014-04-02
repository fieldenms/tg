package ua.com.fielden.platform.dao;

/**
 * A contract for any kind of computation for monitoring and management purposes.
 * 
 * @author TG Team
 * 
 */
public interface IComputationMonitor {

    /**
     * A request to stop computation.
     * 
     * @return
     */
    boolean stop();

    /**
     * A request to get the current computation progress.
     * <p>
     * A integer value from 0 to 100 is expected to indicate percentage of computation completed, or <code>null</code> if such information is not applicable (e.g. indefinite
     * computation such as request to a database).
     * 
     * @return
     */
    Integer progress();

}
