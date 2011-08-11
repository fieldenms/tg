package ua.com.fielden.platform.equery.lifecycle;

/**
 * Interface for dynamic progress information updating. Should be used for long processes to mark the stages of process progress.
 *
 * @author TG Team
 *
 */
public interface IProgressUpdater {

    /**
     * Updates progress indicator by specific message.
     *
     * @param message
     */
    void updateProgress(final String message);
}