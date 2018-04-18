package ua.com.fielden.platform.types.try_wrapper;

/**
 * A contract that describes a side-effectful computation that may throw an exception.
 *
 * @author TG Team
 *
 */

@FunctionalInterface
public interface FailableRunnable {

    /**
     * Executes a side-effectful computation.
     *
     * @throws Exception
     *             if it fails
     */
    public void run() throws Exception;
}