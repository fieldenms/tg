package ua.com.fielden.platform.entity.meta;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.Mutator;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.validation.IBeforeChangeEventHandler;
import ua.com.fielden.platform.entity.validation.NotNullValidator;
import ua.com.fielden.platform.entity.validation.StubValidator;
import ua.com.fielden.platform.entity.validation.annotation.ValidationAnnotation;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.Reflector;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;

/**
 * Implements the concept of a meta-property.
 * <p>
 * Currently it provides validation support and validation change listeners, which can be specified whenever it is necessary to handle validation state changes.
 * <p>
 * It is planned to support other meta-information such as accessibility, label etc. This information can be user dependent and retrieved from a database.
 * <p>
 * <b>Date: 2008-10-xx</b><br>
 * Provided support for meta property <code>editable</code>.<br>
 * Provided support for keeping the track of property value changes, which includes support for <code>prevValue</code>, <code>originalValue</code> and <code>valueChangeCount</code>.
 * <p>
 * <b>Date: 2008-12-11</b><br>
 * Implemented support for {@link Comparable}.<br>
 * Provided flags <code>active</code> and <code>key</code>.
 * <p>
 * <b>Date: 2008-12-22</b><br>
 * Implemented support for recording last invalid value.<br>
 * <p>
 * <b>Date: 2010-03-18</b><br>
 * Implemented support for restoring to original value with validation error cancellation.<br>
 *
 *
 * @author TG Team
 *
 */
public final class MetaProperty implements Comparable<MetaProperty> {
    private final AbstractEntity<?> entity;
    private final String name;
    private Class<?> type;
    private final Class<?> propertyAnnotationType;
    private final Map<ValidationAnnotation, Map<IBeforeChangeEventHandler, Result>> validators;
    private final IMetaPropertyDefiner definer;
    private final boolean key;
    private final boolean collectional;
    /**
     * This property indicates whether a corresponding property was modified. This is similar to <code>dirty</code> property at the entity level.
     */
    private boolean dirty;

    public static final String ORIGINAL_VALUE_NOT_INIT_COLL = "This is the original value for not-initialized collection.";

    ///////////////////////////////////////////////////////
    /// Holds an original value of the property.
    ///////////////////////////////////////////////////////
    private Object originalValue;
    private Object prevValue;
    private Object lastInvalidValue;
    private int valueChangeCount = 0;
    /**
     * Indicates whether this property has an assigned value. This flag is requited due to the fact that the value of null could be assigned making it impossible to identify the
     * fact of value assignment.
     */
    private boolean assigned = false;
    ///////////////////////////////////////////////////////

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // The following properties are more related to UI controls rather than to actual property value modification. //
    // Some external to meta-property logic may define whether the value of <code>editable</code>.                 //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private boolean editable = true;
    private boolean visible = true;
    private boolean required = false;
    private String title; // TODO to be used for holding property title (can be read from db or other resources) as applicable to UI controls
    private String desc; // TODO to be used for holding extended description of the property's purpose (can be read from db or other resources), can be used as a default for tool tips on UI controls representing property value
    private final boolean calculated;
    private final boolean upperCase;
    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    private final String[] dependentPropertyNames;

    public static final String EDITABLE_PROPERTY_NAME = "editable";
    public static final String REQUIRED_PROPERTY_NAME = "required";
    public static final String VALIDATION_RESULTS_PROPERTY_NAME = "validationResults";

    private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

    private final Logger logger = Logger.getLogger(this.getClass());

    private boolean enforceMutator = false;

    /**
     * Property supports specification of the precise type. For example, property <code>key</code> in entity classes is recognised by reflection API as Comparable. Therefore, it
     * might be desirable to specify type more accurately.
     *
     * @param field
     * @param type
     * @param validators
     */
    public MetaProperty(final AbstractEntity<?> entity, final Field field, final Class<?> type, //
    final boolean isKey, final boolean isCollectional, final Class<?> propertyAnnotationType, final boolean calculated, final boolean upperCase,//
    final Map<ValidationAnnotation, Map<IBeforeChangeEventHandler, Result>> validators, final IMetaPropertyDefiner definer, final String[] dependentPropertyNames) {
	this.entity = entity;
	this.name = field.getName();
	this.type = type;
	this.key = isKey;
	this.validators = validators;
	this.definer = definer;
	this.collectional = isCollectional;
	this.propertyAnnotationType = propertyAnnotationType;
	this.calculated = calculated;
	this.upperCase = upperCase;
	this.dependentPropertyNames = dependentPropertyNames != null ? Arrays.copyOf(dependentPropertyNames, dependentPropertyNames.length) : null;
    }

    /**
     * Perform sequential validation.
     *
     * Stops validation at the first violation. This is preferred since subsequent validators may depend on success of preceding ones.
     *
     * Please note that collectional properties may have up to three mutators having different annotations. Thus, a special logic is used to ensure that only validators associated
     * with the mutator being processed are applied.
     *
     * @param property
     * @param value
     * @param entity
     * @param applicableValidationAnnotations
     * @param mutator
     *            - an actual method modifying the value - either {@link Mutator#SETTER}, {@link Mutator#INCREMENTOR} or {@link Mutator#DECREMENTOR}
     * @return
     */
    public synchronized final Result validate(final Object newValue, final Object oldValue, final Set<Annotation> applicableValidationAnnotations, final boolean ignoreRequiredness) {
	setLastInvalidValue(null);
	if (!ignoreRequiredness && isRequired() && NotNullValidator.isNull(newValue, oldValue)) {
	    final Map<IBeforeChangeEventHandler, Result> requiredHandler = getValidators().get(ValidationAnnotation.REQUIRED);
	    if (requiredHandler == null || requiredHandler.size() > 1) {
		throw new IllegalArgumentException("There are no or there is more than one REQUIRED validation handler for required property!");
	    }
	    final Result result = new Result(getEntity(), new IllegalArgumentException("Required property " + (StringUtils.isEmpty(getTitle()) ? name : getTitle())
		    + " is not specified for entity " + TitlesDescsGetter.getEntityTitleAndDesc(getEntity().getType()).getKey()));
	    setValidationResultNoSynch(ValidationAnnotation.REQUIRED, requiredHandler.keySet().iterator().next(), result);
	    return result;
	} else {
	    // refresh REQUIRED validation result if REQUIRED validation annotation pair exists
	    final Map<IBeforeChangeEventHandler, Result> requiredHandler = getValidators().get(ValidationAnnotation.REQUIRED);
	    if (requiredHandler != null && requiredHandler.size() == 1) {
		setValidationResultNoSynch(ValidationAnnotation.REQUIRED, requiredHandler.keySet().iterator().next(), new Result(getEntity(), "Requiredness updated by successful result."));
	    }
	    // process all registered validators (that have its own annotations)
	    return processValidators(newValue, oldValue, applicableValidationAnnotations);
	}
    }

    /**
     * Revalidates this property using {@link #getLastAttemptValue()} value as the input for the property. Revalidation occurs only if this property has an assigned value (null
     * could also be an assigned value).
     *
     * @param ignoreRequiredness
     *            when true then isRequired value is ignored during revalidation, this is currently used for re-validating dependent properties where there is no need to mark
     *            properties as invalid only because they are empty.
     * @return revalidation result
     */
    public synchronized final Result revalidate(final boolean ignoreRequiredness) {
	try {
	    // revalidation is required only is there is an assigned value
	    if (assigned) {
		final Method mutator = Reflector.obtainPropertySetter(getEntity().getType(), getName());
		return validate(getLastAttemptValue(), null, AnnotationReflector.getValidationAnnotations(mutator), ignoreRequiredness);
	    }
	    return Result.successful(this);
	} catch (final NoSuchMethodException e) {
	    throw new IllegalStateException("Cannot obtain setter for property " + getEntity().getClass().getName() + "." + getName(), e);
	}
    }

    /**
     * Processes all registered validators (that have its own annotations) by iterating over validators associated with corresponding validation annotations (va).
     *
     * @param newValue
     * @param oldValue
     * @param applicableValidationAnnotations
     * @param mutatorType
     * @return
     */
    private Result processValidators(final Object newValue, final Object oldValue, final Set<Annotation> applicableValidationAnnotations) {
	// iterate over registered validations
	for (final ValidationAnnotation va : validators.keySet()) {
	    final Set<Entry<IBeforeChangeEventHandler, Result>> pairs = validators.get(va).entrySet();
	    for (final Entry<IBeforeChangeEventHandler, Result> pair : pairs) {
		// if validator exists ...and it should... then validated and set validation result
		final IBeforeChangeEventHandler handler = pair.getKey();
		if (handler != null && isValidatorApplicable(applicableValidationAnnotations, va.getType())) {
		    final Result result = pair.getKey().handle(this, newValue, oldValue, applicableValidationAnnotations);
		    setValidationResultNoSynch(va, handler, result); // important to call setValidationResult as it fires property change event listeners
		    if (!result.isSuccessful()) {
			// 1. isCollectional() && newValue instance of Collection : previously the size of "newValue" collection was set as LastInvalidValue, but now,
			//    if we need to update bounded component by the lastInvalidValue then we set it as a collectional value
			// 2. if the property is not collectional then simply set LastInvalidValue as newValue
			setLastInvalidValue(newValue);
			return result;
		    }
		} else {
		    pair.setValue(null);
		}
	    }
	}
	return new Result(this, "Validated successfully.");
    }

    /**
     * Checks whether annotation identified by parameter <code>key</code> is amongst applicable validation annotations.
     *
     * @param applicableValidationAnnotations
     * @param validationAnnotationEnumValue
     * @return
     */
    private boolean isValidatorApplicable(final Set<Annotation> applicableValidationAnnotations, final Class<? extends Annotation> validationAnnotationType) {
	for (final Annotation annotation : applicableValidationAnnotations) {
	    if (annotation.annotationType() == validationAnnotationType) {
		return true;
	    }
	}
	return false;
    }

    public final Map<ValidationAnnotation, Map<IBeforeChangeEventHandler, Result>> getValidators() {
	return validators;
    }

    public final String getName() {
	return name;
    }

    /**
     * Indicates the type of the entity property represented by this meta-property.
     *
     * @return
     */
    public final Class<?> getType() {
	return type;
    }

    /**
     * This setter was introduces specifically to set property type in cases where it is not possible to determine during the creation of meta-property. The specific case is the
     * <code>key</code> property in {@link AbstractEntity}, where the type can be determined only when the actual value is assigned.
     *
     * This setter should be used extremely judiciously.
     *
     * @param type
     */
    public final void setType(final Class<?> type) {
	this.type = type;
    }

    @Override
    public final String toString() {
	return "Property for field " + getName() + " with validators " + validators;
    }

    /**
     * Sets the result for validator with index.
     *
     * @param key
     *            -- annotation representing validator
     * @param validationResult
     */
    private void setValidationResultNoSynch(final ValidationAnnotation key, final IBeforeChangeEventHandler handler, final Result validationResult) {
	// fire validationResults change event!!
	if (validators.get(key) != null) {
	    final Map<IBeforeChangeEventHandler, Result> annotationHandlers = validators.get(key);
	    final Result oldValue = annotationHandlers.get(handler);// getValue();
	    annotationHandlers.put(handler, validationResult);
	    final Result firstFailure = getFirstFailure();
	    if (firstFailure != null) {
		changeSupport.firePropertyChange(VALIDATION_RESULTS_PROPERTY_NAME, oldValue, firstFailure);
	    } else {
		changeSupport.firePropertyChange(VALIDATION_RESULTS_PROPERTY_NAME, oldValue, validationResult);
	    }
	}
    }

    /**
     * Same as {@link #setValidationResultNoSynch(Annotation, IBeforeChangeEventHandler, Result)}, but with synchronization block.
     *
     * @param key
     * @param handler
     * @param validationResult
     */
    public synchronized final void setValidationResult(final ValidationAnnotation key, final IBeforeChangeEventHandler handler, final Result validationResult) {
	setValidationResultNoSynch(key, handler, validationResult);
    }

    /**
     * Sets validation result specifically for {@link ValidationAnnotation.REQUIRED};
     * @param validationResult
     */
    public synchronized final void setRequiredValidationResult(final Result validationResult) {
	setValidationResultForFirtsValidator(validationResult, ValidationAnnotation.REQUIRED);
    }


    /**
     * Sets validation result specifically for {@link ValidationAnnotation.ENTITY_EXISTS};
     * @param validationResult
     */
    public synchronized final void setEntityExistsValidationResult(final Result validationResult) {
	setValidationResultForFirtsValidator(validationResult, ValidationAnnotation.ENTITY_EXISTS);
    }

    /**
     * Sets validation result specifically for {@link ValidationAnnotation.REQUIRED};
     * @param validationResult
     */
    public synchronized final void setDomainValidationResult(final Result validationResult) {
	setValidationResultForFirtsValidator(validationResult, ValidationAnnotation.DOMAIN);
    }

    /**
     * Sets validation result for the first of the annotation handlers designated by the validation annotation value.
     *
     * @param validationResult
     * @param annotationHandlers
     */
    private void setValidationResultForFirtsValidator(final Result validationResult, final ValidationAnnotation va) {
	Map<IBeforeChangeEventHandler, Result> annotationHandlers = validators.get(va);

	if (annotationHandlers == null) {
	    annotationHandlers = new HashMap<IBeforeChangeEventHandler, Result>();
	    annotationHandlers.put(StubValidator.singleton, null);
	    validators.put(va, annotationHandlers);
	}

	final IBeforeChangeEventHandler handler = annotationHandlers.keySet().iterator().next();
	final Result oldValue = annotationHandlers.get(handler);
	annotationHandlers.put(handler, validationResult);
	final Result firstFailure = getFirstFailure();
	if (firstFailure != null) {
	    changeSupport.firePropertyChange(VALIDATION_RESULTS_PROPERTY_NAME, oldValue, firstFailure);
	} else {
	    changeSupport.firePropertyChange(VALIDATION_RESULTS_PROPERTY_NAME, oldValue, validationResult);
	}
    }

    /**
     * Fires (if result is not null) the changing of the "validationResults" property. It can be used as a trick, not to set directly validationResult() to unknown
     * ValidationAnnotation, but only fire the changing result! It should not be used with Null ValidationResults
     *
     * @param validationResult
     */
    private void fireImaginaryValidationResultNoSynch(final Result validationResult) {
	System.out.println("fireImaginaryValidationResultNoSynch : successful == " + validationResult.isSuccessful());
	changeSupport.firePropertyChange(VALIDATION_RESULTS_PROPERTY_NAME, null, validationResult);
    }

    /**
     * Same as {@link #fireImaginaryValidationResultNoSynch(Result)}, but with synchronization block.
     *
     * @param key
     * @param validationResult
     */
    public synchronized final void fireImaginaryValidationResult(final Result validationResult) {
	if (validationResult == null) {
	    // FIXME: No actual excepting throwing is actually happening. This most likely needs to be fixed.
	    new IllegalArgumentException("Cannot fire imaginary validation result when this result is NULL. ").printStackTrace();
	}
	fireImaginaryValidationResultNoSynch(validationResult);
    }

    /**
     * Returns the last result of the first validator associated with {@link ValidationAnnotation} value in a synchronised manner if all validators for this annotation succeeded,
     * or the last result of the first failed validator. Most validation annotations are associated with a single validator. But some, such as
     * {@link ValidationAnnotation#BEFORE_CHANGE} may have more than one validator associated with it.
     *
     * @param va -- validation annotation.
     * @return
     */
    public synchronized final Result getValidationResult(final ValidationAnnotation va) {
	final Result failure = getFirstFailureFor(va);
	return failure != null ? failure : validators.get(va).values().iterator().next();
    }

    /**
     * Returns false if there is at least one unsuccessful result. Evaluation of the validation results happens in a synchronised manner.
     *
     * @return
     */
    public synchronized final boolean isValid() {
	final Result failure = getFirstFailure();
	return failure == null;
    }

    public synchronized final boolean hasWarnings() {
	final Result failure = getFirstWarning();
	return failure  != null;
    }

    /**
     * Returns the first warning associated with property validators.
     *
     * @return
     */
    public synchronized final Result getFirstWarning() {
	for (final ValidationAnnotation va : validators.keySet()) {
	    final Map<IBeforeChangeEventHandler, Result> annotationHandlers = validators.get(va);
	    for (final Result result : annotationHandlers.values()) {
		if (result != null && result.isWarning()) {
		    return result;
		}
	    }
	}
	return null;
    }

    /**
     * This method invokes {@link #isValid()} and if its result is <code>true</code> (i.e. valid) then
     * additional check kicks in to ensure requiredness validation.
     *
     * @return
     */
    public synchronized final boolean isValidWithRequiredCheck() {
	final boolean result = isValid();
	if (result) {
	    // if valid check whether it's requiredness sound
	    final Object value = ((AbstractEntity<?>) getEntity()).get(getName());
	    if (isRequired() && (value == null || isEmpty(value))) {
		if (!getValidators().containsKey(ValidationAnnotation.REQUIRED)) {
		    throw new IllegalArgumentException("There are no REQUIRED validation annotation pair for required property!");
		}
		setValidationResultNoSynch(ValidationAnnotation.REQUIRED, StubValidator.singleton, new Result(getEntity(), new IllegalArgumentException("Required property "
			+ (StringUtils.isEmpty(getTitle()) ? name : getTitle()) + " is not specified for entity "
			+ TitlesDescsGetter.getEntityTitleAndDesc(getEntity().getType()).getKey())));
		return false;
	    }
	}
	return result;
    }

    /**
     * A convenient method, which ensures that only string values are tested for empty when required. This prevents accidental and redundant lazy loading when invoking
     * values.toString() on entity instances.
     *
     * @param value
     * @return
     */
    private boolean isEmpty(final Object value) {
	return value instanceof String ? StringUtils.isEmpty(value.toString()) : false;
    }

    /**
     * Return the first failed validation result. If there is no failure then returns null.
     *
     * @return
     */
    public synchronized final Result getFirstFailure() {
	for (final ValidationAnnotation va : validators.keySet()) {
	    final Map<IBeforeChangeEventHandler, Result> annotationHandlers = validators.get(va);
	    for (final Result result : annotationHandlers.values()) {
		if (result != null && !result.isSuccessful()) {
		    return result;
		}
	    }
	}
	return null;
    }

    /**
     * Returns the first failure associated with <code>annotation</code> value.
     *
     * @param annotation
     * @return
     */
    private final Result getFirstFailureFor(final ValidationAnnotation annotation) {
	final Map<IBeforeChangeEventHandler, Result> annotationHandlers = validators.get(annotation);
	for (final Result result : annotationHandlers.values()) {
	    if (result != null && !result.isSuccessful()) {
		return result;
	    }
	}
	return null;
    }

    /**
     * Returns the number of property validators. Can be zero.
     *
     * @return
     */
    public final int numberOfValidators() {
	return validators.size();
    }

    /**
     * Registers property change listener to validationResults. This listener fires when setValidationResult(..) method invokes.
     *
     * @param propertyName
     * @param listener
     */
    public final synchronized void addValidationResultsChangeListener(final PropertyChangeListener listener) {
	if (listener == null) {
	    throw new IllegalArgumentException("PropertyChangeListener cannot be null.");
	}
	changeSupport.addPropertyChangeListener(VALIDATION_RESULTS_PROPERTY_NAME, listener);
    }

    /**
     * Removes validationResults change listener.
     */
    public synchronized final void removeValidationResultsChangeListener(final PropertyChangeListener listener) {
	changeSupport.removePropertyChangeListener(VALIDATION_RESULTS_PROPERTY_NAME, listener);
    }

    /**
     * Registers property change listener for property <code>editable</code>.
     *
     * @param propertyName
     * @param listener
     */
    public final synchronized void addEditableChangeListener(final PropertyChangeListener listener) {
	if (listener == null) {
	    throw new IllegalArgumentException("PropertyChangeListener cannot be null.");
	}
	changeSupport.addPropertyChangeListener(EDITABLE_PROPERTY_NAME, listener);
    }

    /**
     * Removes change listener for property <code>editable</code>.
     */
    public final synchronized void removeEditableChangeListener(final PropertyChangeListener listener) {
	changeSupport.removePropertyChangeListener(EDITABLE_PROPERTY_NAME, listener);
    }

    /**
     * Registers property change listener for property <code>required</code>.
     *
     * @param propertyName
     * @param listener
     */
    public final synchronized void addRequiredChangeListener(final PropertyChangeListener listener) {
	if (listener == null) {
	    throw new IllegalArgumentException("PropertyChangeListener cannot be null.");
	}
	changeSupport.addPropertyChangeListener(REQUIRED_PROPERTY_NAME, listener);
    }

    /**
     * Removes change listener for property <code>required</code>.
     */
    public final synchronized void removeRequiredChangeListener(final PropertyChangeListener listener) {
	changeSupport.removePropertyChangeListener(REQUIRED_PROPERTY_NAME, listener);
    }

    public final PropertyChangeSupport getChangeSupport() {
	return changeSupport;
    }

    public final Object getOriginalValue() {
	return originalValue;
    }

    public final void setCollectionOriginalValue(final Number size) {
	if (isCollectional()) {
	    this.originalValue = size;
	}
    }

    /**
     * Sets the original value.
     *
     * <p>
     * VERY IMPORTANT : the method should not cause proxy initialisation!!!
     *
     * @param value
     */
    public final MetaProperty setOriginalValue(final Object value) {
	if (value != null) {
	    if (isCollectional() && value instanceof Collection) {
		// possibly at this stage the "value" is the Hibernate proxy.
		originalValue = MetaProperty.ORIGINAL_VALUE_NOT_INIT_COLL;
	    } else { // The single property (proxied or not!!!)
		originalValue = value;
	    }
	} else { // value == null;
	    originalValue = isCollectional() ? 0 : null;
	}
	// when original property value is set then the previous value should be the same
	// the previous value setter is not used deliberately since it has some logic not needed here
	prevValue = originalValue;
	// reset value change counter
	resetValueChageCount();
	//
	assigned = true;
	return this;
    }

    public final int getValueChangeCount() {
	return valueChangeCount;
    }

    private void incValueChangeCount() {
	valueChangeCount++;
    }

    private void resetValueChageCount() {
	valueChangeCount = 0;
    }

    public final Object getPrevValue() {
	return prevValue;
    }

    /**
     * Returns the current (last successful) value of the property.
     *
     * @return
     */
    public final Object getValue() {
	return entity.get(name);
    }

    /**
     * Updates the previous value for the entity property. Increments the update counter and check if the original value should be updated as well.
     *
     * Please note that for collectional properties their size is used as previous and original values.
     *
     * @param prevValue
     * @return
     */
    public final MetaProperty setPrevValue(final Object prevValue) {
	incValueChangeCount();
	// just in case cater for correct processing of collection properties
	if (isCollectional() && prevValue instanceof Collection) {
	    this.prevValue = ((Collection<?>) prevValue).size();
	} else if (isCollectional() && prevValue == null) { // very unlikely, but let's be defensive
	    this.prevValue = 0;
	} else {
	    this.prevValue = prevValue;
	}
	return this;
    }

    /**
     * Checks if the current value is changed from the original one.
     *
     * @return
     */
    public final boolean isChangedFromOriginal() {
	try {
	    final Method getter = Reflector.obtainPropertyAccessor(entity.getClass(), getName());
	    final Object currValue = isCollectional() ? ((Collection<?>) getter.invoke(entity)).size() : getter.invoke(entity);
	    if (getOriginalValue() == null) {
		return currValue != null;
	    } else {
		return currValue == null || !currValue.equals(getOriginalValue());
	    }
	} catch (final Exception e) {
	    // TODO change to logging
	}
	return false;
    }

    /**
     * Checks if the current value is changed from the previous one
     *
     * @return
     */
    public final boolean isChangedFromPrevious() {
	try {
	    final Method getter = Reflector.obtainPropertyAccessor(entity.getClass(), getName());
	    final Object currValue = isCollectional() ? ((Collection<?>) getter.invoke(entity)).size() : getter.invoke(entity);
	    if (getPrevValue() == null) {
		return currValue != null;
	    } else {
		return currValue == null || !currValue.equals(getPrevValue());
	    }
	} catch (final Exception e) {
	    // TODO change to logging
	}
	return false;
    }

    public final boolean isEditable() {
	return editable;
    }

    public final void setEditable(final boolean editable) {
	final boolean oldValue = this.editable;
	this.editable = editable;
	changeSupport.firePropertyChange(EDITABLE_PROPERTY_NAME, oldValue, editable);
    }

    /**
     * Invokes {@link IMetaPropertyDefiner#define(Object)} if it has been provided.
     *
     * @param entityPropertyValue
     * @return
     */
    public final MetaProperty define(final Object entityPropertyValue) {
	if (definer != null) {
	    definer.define(this, entityPropertyValue);
	}
	return this;
    }

    @SuppressWarnings("rawtypes")
    public final AbstractEntity getEntity() {
	return entity;
    }

    /**
     * Returns true if MetaProperty represents a collectional property.
     *
     * @return
     */
    public final boolean isCollectional() {
	return collectional;
    }

    /**
     * Returns a type provided as part of the annotation {@link IsProperty} when defining property. It is provided, for example, in cases where meta-property represents a
     * collection property -- the provided type indicates the type collection elements.
     *
     * @return
     */
    public final Class<?> getPropertyAnnotationType() {
	return propertyAnnotationType;
    }

    /**
     * Meta-property comparison gives preference to properties, which represent a key member. In case where both meta-properties are key members or both are non-key members the
     * property names are used for comparison.
     *
     * Comparison between key and non-key properties happens in reverse order to ensure that key properties are at the top.
     */
    @Override
    public final int compareTo(final MetaProperty mp) {
	if (isKey() && mp.isKey() || !isKey() && !mp.isKey()) {
	    return getName().compareTo(mp.getName());
	}
	return isKey() ? -1 : 1;
    }

    public final boolean isKey() {
	return key;
    }

    public final boolean isVisible() {
	return visible;
    }

    public final void setVisible(final boolean visible) {
	this.visible = visible;
    }

    public final Object getLastInvalidValue() {
	return lastInvalidValue;
    }

    public final void setLastInvalidValue(final Object lastInvalidValue) {
	this.lastInvalidValue = lastInvalidValue;
    }

    /**
     * A convenient method for determining whether there are validators associated a the corresponding property.
     *
     * @return
     */
    public final boolean hasValidators() {
	return getValidators().size() > 0;
    }

    /**
     * Convenient method that returns either property value (if property validation passed successfully) or {@link #getLastInvalidValue()} (if property validation idn't pass)
     *
     * @return
     */
    public final Object getLastAttemptValue() {
	return isValid() ? getEntity().get(getName()) : getLastInvalidValue();
    }

    public final String getTitle() {
	return title;
    }

    public final void setTitle(final String title) {
	this.title = title;
    }

    public final String getDesc() {
	return desc;
    }

    public final void setDesc(final String desc) {
	this.desc = desc;
    }

    public final boolean isRequired() {
	return required;
    }

    /**
     * This setter change the 'required' state for metaProperty. Also it puts RequiredValidator to the list of validators if it does not exist. And if 'required' became false -> it
     * clears REQUIRED validation result by successful result.
     *
     * @param required
     */
    public final void setRequired(final boolean required) {
	final boolean oldRequired = this.required;
	this.required = required;

	// if requirement changed from false to true, and REQUIRED validator does not exist in the list of validators -> then put REQUIRED validator to the list of validators
	if (required && !oldRequired && !containsRequiredValidator()) {
	    putRequiredValidator();
	}
	// if requirement changed from true to false, then update REQUIRED validation result to be successful
	if (!required && oldRequired) {
	    if (containsRequiredValidator()) {
		setValidationResultNoSynch(ValidationAnnotation.REQUIRED, StubValidator.singleton, new Result(this.getEntity(), "'Required' became false. The validation result cleared."));
	    } else {
		throw new IllegalStateException("The metaProperty was required but RequiredValidator didn't exist.");
	    }
	}
	changeSupport.firePropertyChange(REQUIRED_PROPERTY_NAME, oldRequired, required);
    }

    public final void resetState() {
	setOriginalValue(entity.get(name));
	setDirty(false);
    }

    public final void resetValues() {
	setOriginalValue(entity.get(name));
    }

    public final boolean isCalculated() {
	return calculated;
    }

    /**
     * Checks if REQUIRED validator were ever put to the list of validators.
     *
     * @return
     */
    public final boolean containsRequiredValidator() {
	return getValidators().containsKey(ValidationAnnotation.REQUIRED);
    }

    /**
     * Checks if DYNAMIC validator were ever put to the list of validators.
     *
     * @return
     */
    public final boolean containsDynamicValidator() {
	return getValidators().containsKey(ValidationAnnotation.DYNAMIC);
    }

    /**
     * Creates and puts EMPTY validator related to DYNAMIC validation annotation. Validation result for DYNAMIC validator can be set only from the outside logic, for e.g. using
     * method MetaProperty.setValidationResult().
     */
    public final void putDynamicValidator() {
	putValidator(ValidationAnnotation.DYNAMIC);
    }

    /**
     * Creates and puts EMPTY validator related to REQUIRED validation annotation. Validation result for REQURED validator can be set only from the outside logic, for e.g. using
     * method MetaProperty.setValidationResult().
     */
    public final void putRequiredValidator() {
	putValidator(ValidationAnnotation.REQUIRED);
    }

    /**
     * Used to create and put new validator (without any validation logic!) to validators list related to <code>valAnnotation</code>.
     *
     * This method should be used ONLY for DYNAMIC, REQUIRED and all validators that cannot change its result from its own "validate" method, but only from the outside method
     * MetaProperty.setValidationResult().
     *
     * @param valAnnotation
     */
    @SuppressWarnings("unchecked")
    private void putValidator(final ValidationAnnotation valAnnotation) {
	final Map<IBeforeChangeEventHandler, Result> map = new HashMap<IBeforeChangeEventHandler, Result>();
	map.put(StubValidator.singleton, null);
	getValidators().put(valAnnotation, map);
    }

    /**
     * Returns the array of "dependent property names".
     *
     * @return
     */
    public final String[] getDependentPropertyNames() {
	return dependentPropertyNames;
    }

    /**
     * Returns true if there is at least one correct dependent property related to this one, false otherwise.
     *
     * @return
     */
    public final boolean hasDependentProperties() {
	return dependentPropertyNames != null && dependentPropertyNames.length > 0;
    }

    public boolean isDirty() {
	return dirty || !entity.isPersisted();
    }

    public MetaProperty setDirty(final boolean dirty) {
	this.dirty = dirty;
	return this;
    }

    public boolean isUpperCase() {
	return upperCase;
    }

    /**
     * Restores property state to original if possible, which includes setting the original value and removal of all validation errors.
     */
    public final void restoreToOriginal() {
	resetValidationResult();
	// need to ignore composite key instance resetting
	if (!DynamicEntityKey.class.isAssignableFrom(type)) {
	    try {
		entity.set(name, getOriginalValue());
	    } catch (final Exception ex) {
		logger.debug("Could not restore to original property " + name + "#" + entity.getType().getName() + ".");
	    }
	}
	resetState();
    }

    /**
     * Resets validation results for all validators by setting their value to <code>null</code>.
     */
    public synchronized final void resetValidationResult() {
	for (final ValidationAnnotation va : validators.keySet()) {
	    final Map<IBeforeChangeEventHandler, Result> annotationHandlers = validators.get(va);
	    for (final IBeforeChangeEventHandler handler : annotationHandlers.keySet()) {
		annotationHandlers.put(handler, null);
	    }
	}
    }

    public boolean isEnforceMutator() {
	return enforceMutator;
    }

    public void setEnforceMutator(final boolean enforceMutator) {
	this.enforceMutator = enforceMutator;
    }

    private MetaProperty parentMetaPropertyOnDependencyPath;

    public boolean onDependencyPath(final MetaProperty dependentMetaProperty) {
	if (parentMetaPropertyOnDependencyPath == null) {
	    return false;
	}

	if (parentMetaPropertyOnDependencyPath == dependentMetaProperty) {
	    return true;
	} else {
	    return parentMetaPropertyOnDependencyPath.onDependencyPath(dependentMetaProperty);
	}
    }

    public void addToDependencyPath(final MetaProperty metaProperty) {
	if (parentMetaPropertyOnDependencyPath != null) {
	    final String msg = "Parent meta-property " + parentMetaPropertyOnDependencyPath.getName() + " for dependency path is already assigned.";
	    logger.error(msg);
	    throw new IllegalStateException(msg);
	}
	if (metaProperty == null) {
	    final String msg = "Parent meta-property for dependency path cannot be null.";
	    logger.error(msg);
	    throw new IllegalStateException(msg);
	}

	boolean contains = false;
	for (final String name : metaProperty.dependentPropertyNames) {
	    if (getName().equals(name)) {
		contains = true;
		break;
	    }
	}
	if (!contains) {
	    final String msg = "Meta-property " + metaProperty.getName() + " is not on dependency list.";
	    logger.error(msg);
	    throw new IllegalStateException(msg);
	}

	parentMetaPropertyOnDependencyPath = metaProperty;
    }

    public void removeFromDependencyPath(final MetaProperty metaProperty) {
	if (metaProperty == null) {
	    final String msg = "Meta-property to be removed should not be null.";
	    logger.error(msg);
	    throw new IllegalStateException(msg);
	}

	if (parentMetaPropertyOnDependencyPath != metaProperty) {
	    final String msg = "Parent meta-property " + parentMetaPropertyOnDependencyPath.getName() + " is different to " + metaProperty.getName() + ".";
	    logger.error(msg);
	    throw new IllegalStateException(msg);
	}

	parentMetaPropertyOnDependencyPath = null;
    }

    public boolean isAssigned() {
	return assigned;
    }

    public void setAssigned(final boolean hasAssignedValue) {
	this.assigned = hasAssignedValue;
    }
}
