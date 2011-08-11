package ua.com.fielden.platform.swing.model;

/**
 * A contract to be implemented by UI view.
 * <p>
 * The main purpose of this interface is to provide a hook up for places responsible for creation of views. For example, if a view representing some entity, has some external
 * dependency, which was not fulfilled, then the view may block its opening (visualisation).
 * 
 * However, this is the responsibility of the caller to check whether a view can be open.
 * <p>
 * In most cases the model rather than view would have enough information to decide whether view can be open. Thus, the view implementation of the interface would most likely be a
 * redirection of a call to its model.
 * 
 * @author TG Team
 * 
 */
public interface IOpenGuard {
    /**
     * Should return <code>true</code> if a view can be open.
     * 
     * @return
     */
    boolean canOpen();

    /**
     * Should return a message explaining why the guarded view cannot be open.
     * 
     * @return
     */
    String whyCannotOpen();
}
