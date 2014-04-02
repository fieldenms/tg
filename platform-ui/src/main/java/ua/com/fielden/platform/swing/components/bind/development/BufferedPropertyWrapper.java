package ua.com.fielden.platform.swing.components.bind.development;

/*
 * Copyright (c) 2002-2008 JGoodies Karsten Lentzsch. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  o Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  o Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 *  o Neither the name of JGoodies Karsten Lentzsch nor the names of
 *    its contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import ua.com.fielden.platform.entity.IBindingEntity;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.swing.components.bind.development.Binder.IPropertyConnector;
import ua.com.fielden.platform.swing.components.bind.development.Binder.IRebindable;
import ua.com.fielden.platform.swing.components.bind.development.ComponentFactory.IOnCommitAction;
import ua.com.fielden.platform.swing.components.bind.development.ComponentFactory.IOnCommitActionable;
import ua.com.fielden.platform.utils.PropertyChangeSupportEx;
import ua.com.fielden.platform.utils.PropertyChangeSupportEx.PropertyChangeOrIncorrectAttemptListener;

import com.jgoodies.binding.value.Trigger;
import com.jgoodies.binding.value.ValueModel;

/**
 * This class represents the BufferedProperty, from which binded component can be created. Constructs from AbstractEntity, PropertyName and Trigger. The Trigger - can be used in
 * triggerCommit, triggerFlush methods, to commit/flush changes of the assigned properties. E.g. SaveButtonTrigger can be assigned to SaveButton and need to be assigned to All
 * properties that have to be saved by SaveButtonClick
 * 
 * 10.02.2009 YN : Added key type of type {@link String}, because this class cannot be created without {@link KeyType} annotation
 * 
 * ====================Jdocs from jgoodies Buffered Value Model ======================
 * 
 * 
 * A ValueModel that wraps another ValueModel, the subject, and delays changes of the subject's value. Returns the subject's value until a value has been set. The buffered value is
 * not written to the subject until the trigger channel changes to <code>Boolean.TRUE</code>. The buffered value can be flushed by changing the trigger channel value to
 * <code>Boolean.FALSE</code>. Note that the commit and flush events are performed only if the trigger channel fires a change event. Since a plain ValueHolder fires no property
 * change event if a value is set that has been set before, it is recommended to use a {@link Trigger} instead and invoke its <code>#triggerCommit</code> and
 * <code>triggerFlush</code> methods.
 * <p>
 * 
 * The BufferedValueModel has been designed to behave much like its subject when accessing the value. Therefore it throws all exceptions that would arise when accessing the subject
 * directly. Hence, attempts to read or write a value while the subject is {@code null} are always rejected with a <code>NullPointerException</code>.
 * <p>
 * 
 * This class provides the bound read-write properties <em>subject</em> and <em>triggerChannel</em> for the subject and trigger channel and a bound read-only property
 * <em>buffering</em> for the buffering state.
 * <p>
 * 
 * The BufferedValueModel registers listeners with the subject and trigger channel. It is recommended to remove these listeners by invoking <code>#release</code> if the subject and
 * trigger channel live much longer than this buffer. After <code>#release</code> has been called you must not use the BufferedValueModel instance any longer. As an alternative you
 * may use event listener lists in subjects and trigger channels that are based on <code>WeakReference</code>s.
 * <p>
 * 
 * If the subject value changes while this model is in buffering state this change won't show through as this model's new value. If you want to update the value whenever the
 * subject value changes, register a listener with the subject value and flush this model's trigger.
 * <p>
 * 
 * <strong>Constraints:</strong> The subject is of type <code>Object</code>, the trigger channel value of type <code>Boolean</code>.
 * 
 * @author Karsten Lentzsch
 * @version $Revision: 1.12 $
 * 
 * @see ValueModel
 * @see ValueModel#getValue()
 * @see ValueModel#setValue(Object)
 */
@KeyType(String.class)
public final class BufferedPropertyWrapper implements IBindingEntity, IOnCommitActionable, IRebindable { // extends AbstractEntity<String>
    private List<IPropertyConnector> connectors = new ArrayList<IPropertyConnector>();
    /**
     * Provides property change support.
     */
    private final PropertyChangeSupportEx changeSupport;
    private final Logger logger = Logger.getLogger(this.getClass());

    public boolean addPropertyConnector(final IPropertyConnector propertyConnector) {
        return connectors.add(propertyConnector);
    }

    private static final long serialVersionUID = 1L;
    // Names of the bound bean properties *************************************

    /**
     * The name of the bound read-only bean property that indicates whether this models is buffering or in write-through state.
     * 
     * @see #isBuffering()
     */
    public static final String PROPERTYNAME_BUFFERING = "buffering";

    /**
     * The name of the bound read-write bean property for the subject.
     * 
     * @see #getSubjectBean()
     * @see #setSubjectBean(ValueModel)
     */
    public static final String PROPERTYNAME_SUBJECT = "subject";

    /**
     * The name of the bound read-write bean property for the trigger channel.
     * 
     * @see #getTriggerChannel()
     * @see #setTriggerChannel(ValueModel)
     */
    public static final String PROPERTYNAME_TRIGGER_CHANNEL = "triggerChannel";

    // ************************************************************************

    /**
     * Holds the subject that provides the underlying value of type <code>Object</code>.
     */
    private IBindingEntity subjectBean;

    private String propertyName;

    /**
     * Holds the three-state trigger of type <code>Boolean</code>.
     */
    private ValueModel triggerChannel;

    /**
     * Holds the buffered value. This value is ignored if we are not buffering.
     */
    private Object bufferedValue;

    /**
     * Indicates whether a value has been assigned since the last trigger change.
     */
    private boolean valueAssigned;

    /**
     * Holds a PropertyChangeListener that observes trigger changes.
     */
    private final TriggerChangeHandler triggerChangeHandler;

    private final List<IOnCommitAction> onCommitActions = new ArrayList<IOnCommitAction>();

    // Instance Creation ****************************************************

    /**
     * Constructs a BufferedPropertyWrapper on the given entity and propertyName using the given trigger channel.
     * 
     * @param subjectBean
     *            the value model to be buffered
     * @param triggerChannel
     *            the value model that triggers the commit or flush event
     * @throws NullPointerException
     *             if the triggerChannel is {@code null}
     */
    BufferedPropertyWrapper(final IBindingEntity subjectBean, final String propertyName, final ValueModel triggerChannel, final IOnCommitAction... actions) {
        changeSupport = new PropertyChangeSupportEx(this);
        if (subjectBean == null) {
            throw new NullPointerException("The entity must not be null.");
        }
        if (propertyName == null) {
            throw new NullPointerException("The propertyName must not be null.");
        }
        this.propertyName = propertyName;
        triggerChangeHandler = new TriggerChangeHandler();
        setSubjectBean(subjectBean);
        setTriggerChannel(triggerChannel);
        setBuffering(false);
        for (int i = 0; i < actions.length; i++) {
            this.addOnCommitAction(actions[i]);
        }
    }

    @Override
    public void rebindTo(final IBindingEntity entity) {
        if (entity == null) {
            new IllegalArgumentException("the component cannot be reconnected to the Null entity!!").printStackTrace();
        } else {
            unbound();
            setSubjectBean(entity);
            for (final IPropertyConnector propertyConnector : connectors) {
                propertyConnector.addOwnEntitySpecificListeners();
                propertyConnector.updateStates();
            }
        }
    }

    @Override
    public void unbound() {
        for (final IPropertyConnector propertyConnector : connectors) {
            propertyConnector.removeOwnEntitySpecificListeners();
        }
    }

    // Accessing the Subject and Trigger Channel ******************************

    /**
     * Returns the subjectBean, i.e. the underlying {@link IBindingEntity} that provides the unbuffered value.
     * 
     * @return the ValueModel that provides the unbuffered value
     */
    @Override
    public IBindingEntity getSubjectBean() {
        return subjectBean;
    }

    /**
     * overridden from IBindingEntity to get SubjectBeanProperty value or buffered value
     */
    @Override
    public Object get(final String propertyName) {
        if (!propertyName.equals(this.propertyName)) {
            new Exception("propertyName have to be the propertyName incapsulated in This BufferedIBindingEntity!!!!!!!").printStackTrace();
            return null;
        }
        return getValue();
    }

    /**
     * overridden from IBindingEntity to set SubjectBeanProperty value or buffered value
     */
    @Override
    public void set(final String propertyName, final Object value) {
        if (!propertyName.equals(this.propertyName)) {
            new Exception("propertyName have to be the propertyName incapsulated in This BufferedAbstractEntity!!!!!!!").printStackTrace();
            return;
        }
        setValue(value);
    }

    /**
     * Sets a new subject bean, i.e. the model that provides the unbuffered value. Notifies all listeners that the <i>subjectBean</i> property has changed.
     * 
     * @param newSubjectBean
     *            the subject ValueModel to be set
     */
    public void setSubjectBean(final IBindingEntity newSubjectBean) {
        final IBindingEntity oldSubjectBean = getSubjectBean();
        subjectBean = newSubjectBean;
        firePropertyChange(PROPERTYNAME_SUBJECT, oldSubjectBean, newSubjectBean);
    }

    /**
     * Returns the ValueModel that is used to trigger commit and flush events.
     * 
     * @return the ValueModel that is used to trigger commit and flush events
     */
    public ValueModel getTriggerChannel() {
        return triggerChannel;
    }

    /**
     * Sets the ValueModel that triggers the commit and flush events.
     * 
     * @param newTriggerChannel
     *            the ValueModel to be set as trigger channel
     * @throws NullPointerException
     *             if the newTriggerChannel is {@code null}
     */
    public void setTriggerChannel(final ValueModel newTriggerChannel) {
        if (newTriggerChannel == null) {
            throw new NullPointerException("The trigger channel must not be null.");
        }
        final ValueModel oldTriggerChannel = getTriggerChannel();
        if (oldTriggerChannel != null) {
            oldTriggerChannel.removeValueChangeListener(triggerChangeHandler);
        }
        triggerChannel = newTriggerChannel;
        newTriggerChannel.addValueChangeListener(triggerChangeHandler);
        firePropertyChange(PROPERTYNAME_TRIGGER_CHANNEL, oldTriggerChannel, newTriggerChannel);
    }

    // Implementing the ValueModel Interface ********************************

    /**
     * Returns the subjectBean property's value if no value has been set since the last commit or flush, and returns the buffered value otherwise. Attempts to read a value when no
     * subject is set are rejected with a NullPointerException.
     * 
     * @return the buffered value
     * @throws NullPointerException
     *             if no subject is set
     */
    public Object getValue() {
        if (subjectBean == null) {
            throw new NullPointerException("The subject must not be null " + "when reading a value from a BufferedValueModel.");
        }
        return isBuffering() ? bufferedValue : subjectBean.get(propertyName);
    }

    /**
     * Sets a new buffered value and turns this {@link BufferedPropertyWrapper} into the buffering state. The buffered value is not provided to the underlying model until the
     * trigger channel indicates a commit. Attempts to set a value when no subject is set are rejected with a NullPointerException.
     * <p>
     * 
     * The above semantics is easy to understand, however it is tempting to check the new value against the current subject value to avoid that the buffer unnecessary turns into
     * the buffering state. But here's a problem. Let's say the subject value is "first" at buffer creation time, and let's say the subject value has changed in the meantime to
     * "second". Now someone sets the value "second" to this buffer. The subject value and the value to be set are equal. Shall we buffer? Also, this decision would depend on the
     * ability to read the subject. The semantics would depend on the subject' state and capabilities.
     * <p>
     * 
     * It is often sufficient to observe the buffering state when enabling or disabling a commit command button like "OK" or "Apply". And later check the <em>changed</em> state in
     * a PresentationModel. You may want to do better and may want to observe a property like "defersTrueChange" that indicates whether flushing a buffer will actually change the
     * subject. But note that such a state may change with subject value changes, which may be hard to understand for a user.
     * <p>
     * 
     * Consider adding an optimized execution path for the case that this model is already in buffering state. In this case the old buffered value can be used instead of invoking
     * <code>#readBufferedOrSubjectValue()</code>.
     * 
     * @param newBufferedValue
     *            the value to be buffered
     * @throws NullPointerException
     *             if no subject is set
     */
    public void setValue(final Object newBufferedValue) {
        if (subjectBean == null) {
            throw new NullPointerException("The subject must not be null " + "when setting a value to a BufferedValueModel.");
        }
        bufferedValue = newBufferedValue;
        logger.info("Buffered value := " + newBufferedValue);
        setBuffering(true);
    }

    // Releasing PropertyChangeListeners **************************************

    /**
     * Removes the PropertyChangeListeners from the subject and trigger channel.
     * <p>
     * 
     * To avoid memory leaks it is recommended to invoke this method if the subject and trigger channel live much longer than this buffer. Once #release has been invoked the
     * BufferedValueModel instance must not be used any longer.
     * <p>
     * 
     * As an alternative you may use event listener lists in subjects and trigger channels that are based on <code>WeakReference</code>s.
     * 
     * @see java.lang.ref.WeakReference
     */
    public void release() {
        final ValueModel aTriggerChannel = getTriggerChannel();
        if (aTriggerChannel != null) {
            aTriggerChannel.removeValueChangeListener(triggerChangeHandler);
        }
    }

    // Misc *****************************************************************

    /**
     * Returns whether this model buffers a value or not, that is, whether a value has been assigned since the last commit or flush.
     * 
     * @return true if a value has been assigned since the last commit or flush
     */
    public boolean isBuffering() {
        return valueAssigned;
    }

    private void setBuffering(final boolean newValue) {
        final boolean oldValue = isBuffering();
        valueAssigned = newValue;
        firePropertyChange(PROPERTYNAME_BUFFERING, oldValue, newValue);
    }

    @Override
    public final PropertyChangeSupportEx getChangeSupport() {
        return changeSupport;
    }

    /**
     * Registers property change listener.<br>
     * <br>
     * Note : Please, refer also to {@link PropertyChangeOrIncorrectAttemptListener} JavaDocs.
     * 
     * @param propertyName
     * @param listener
     */
    @Override
    public final synchronized void addPropertyChangeListener(final String propertyName, final PropertyChangeListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("PropertyChangeListener cannot be null.");
        }
        //	if (!isObservable(propertyName)) {
        //	    throw new IllegalArgumentException("Cannot register PropertyChangeListener with non-observable property '" + propertyName + "'.");
        //	}
        getChangeSupport().addPropertyChangeListener(propertyName, listener);
    }

    /**
     * Removes property change listener.
     */
    @Override
    public final synchronized void removePropertyChangeListener(final String propertyName, final PropertyChangeListener listener) {
        getChangeSupport().removePropertyChangeListener(propertyName, listener);
    }

    private final void firePropertyChange(final String propertyName, final Object oldValue, final Object newValue) {
        if (getChangeSupport() == null) {
            return;
        }
        getChangeSupport().firePropertyChange(propertyName, oldValue, newValue);
    }

    /**
     * Sets the buffered value as new subject value - if any value has been set. After this commit this {@link BufferedPropertyWrapper} behaves as if no value has been set before.
     * This method is invoked if the trigger has changed to {@code Boolean.TRUE}.
     * <p>
     * 
     * Since the subject's value is assigned <em>after</em> the buffer marker is reset, subject change notifications will be handled. In this case the subject's old value is not
     * this {@link BufferedPropertyWrapper}'s old value; instead the old value reported to listeners of this model is the formerly buffered value.
     * 
     * @throws NullPointerException
     *             if no subject is set
     */
    private void commit() {
        if (isBuffering()) {
            // lock subject bean, even if the setter will not be perfomed (it is more safe)
            subjectBean.lock();

            // IMPORTANT: need to capture the relevant values in the EDT before entering the SwingWorker
            // this is essential in order to have UI control value writes and reads synchronised
            final Object bufferedValue = this.bufferedValue;

            // now create and execute the swing worker
            new SwingWorkerCatcher<Result, Void>() {

                private boolean setterPerformed = false;

                @Override
                protected Result tryToDoInBackground() throws Exception {
                    setBuffering(false);
                    subjectBean.set(propertyName, bufferedValue);
                    setterPerformed = true;
                    return null;

                }

                @Override
                protected void tryToDone() {
                    if (setterPerformed) {
                        for (int i = 0; i < onCommitActions.size(); i++) {
                            if (onCommitActions.get(i) != null) {
                                onCommitActions.get(i).postCommitAction();
                                if (subjectBean.getProperty(propertyName) == null || subjectBean.getProperty(propertyName).isValid()) {
                                    onCommitActions.get(i).postSuccessfulCommitAction();
                                } else {
                                    onCommitActions.get(i).postNotSuccessfulCommitAction();
                                }
                            }
                        }
                    }
                    // need to unlock subjectBean in all cases:
                    // 1. setter not performed - exception throwed
                    // 2. setter not performed - the committing logic didn't invoke setter
                    // 3. setter performed correctly
                    subjectBean.unlock();
                }
            }.execute();
        }
    }

    /**
     * Flushes the buffered value. This method is invoked if the trigger has changed to {@code Boolean.FALSE}. After this flush this {@link BufferedPropertyWrapper} behaves as if
     * no value has been set before.
     * <p>
     * 
     * Check whether we need to use #getValueSafe instead of #getValue.
     * 
     * @throws NullPointerException
     *             if no subject is set
     */
    protected void flush() {
        logger.debug("Inner flush perfomed.");
        final Object oldValue = getValue();
        setBuffering(false);
        final Object newValue = getValue();
        if (subjectBean.getChangeSupport() != null) {
            subjectBean.getChangeSupport().firePropertyChange(propertyName, oldValue, newValue);
        }
    }

    protected String valueString() {
        try {
            final Object value = getValue();
            return value == null ? "null" : value.toString();
        } catch (final Exception e) {
            return "Can't read";
        }
    }

    // Overriding Superclass Behavior *****************************************

    protected String paramString() {
        return "value=" + valueString() + "; buffering" + isBuffering();
    }

    // Event Handling *********************************************************

    /**
     * Listens to changes of the trigger channel.
     */
    private final class TriggerChangeHandler implements PropertyChangeListener {

        /**
         * The trigger has been changed. Commits or flushes the buffered value.
         * 
         * @param evt
         *            the property change event to be handled
         */
        @Override
        public void propertyChange(final PropertyChangeEvent evt) {
            if (Boolean.TRUE.equals(evt.getNewValue())) {
                commit();
            } else if (Boolean.FALSE.equals(evt.getNewValue())) {
                flush();
            }
        }
    }

    @Override
    public String getPropertyName() {
        return propertyName;
    }

    @Override
    public synchronized boolean addOnCommitAction(final IOnCommitAction onCommitAction) {
        return onCommitActions.add(onCommitAction);
    }

    @Override
    public synchronized boolean removeOnCommitAction(final IOnCommitAction onCommitAction) {
        return onCommitActions.remove(onCommitAction);
    }

    @Override
    public List<IOnCommitAction> getOnCommitActions() {
        return Collections.unmodifiableList(onCommitActions);
    }

    @Override
    public MetaProperty getProperty(final String name) {
        throw new RuntimeException("Illegal operation.");
    }

    @Override
    public void lock() {
        throw new RuntimeException("Illegal operation.");
    }

    @Override
    public void unlock() {
        throw new RuntimeException("Illegal operation.");
    }

    @Override
    public Class<?> getPropertyType(final String propertyName) {
        throw new RuntimeException("Illegal operation.");
    }

    @Override
    public void updateToolTip() {
        for (final IPropertyConnector connector : connectors) {
            connector.updateToolTip();
        }
    }
}
