package ua.com.fielden.platform.entity;

import java.beans.PropertyChangeListener;

import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.utils.PropertyChangeSupportEx;
import ua.com.fielden.platform.utils.PropertyChangeSupportEx.PropertyChangeOrIncorrectAttemptListener;

/**
 * "Entity" interface for Bind API.
 *
 * @author Jhou
 *
 */
public interface IBindingEntity {

    /**
     * Dynamic setter for setting property value.
     *
     * @param propertyName
     * @param value
     */
    void set(final String propertyName, final Object value);

    /**
     * Dynamic getter for accessing property value.
     *
     * @param propertyName
     * @return
     */
    <T> T get(final String propertyName);

    PropertyChangeSupportEx getChangeSupport();

    /**
     * Registers property change listener.<br>
     * <br>
     * Note : Please, refer also to {@link PropertyChangeOrIncorrectAttemptListener} JavaDocs.
     *
     * @param propertyName
     * @param listener
     */
    void addPropertyChangeListener(final String propertyName, final PropertyChangeListener listener);

    /**
     * Removes property change listener.
     */
    void removePropertyChangeListener(final String propertyName, final PropertyChangeListener listener);

    /**
     * Returns property by name.
     *
     * @param name
     * @return
     */
    <T> MetaProperty<T> getProperty(final String name);

    /**
     * Returns the type of property specified by <code>propertyName</code>.
     *
     * @param propertyName
     * @return
     */
    Class<?> getPropertyType(final String propertyName);

    /**
     * Increases <code>lockCount</code> by one in a thread safe manner.
     */
    void lock();

    /**
     * Decreases <code>lockCount</code> by one in a thread safe manner. Signals lock condition <code>validationInProgress</code> when <code>lockCount</code> reaches value zero.
     */
    void unlock();

}
