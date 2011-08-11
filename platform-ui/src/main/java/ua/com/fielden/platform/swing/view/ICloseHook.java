package ua.com.fielden.platform.swing.view;

/**
 * This is a contract that should be implemented by holders of the guarded frame.
 * <p>
 * One possible purpose of this interface is to remove any reference to the frame once it's closed.
 * 
 * @author 01es
 * 
 */
public interface ICloseHook<F extends BaseFrame> {
    /**
     * Notifies the hook that the frame is closed.
     * 
     * @param frame
     *            -- instance, which is being closed.
     */
    void closed(F frame);
}
