package ua.com.fielden.platform.update;

import java.util.Map;

/**
 * A contract used by the {@link Updater} to provide a feedback about currently performed update action.
 * 
 * @author TG Team
 * 
 */
public interface IUpdateActionFeedback extends IDownloadProgress {

    /**
     * Indicates that a dependency information is being obtained.
     */
    void checkDependency();

    /**
     * Should be used to specify what dependency actions are going to be performed.
     * 
     * @param map
     */
    void dependencyActions(Map<String, DependencyAction> map);

    /**
     * Indication that a backup of the current dependencies started.
     */
    void backuping();

    /**
     * Indication that restoring dependencies from backup is in progress, which should only be result of an unsuccessful update.
     */
    void restoring();

    /**
     * When invoked indicates a start of the specified action applied to the provided dependency file.
     * 
     * @param dependencyFileName
     * @param fileSize
     * @param action
     */
    void start(final String dependencyFileName, final Long fileSize, final DependencyAction action);

    /**
     * When invoked indicates the end of the specified action on the provided dependency file. The <code>result</code> indicates the result of the action.
     * 
     * @param dependencyFileName
     * @param action
     * @param result
     */
    void finish(final String dependencyFileName, final DependencyAction action, final DependencyActionResult result);

    /**
     * Should be invoked when the update process is completed successfully.
     * 
     * @param msg
     */
    void updateCompleted(final String msg);

    /**
     * Should be invoked when the update process has failed.
     * 
     * @param msg
     */
    void updateFailed(final Exception ex);
}
