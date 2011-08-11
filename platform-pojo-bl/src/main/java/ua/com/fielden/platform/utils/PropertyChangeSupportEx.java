package ua.com.fielden.platform.utils;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeListenerProxy;
import java.beans.PropertyChangeSupport;

import ua.com.fielden.platform.entity.ioc.ObservableMutatorInterceptor;
import ua.com.fielden.platform.entity.meta.MetaProperty;

/**
 * Use this PropertyChangeSupport in cases when need to fire property change events for :
 * <p>
 * 
 * 1. !oldValue.equals(newValue) - see CHECK_EQUALITY strategy (default PropertyChangeListener behaviour)
 * <p>
 * 
 * 2. oldValue != newValue - see CHECK_IDENTITY strategy
 * <p>
 * 
 * 3. without any checking - see CHECK_NOTHING strategy
 * <p>
 * 
 * @author jhou
 * 
 */
public final class PropertyChangeSupportEx extends PropertyChangeSupport {
    private static final long serialVersionUID = -4489365154800642694L;

    /**
     * The object to be provided as the "source" for any generated events.
     * 
     * @serial
     */
    private final Object source;

    /**
     * The default setting for the "checkingStrategy". Can be overridden by the #firePropertyChange methods that accept a {@code checkingStrategy} parameter.
     */
    private final CheckingStrategy checkingStrategyDefault;

    /**
     * The strategies of old/newValue checking in this ExtendedPropertyChangeSupport
     * 
     * @author jhou
     * 
     */
    public enum CheckingStrategy {
	CHECK_EQUALITY, CHECK_IDENTITY, CHECK_NOTHING
    }

    // Instance Creation ******************************************************

    /**
     * Constructs an ExtendedPropertyChangeSupport object. It behaves like simple PropertyChangeSupport in standart methods like :
     * <p>
     * 
     * "firePropertyChange(final PropertyChangeEvent evt)" or "firePropertyChange(final String propertyName, final Object oldValue, final Object newValue)"
     * 
     * @param sourceBean
     *            The bean to be given as the source for any events.
     */
    public PropertyChangeSupportEx(final Object sourceBean) {
	this(sourceBean, CheckingStrategy.CHECK_EQUALITY);
    }

    /**
     * Constructs an ExtendedPropertyChangeSupport object with the specified default test method for differences between the old and new property values.
     * 
     * @param sourceBean
     *            The object provided as the source for any generated events.
     * @param checkIdentityDefault
     *            true enables the identity check by default
     */
    public PropertyChangeSupportEx(final Object sourceBean, final CheckingStrategy checkingStrategyDefault) {
	super(sourceBean);
	this.source = sourceBean;
	this.checkingStrategyDefault = checkingStrategyDefault;
    }

    // Firing Events **********************************************************

    /**
     * Fires the specified PropertyChangeEvent to any registered listeners. Uses the default test ({@code #equals} vs. {@code ==}) to determine whether the event's old and new
     * values are different. No event is fired if old and new value are the same.
     * 
     * @param evt
     *            The PropertyChangeEvent object.
     * 
     *            Note : processes ALL PropertyChangeEvents! (including PropertyChangeOrIncorrectAttemptListeners)
     * 
     * @see PropertyChangeSupport#firePropertyChange(PropertyChangeEvent)
     */
    @Override
    public void firePropertyChange(final PropertyChangeEvent evt) {
	firePropertyChange(evt, checkingStrategyDefault, false);
    }

    /**
     * Reports a bound property update to any registered listeners. Uses the default test ({@code #equals} vs. {@code ==}) to determine whether the event's old and new values are
     * different. No event is fired if old and new value are the same.
     * 
     * @param propertyName
     *            The programmatic name of the property that was changed.
     * @param oldValue
     *            The old value of the property.
     * @param newValue
     *            The new value of the property.
     * 
     *            Note : processes ALL PropertyChangeEvents! (including PropertyChangeOrIncorrectAttemptListeners)
     * 
     * @see PropertyChangeSupport#firePropertyChange(String, Object, Object)
     */
    @Override
    public void firePropertyChange(final String propertyName, final Object oldValue, final Object newValue) {
	firePropertyChange(propertyName, oldValue, newValue, checkingStrategyDefault, false);
    }

    /**
     * Fires an existing PropertyChangeEvent to any registered listeners. The boolean parameter specifies whether differences between the old and new value are tested using
     * {@code ==} or {@code #equals}. No event is fired if old and new value are the same.
     * 
     * @param evt
     *            The PropertyChangeEvent object.
     * @param checkIdentity
     *            true to check differences using {@code ==} false to use {@code #equals}.
     * @param fireOnlyIncorrectAttemptListeners
     *            - if <code>true</code> fires ONLY the {@link PropertyChangeListener}s of type {@link PropertyChangeOrIncorrectAttemptListener}, if <code>false</code> - fires ALL
     *            {@link PropertyChangeListener}s
     */
    public void firePropertyChange(final PropertyChangeEvent evt, final CheckingStrategy checkingStrategy, final boolean fireOnlyIncorrectAttemptListeners) {
	final Object oldValue = evt.getOldValue();
	final Object newValue = evt.getNewValue();
	if (checkingStrategy == CheckingStrategy.CHECK_NOTHING) {
	    fireUnchecked(evt, fireOnlyIncorrectAttemptListeners);
	} else if (checkingStrategy == CheckingStrategy.CHECK_IDENTITY) {
	    if (oldValue != null && oldValue == newValue) {
		return;
	    }
	    fireUnchecked(evt, fireOnlyIncorrectAttemptListeners);
	} else { // checkingStrategy == CHECK_EQUALITY
	    if (oldValue != null && newValue != null && oldValue.equals(newValue)) {
		return;
	    }
	    fireUnchecked(evt, fireOnlyIncorrectAttemptListeners);
	}
    }

    /**
     * Reports a bound property update to any registered listeners. No event is fired if the old and new value are the same. If checkIdentity is {@code true} an event is fired in
     * all other cases. If this parameter is {@code false}, an event is fired if old and new values are not equal.
     * 
     * @param propertyName
     *            The programmatic name of the property that was changed.
     * @param oldValue
     *            The old value of the property.
     * @param newValue
     *            The new value of the property.
     * @param checkIdentity
     *            true to check differences using {@code ==} false to use {@code #equals}.
     * @param fireOnlyIncorrectAttemptListeners
     *            - if <code>true</code> fires ONLY the {@link PropertyChangeListener}s of type {@link PropertyChangeOrIncorrectAttemptListener}, if <code>false</code> - fires ALL
     *            {@link PropertyChangeListener}s
     */
    public void firePropertyChange(final String propertyName, final Object oldValue, final Object newValue, final CheckingStrategy checkingStrategy, final boolean fireOnlyIncorrectAttemptListeners) {
	if (checkingStrategy == CheckingStrategy.CHECK_NOTHING) {
	    fireUnchecked(new PropertyChangeEvent(source, propertyName, oldValue, newValue), fireOnlyIncorrectAttemptListeners);
	} else if (checkingStrategy == CheckingStrategy.CHECK_IDENTITY) {
	    if (oldValue != null && oldValue == newValue) {
		return;
	    }
	    fireUnchecked(new PropertyChangeEvent(source, propertyName, oldValue, newValue), fireOnlyIncorrectAttemptListeners);
	} else { // checkingStrategy == CHECK_EQUALITY
	    if (oldValue != null && newValue != null && oldValue.equals(newValue)) {
		return;
	    }
	    fireUnchecked(new PropertyChangeEvent(source, propertyName, oldValue, newValue), fireOnlyIncorrectAttemptListeners);
	}
    }

    /**
     * Fires a PropertyChangeEvent to all its listeners without checking via equals method if the old value is equal to new value. The instance equality check is done by the
     * calling firePropertyChange method (to avoid instance creation of the PropertyChangeEvent).
     * <p>
     * 
     * If some listeners have been added with a named property, then {@code PropertyChangeSupport#getPropertyChangeListeners()} returns an array with a mixture of
     * PropertyChangeListeners and {@code PropertyChangeListenerProxy}s. We notify all non-proxies and those proxies that have a property name that is equals to the event's
     * property name.
     * 
     * @param evt
     *            event to fire to the listeners
     * 
     * @param fireOnlyIncorrectAttemptListeners
     *            - if <code>true</code> fires ONLY the {@link PropertyChangeListener}s of type {@link PropertyChangeOrIncorrectAttemptListener}, if <code>false</code> - fires ALL
     *            {@link PropertyChangeListener}s
     * 
     * @see PropertyChangeListenerProxy
     * @see PropertyChangeSupport#getPropertyChangeListeners()
     */
    private void fireUnchecked(final PropertyChangeEvent evt, final boolean fireOnlyIncorrectAttemptListeners) {
	PropertyChangeListener[] listeners;
	synchronized (this) {
	    listeners = getPropertyChangeListeners();
	}
	for (final PropertyChangeListener listener : listeners) {
	    final String propertyName = evt.getPropertyName();
	    if (listener instanceof PropertyChangeListenerProxy) {
		final PropertyChangeListenerProxy proxy = (PropertyChangeListenerProxy) listener;
		final boolean theProxyListenerNeedToBeProcessed = fireOnlyIncorrectAttemptListeners ? (proxy.getListener() instanceof PropertyChangeOrIncorrectAttemptListener)
			: true;
		if (theProxyListenerNeedToBeProcessed && proxy.getPropertyName().equals(propertyName)) {
		    proxy.propertyChange(evt);
		}
	    } else {
		final boolean theSimpleListenerNeedToBeProcessed = fireOnlyIncorrectAttemptListeners ? (listener instanceof PropertyChangeOrIncorrectAttemptListener) : true;
		if (theSimpleListenerNeedToBeProcessed) {
		    listener.propertyChange(evt);
		}
	    }
	}
    }

    /**
     * This is the specific Listener to be used as simple PropertyChangeListener but to be fired not only in the case of succeeded "setting" the value but also in the case of
     * incorrect attempt of setting the value. This firing is supported in {@link ObservableMutatorInterceptor} after validation fails. This is the useful firing in the case for
     * e.g. SubjectValueChangeHandler(Bind API) when the bounded component updates by the incorrect value! Also can be used for the testing or other purposes, but need to be
     * careful not to use it very often. Note : for the validation results changing -> better to use listeners assigned to {@link MetaProperty}'s "validationResults" property
     * 
     * <p>
     * IMPORTANT : after the validation fails -> the firing invokes only for {@link PropertyChangeOrIncorrectAttemptListener} type of listeners!
     * 
     * @author jhou & Yura
     * 
     */
    public interface PropertyChangeOrIncorrectAttemptListener extends PropertyChangeListener {
    }

}
