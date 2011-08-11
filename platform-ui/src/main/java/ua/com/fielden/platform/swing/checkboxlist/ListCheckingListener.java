package ua.com.fielden.platform.swing.checkboxlist;

import java.util.EventListener;

/**
 * The listener notified when the checking in a {@link ListCheckingModel} changes.
 * 
 * @see CheckboxList
 * @author oleh
 */
public interface ListCheckingListener<T> extends EventListener {

    /**
     * Called whenever the value of the checking changes.
     * 
     * @param e
     *            - the event that characterizes the change.
     */
    void valueChanged(final ListCheckingEvent<T> e);
}
