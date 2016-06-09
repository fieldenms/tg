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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.IBindingEntity;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.validation.annotation.ValidationAnnotation;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.swing.components.bind.development.Binder.IPropertyConnector;
import ua.com.fielden.platform.swing.components.bind.development.Binder.IRebindable;
import ua.com.fielden.platform.swing.components.bind.development.ComponentFactory.IOnCommitAction;
import ua.com.fielden.platform.swing.components.bind.development.ComponentFactory.IOnCommitActionable;
import ua.com.fielden.platform.swing.components.smart.autocompleter.development.AutocompleterTextFieldLayer;
import ua.com.fielden.platform.utils.PropertyChangeSupportEx;
import ua.com.fielden.platform.utils.PropertyChangeSupportEx.CheckingStrategy;
import ua.com.fielden.platform.utils.PropertyChangeSupportEx.PropertyChangeOrIncorrectAttemptListener;

import com.jgoodies.binding.value.BufferedValueModel;
import com.jgoodies.binding.value.Trigger;
import com.jgoodies.binding.value.ValueModel;

/**
 * This class represents the BufferedProperty, from which bounded autocompleter can be created. Constructs from IBindingEntity, PropertyName and Trigger. The Trigger - can be used
 * in triggerCommit, triggerFlush methods, to commit/flush changes of the assigned properties. E.g. SaveButtonTrigger can be assigned to SaveButton and need to be assigned to All
 * properties that have to be saved by SaveButtonClick. Also it used for OnFocusLostModel inner creation (used OnFocusLostTrigger)
 *
 * 10.02.2009 YN : Added key type of type {@link String}, because this class cannot be created without {@link KeyType} annotation
 *
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
 * @param <T>
 *
 * @see ValueModel
 * @see ValueModel#getValue()
 * @see ValueModel#setValue(Object)
 */
@SuppressWarnings("unchecked")
@KeyType(String.class)
public final class AutocompleterBufferedPropertyWrapper<T> implements IBindingEntity, IOnCommitActionable, IRebindable {
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
    public static final String PROPERTYNAME_SUBJECT_BEAN = "subjectBean";

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

    //    /**
    //     * Holds a PropertyChangeListener that observes subject value changes.
    //     */
    //    private final ValueChangeHandler valueChangeHandler;

    /**
     * Holds a PropertyChangeListener that observes trigger changes.
     */
    private final TriggerChangeHandler triggerChangeHandler;

    private final AutocompleterTextFieldLayer<T> autocompleter;

    private final List<IOnCommitAction> onCommitActions = new ArrayList<IOnCommitAction>();

    private final boolean stringBinding;

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
    AutocompleterBufferedPropertyWrapper(final IBindingEntity subjectBean, final String propertyName, final ValueModel triggerChannel, final AutocompleterTextFieldLayer<T> autocompleter, final boolean stringBinding, final IOnCommitAction... actions) {
        changeSupport = new PropertyChangeSupportEx(this);
        if (subjectBean == null) {
            throw new NullPointerException("The entity must not be null.");
        }
        if (propertyName == null) {
            throw new NullPointerException("The propertyName must not be null.");
        }
        this.stringBinding = stringBinding;
        this.propertyName = propertyName;
        //	valueChangeHandler = new ValueChangeHandler();
        triggerChangeHandler = new TriggerChangeHandler();
        setSubjectBean(subjectBean);
        setTriggerChannel(triggerChannel);
        setBuffering(false);
        this.autocompleter = autocompleter;
        if (autocompleter == null) {
            throw new NullPointerException("The autocompleter must not be null.");
        }
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
        // TODO maybe need to remove component-specific listeners
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

    @Override
    /*
     * overridden from IBindingEntity to get SubjectBeanProperty value or
     * buffered value
     */
    public Object get(final String propertyName) {
        if (!propertyName.equals(this.propertyName)) {
            logger.error("PropertyName have to be the propertyName incapsulated in this " + getClass().getSimpleName() + ".");
            return null;
        }
        return getValue();
    }

    /**
     * overridden from IBindingEntity to set SubjectBeanProperty value or buffered value
     */
    @Override
    public AutocompleterBufferedPropertyWrapper<T> set(final String propertyName, final Object value) {
        if (!propertyName.equals(this.propertyName)) {
            logger.error("PropertyName have to be the propertyName incapsulated in this " + getClass().getSimpleName() + ".");
            return this;
        }
        setValue(value);
        return this;
    }

    /**
     * Sets a new subject bean, i.e. the model that provides the unbuffered value. Notifies all listeners that the <i>subjectBean</i> property has changed.
     *
     * @param newSubjectBean
     *            the subject ValueModel to be set
     */
    public void setSubjectBean(final IBindingEntity newSubjectBean) throws NullPointerException {
        if (newSubjectBean == null) {
            throw new NullPointerException("the new Subject Bean for BufferedPropertyWrapper cannot be null");
        }
        final IBindingEntity oldSubjectBean = getSubjectBean();
        subjectBean = newSubjectBean;
        firePropertyChange(PROPERTYNAME_SUBJECT_BEAN, oldSubjectBean, newSubjectBean);
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
     *
     * model is already in buffering state. In this case the old buffered value can be used instead of invoking <code>#readBufferedOrSubjectValue()</code>.
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

        final ReadAccessResult oldReadValue = readBufferedOrSubjectValue();
        final Object oldValue = oldReadValue.value;
        bufferedValue = newBufferedValue;
        logger.info("Buffered value := " + newBufferedValue);
        setBuffering(true);
        if (oldReadValue.readable && oldValue == newBufferedValue) {
            return;
        }
    }

    /**
     * Tries to lookup the current buffered or subject value and returns this value plus a marker that indicates whether the read-access succeeded or failed. The latter situation
     * arises in an attempt to read a value from a write-only subject if this {@link BufferedPropertyWrapper} is not buffering and if this model changes its subject.
     *
     * @return the current value plus a boolean that indicates the success or failure
     */
    private ReadAccessResult readBufferedOrSubjectValue() {
        try {
            final Object value = getValue(); // May fail with write-only models
            return new ReadAccessResult(value, true);
        } catch (final Exception e) {
            return new ReadAccessResult(null, false);
        }
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

    /**
     *
     * Gets the actualValues from autocompleter and sets as subjectValue (when autocompleter singleValued - the only one value, otherwise - the List) Also notifies appropriate
     * listeners.
     *
     * Sets the buffered value as new subject value - if any value has been set. After this commit this {@link BufferedPropertyWrapper} behaves as if no value has been set before.
     * This method is invoked if the trigger has changed to {@code Boolean.TRUE}.
     * <p>
     *
     * Since the subject's value is assigned <em>after</em> the buffer marker is reset, subject change notifications will be handled. In this case the subject's old value is not
     * this {@link BufferedPropertyWrapper}'s old value; instead the old value reported to listeners of this model is the formerly buffered value.
     *
     * The commit logic has been enhanced with indication of the start and finish for the property validation process (refer {@link Property} for more details). Validation is not
     * considered finished if the validation result is not successful.
     *
     * @throws Exception
     *             - throws when single valued autocompleter returns multiple values!
     *
     * @throws NullPointerException
     *             if no subject is set
     */
    private void commit() {
        if (isBuffering()) {
            // lock subject bean, even if the setter will not be performed (it is more safe)
            subjectBean.lock();

            // IMPORTANT: need to capture the relevant values in the EDT before entering the SwingWorker
            // this is essential in order to have UI control value writes and reads synchronised
            final String controlValue = autocompleter.getView().getText();
            final Object bufferedValue = this.bufferedValue;

            // now create and execute the swing worker
            new SwingWorkerCatcher<String, Void>() {
                private boolean setterPerformed = false;

                @Override
                protected String tryToDoInBackground() {
                    setBuffering(false);
                    logger.debug("Buffered value [" + bufferedValue + "] commits.");
                    if (!bufferedValue.equals(controlValue)) {
                        logger.error("Buffered value == [" + bufferedValue + "] is not identical to autocompleter.getView().getText() == [" + controlValue + "]");
                    }

                    // IMPORTANT: bufferedValue is of type String!!!!
                    // that is why need to get actual values from autocompleter.
                    final List values = stringBinding && autocompleter.isMulti() ? new ArrayList<T>() : autocompleter.values((String) bufferedValue);
                    if (autocompleter.isMulti()) { // multi-valued autocompleter
                        final Object valueToSet = stringBinding ? (AutocompleterConnector.constructStringListFromStringBufferedValue((String) bufferedValue, autocompleter.getAutocompleter().getValueSeparator()))
                                : values;
                        subjectBean.set(propertyName, valueToSet);
                        setterPerformed = true;
                    } else { // otherwise single-valued
                        if (StringUtils.isEmpty(controlValue)) { // if text is empty then there should no value -- hence null
                            subjectBean.set(propertyName, null);
                            setterPerformed = true;
                        } else if (isEmpty(values, bufferedValue, stringBinding)) { // otherwise, if there is some text, but autocompleter failed to find a corresponding entity then this should be reported as non-existing entity case
                            final String errorMsg = TitlesDescsGetter.processEntityExistsErrorMsg(propertyName, bufferedValue, subjectBean.getProperty(propertyName).getEntity().getType());

                            final Result result = new Result(subjectBean, new Exception(!StringUtils.isEmpty(errorMsg) ? errorMsg : "Could not find a matching value/values for "
                                    + bufferedValue));
                            // ------ pre-setter logic validation
                            logger.info("Could not find a matching value/values for " + bufferedValue);
                            if (!stringBinding) {
                                // if autocompleter binds to IBindingEntity : update ENTITY_EXISTS validation result
                                // (it ensures that ENTITY_EXISTS should be specified as validation annotation in the property setter)
                                // but the same can be simply performed with  fireImaginaryValidationResult(...) method!!!
                                if (subjectBean.getProperty(propertyName) != null) {
                                    subjectBean.getProperty(propertyName).setRequiredValidationResult(null);
                                    subjectBean.getProperty(propertyName).setEntityExistsValidationResult(result);
                                }
                            } else {
                                //subjectBean.getMetaProperty(propertyName).fireImaginaryValidationResult(result);
                                if (subjectBean.getProperty(propertyName) != null
                                        && subjectBean.getProperty(propertyName).getValidators().containsKey(ValidationAnnotation.ENTITY_EXISTS)) { // if String property annotated by EntityExists,
                                    // then should report "early" validation result error
                                    subjectBean.getProperty(propertyName).setRequiredValidationResult(null);
                                    subjectBean.getProperty(propertyName).setEntityExistsValidationResult(result);
                                } else {
                                    // if String property is not annotated by EntityExists -> the buffered value (String) is legal and have to be set!
                                    subjectBean.set(propertyName, bufferedValue);
                                    setterPerformed = true;
                                }
                            }
                            return null;
                        } else { // everything is cool and we can set a single value that was returned by the autocompleter
                            subjectBean.set(propertyName, stringBinding ? bufferedValue : ((values.size() > 1) ? findValue((String) bufferedValue, values) : values.get(0)));
                            setterPerformed = true;
                        }
                    } // end of single-valued autocompleter processing
                    return null;
                }

                /**
                 * This method defines what "empty" means for a list of resultant from autocompleter matcher values.
                 *
                 * @param values
                 * @param bufferedValue
                 * @param stringBinding
                 * @return
                 */
                private boolean isEmpty(final List values, final Object bufferedValue, final boolean stringBinding) {
                    if (stringBinding) {
                        for (final Object value : values) {
                            if (bufferedValue.equals(value)) {
                                return false;
                            }
                        }
                    } else {
                        for (final Object v : values) {
                            final AbstractEntity value = (AbstractEntity) v;
                            Object that;
                            if (value.getKeyType() == DynamicEntityKey.class) {
                                that = value.getKey().toString();
                            } else {
                                that = value.getKey();
                            }

                            // well... the internal logic is really all about strings, so for better Integer support make it a String...
                            if (that instanceof Integer) {
                                that = value.getKey().toString();
                            }

                            //			    if (bufferedValue.equals(that)) {
                            //				return false;
                            //			    }
                            // TODO take into account case sensitivity from upper logic
                            if ((bufferedValue instanceof String) && (that instanceof String) && ((String) bufferedValue).equalsIgnoreCase((String) that)) {
                                return false;
                            } else if (bufferedValue.equals(that)) {
                                return false;
                            }

                        }
                    }
                    return true;
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

    private static <N extends AbstractEntity> N findValue(final String bufferedValue, final List<N> values) {
        for (final N value : values) {
            if (value.getKey().equals(bufferedValue)) {
                return value;
            }
        }
        return null;
    }

    /**
     * Flushes the buffered value. This method is invoked if the trigger has changed to {@code Boolean.FALSE}. After this flush this {@link BufferedPropertyWrapper} behaves as if
     * no value has been set before.
     * <p>
     *
     *
     *
     * @throws NullPointerException
     *             if no subject is set
     */
    protected void flush() {
        logger.debug("Inner flush perfomed.");
        final boolean wasBuffering = isBuffering();
        final Object oldValue = subjectBean.get(propertyName);
        setBuffering(false);
        final Object newValue = getValue();
        if (subjectBean.getChangeSupport() != null) {
            subjectBean.getChangeSupport().firePropertyChange(propertyName, oldValue, newValue, wasBuffering ? CheckingStrategy.CHECK_NOTHING : CheckingStrategy.CHECK_EQUALITY, false);
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

    // Helper Class ***********************************************************

    /**
     * Describes the result of a subject value read-access plus a marker that indicates if the value could be read or not. The latter is used in <code>#setValue</code> to suppress
     * some unnecessary change notifications in case the value could be read successfully.
     *
     * @see BufferedValueModel#setValue(Object)
     */
    private static final class ReadAccessResult {

        final Object value;
        final boolean readable;

        private ReadAccessResult(final Object value, final boolean readable) {
            this.value = value;
            this.readable = readable;
        }

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

    public boolean isStringBinding() {
        return stringBinding;
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
