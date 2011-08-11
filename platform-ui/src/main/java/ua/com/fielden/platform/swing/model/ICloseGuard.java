package ua.com.fielden.platform.swing.model;

/**
 * A contract to be implemented by UI view.
 * <p>
 * The main purpose of this interface is to provide a hook up for places responsible for creation and closing of views. For example, if a view representing some entity, which has
 * been modified, but not save, is being closed then implementation of ICloseGuard should provide an indication that the view cannot be closed without first saving or cancelling
 * changes.
 * 
 * However, this is the responsibility of the caller to check whether a view can be closed.
 * <p>
 * In most cases the model rather than view would have enough information to decide whether view can be closed. Thus, the view implementation of the interface would most likely be
 * a redirection of a call to its model.
 * 
 * @author TG Team
 * 
 */
public interface ICloseGuard {
    /**
     * Should return <code>null</code> if a view can be closed, or an instance of the guard preventing closing otherwise.
     * 
     * @return
     */
    ICloseGuard canClose();

    /**
     * Should return a message explaining why the guarded view cannot be closed.
     * 
     * @return
     */
    String whyCannotClose();

    /**
     * Method which is invoked when guarded item is being closed. Before closing this item one must check whether it is possible to close it invoking {@link #canClose()} routine.
     */
    void close();

    /**
     * Should return value that indicates whether view can be leave or not.
     * 
     * @return
     */
    boolean canLeave();
}
