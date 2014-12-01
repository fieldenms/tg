package ua.com.fielden.platform.entity.ioc;

import static java.lang.String.*;
import java.lang.reflect.Method;
import java.util.Collection;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.log4j.Logger;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.Mutator;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.validation.StubValidator;
import ua.com.fielden.platform.entity.validation.annotation.ValidationAnnotation;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.utils.PropertyChangeSupportEx.CheckingStrategy;

/**
 *
 * This is a method intercepter used for property validation and firing property change events. It automates creation of entity properties that need to provide change notification
 * to registered listeners.
 *
 * This intercepter should be injected only for setters annotated with {@link Observable} covering both simple and indexed properties.
 *
 *
 * @author TG Team
 *
 */
public class ObservableMutatorInterceptor implements MethodInterceptor {
    private final Logger logger = Logger.getLogger(this.getClass());

    /**
     * Strictly this method should be used for proceeding with the original mutator call!
     *
     * @param mi
     * @return
     * @throws Throwable
     */
    private Object proceed(final MethodInvocation mi, final MetaProperty<?> property) throws Throwable {
        final Object result = mi.proceed();
        if (property != null) {
            property.setAssigned(true);
        }
        return result;
    }

    /**
     * Setter invocation interception logic.
     */
    @Override
    public Object invoke(final MethodInvocation invocation) throws Throwable {
        logger.debug("Intercepting " + invocation.getMethod().getName());
        // perform some trivial validation to ensure that this intercepter is associated with a recognised mutator
        final Method method = invocation.getMethod();
        if (!Mutator.isMutator(method)) {
            final String msg = format("Method %s is not a valid mutator and thus can not be observed.", method.getName());
            logger.error(msg);
            throw new IllegalStateException(msg);
        }
        // get the entity on which mutator is invoked
        final AbstractEntity<?> entity = (AbstractEntity<?>) invocation.getThis();
        logger.debug(format("Method is invoked on entity of type %s.", entity.getType().getName()));
        final String propertyName = Mutator.deducePropertyNameFromMutator(method);
        final String fullPropertyName = entity.getType().getName() + "." + propertyName;
        logger.debug(format("Property name is \"%s\".", fullPropertyName));
        final MetaProperty property = entity.getProperty(propertyName);
        // check if entity is not in the initialisation mode during which no property validation should be performed
        if (entity.isInitialising()) {
            logger.debug(format("Skip further logic: Initialisation of entity is in progress.", AbstractUnionEntity.class.isAssignableFrom(entity.getClass()) ? entity.getClass()
                    : entity));
            return proceed(invocation, property);
        }
        // check if entity can be modified at all
        final Result editableResult = entity.isEditable();
        if (!entity.isIgnoreEditableState() && !editableResult.isSuccessful()) {
            logger.warn(format("Entity is not editable and none of its properties should be modified.", entity));
            throw editableResult;
        }
        // proceed with property processing
        synchronized (entity) {
            entity.lock(); // this locking is required to prevent entity validation until all individual properties complete their modification (either successfully or not)
            try { // unlocking try-finally block
                if (property == null) { // this is possible only if key is set to dynamic composite key and the meta-property factory is not yet assigned... thus no observation is
                    // required at this stage
                    logger.debug(format("Skip further logic: Property \"%s\" for intercepted mutator has no meta-property.", fullPropertyName));
                    return proceed(invocation, property);
                }
                final boolean wasValid = property.isValid(); // this flag is needed for correct change event firing
                logger.debug(format("Property \"%s\" was valid: %s", fullPropertyName, wasValid));
                final Pair<Object, Object> newAndOldValues = determineNewAndOldValues(entity, propertyName, invocation.getArguments()[0], method);
                final Object newValue = newAndOldValues.getKey();
                final Object oldValue = newAndOldValues.getValue();
                // logger.debug("Property \"" + fullPropertyName + "\" new value is \"" + newValue + "\", old value is \"" + oldValue + "\".");

                // perform validation and possibly setting of the passed in value
                logger.debug(format("Checking if validation is needed for \"\"...", fullPropertyName));
                if (// enforcement happens in case of dependent properties
                    property.isEnforceMutator() ||
                    // or it could be an error recovery (forces validation + setter + necessarily firePropertyChange(!) )
                    !wasValid ||
                    // or it could be validation + setter + firePropertyChange(see "processMutatorForCollectionalProperty" method) for a collectional property
                    property.isCollectional() ||
                    // or the new value is null and the property is required -- need to trigger validation in such cases even if the current prop value is null
                    newValue == null && property.isRequired() ||
                    // or this is a genuine attempt to set a new property value
                    !EntityUtils.equalsEx(oldValue, newValue)) {
                    ////////////////////////////////////////////////////
                    ///////////////// validation ///////////////////////
                    ////////////////////////////////////////////////////
                    logger.debug(format("Check if property \"%s\" has validators...", fullPropertyName));
                    if (property.hasValidators()) {
                        logger.debug(format("Execute validation for property \"%s\".", fullPropertyName));
                        final Result result = property.validate(newValue, oldValue, property.getValidationAnnotations(), false);
                        if (!result.isSuccessful()) {
                            logger.debug(format("Property \"%s\" validation failed: %s", fullPropertyName, property.getFirstFailure()));
                            // IMPORTANT : it fires ONLY the PropertyChangeOrIncorrectAttemptListeners!!!
                            entity.getChangeSupport().firePropertyChange(
                                    propertyName,
                                    oldValue,
                                    newValue,
                                    propertyWasValidAndNotEnforced(property, wasValid) ? CheckingStrategy.CHECK_EQUALITY : CheckingStrategy.CHECK_NOTHING,
                                    true);
                            // This return is a tricky one, since there in no really information as to what should be returned by the original method call.
                            // However, so far, validation was associated only with entity setters, which should return an instance of its owner or void.
                            return entity;
                        } else if (property.hasWarnings()) {
                            logger.debug(format("Property \"%s\" validation complains about warning: %s", fullPropertyName, property.getFirstWarning()));
                        }
                    }
                    // validation was successful -- proceed with observing logic.
                    // //////////////////////////////////////////////////
                    // /////////////// observing ////////////////////////
                    // //////////////////////////////////////////////////
                    logger.debug(format("Property \"%s\" validation succeeded. Proceeding with observing logic.", fullPropertyName));
                    // the observed setter could either be index or simple: setCollectionPropertyValue(index, value) or setPropertyValue(value)
                    if (!property.isCollectional() && Mutator.SETTER == Mutator.getValueByMethod(method)) { // covers setter for a simple property
                        return processSimpleProperty(entity, propertyName, invocation, propertyWasValidAndNotEnforced(property, wasValid), newAndOldValues);
                    } else if (property.isCollectional()) { // covers collectional property mutators
                        return processMutatorForCollectionalProperty(entity, propertyName, invocation, propertyWasValidAndNotEnforced(property, wasValid), newAndOldValues);
                    }
                    final String errorMsg = format("Method %s.%s is not recognised neither as a simple nor collectional mutator.", entity.getType().getName(), method.getName());
                    logger.error(errorMsg);
                    throw new IllegalStateException(errorMsg);
                } else { // here the attempt of setting the same value was performed for the "valid" "simple" property
                    logger.debug(format("Validation for property \"%s\" is not needed and nor further processing is required (mutator is not invoked).", fullPropertyName));
                    // the inner setter does not invoke - just return a most likely returning value for a setter, which should be an entity itself
                    return entity;
                }
            } finally {
                entity.unlock();
            }
        }
    }

    private boolean propertyWasValidAndNotEnforced(final MetaProperty property, final boolean wasValid) {
        return wasValid && !property.isEnforceMutator();
    }

    /**
     * Handles processing of the setter for a simple property.
     *
     * @param entity
     * @param propertyName
     * @param mutator
     * @param wasValidAndNotEnforced
     * @return
     * @throws Throwable
     */
    private Object processSimpleProperty(final AbstractEntity<?> entity, final String propertyName, final MethodInvocation mutator, final boolean wasValidAndNotEnforced, final Pair<Object, Object> newAndOldValues)
            throws Throwable {
        final String fullPropertyName = entity.getType().getName() + "." + propertyName;
        logger.debug("Processing simple property \"" + fullPropertyName + "\".");
        final Object oldValue = entity.get(propertyName);
        final Object newValue = mutator.getArguments()[0];
        try {
            // try to proceed setter - if unhandled exception throws -> take it to the next level.
            final SetterResult setterResult = proceedSetter(entity, propertyName, mutator, wasValidAndNotEnforced, newAndOldValues);
            if (!setterResult.isSuccessful()) {
                // DYNAMIC validation didn't pass -> return "possible" setter returning value.
                return entity;
            } else {
                // ///// ALL validation succeeded : ///////
                // fire change event: if wasValid - use standard equality checking, if wasInvalid -> fire all listeners events always!
                // IMPORTANT : it fires ALL the listeners - including the PropertyChangeOrIncorrectAttemptListeners and all other listeners added externally by the user
                entity.getChangeSupport().firePropertyChange(propertyName, oldValue, newValue, wasValidAndNotEnforced ? CheckingStrategy.CHECK_EQUALITY
                        : CheckingStrategy.CHECK_NOTHING, false);
                // update meta-property information
                final MetaProperty metaProperty = entity.getProperty(propertyName);
                if (metaProperty != null) {
                    // set previous value and recalculate meta-property properties based on the new value
                    metaProperty.setPrevValue(oldValue).define(newValue);
                    // determine property and entity dirty state
                    if (!metaProperty.isCalculated() && metaProperty.getValueChangeCount() > 0) {
                        final boolean isValueDifferentFromOriginal = !EntityUtils.equalsEx(metaProperty.getOriginalValue(), newValue);

                        metaProperty.setDirty(isValueDifferentFromOriginal);

                        entity.setDirty(entity.isDirty() || metaProperty.isDirty());
                    }
                }
                // handle updating of the dependent properties (dependent properties error recovery).
                handleDependentProperties(metaProperty);
                return setterResult.getSetterReturningValue();
            }
        } catch (final Throwable e) {
            // if unhandled exception throws -> take it to the next level.
            logger.error("Unhandled exception while processing simple property \"" + fullPropertyName + "\".", e);
            throw e;
        } finally {
            logger.debug("Finished processing simple property \"" + fullPropertyName + "\".");
        }
    }

    /**
     * Handles processing of mutator for a collectional property, which includes all three possible mutators.
     * <p>
     *
     * Important : it fires property change in all cases
     *
     * @param entity
     * @param propertyName
     * @param mutator
     * @param wasValidAndNotEnforced
     * @return
     * @throws Throwable
     */
    private Object processMutatorForCollectionalProperty(final AbstractEntity<?> entity, final String propertyName, final MethodInvocation mutator, final boolean wasValidAndNotEnforced, final Pair<Object, Object> newAndOldValues)
            throws Throwable {
        final String fullPropertyName = entity.getType().getName() + "." + propertyName;
        logger.debug("Processing collectional property \"" + fullPropertyName + "\".");
        // get size before invoking mutator
        final Collection<?> collection = (Collection<?>) entity.get(propertyName);
        final Integer oldSize = collection == null ? 0 : collection.size();
        try {
            // try to proceed setter - if unhandled exception throws -> take it to the next level.
            final SetterResult setterResult = proceedSetter(entity, propertyName, mutator, wasValidAndNotEnforced, newAndOldValues);
            if (!setterResult.isSuccessful()) {
                // DYNAMIC validation didn't pass -> return "possible" setter returning value.
                return entity;
            } else {
                // get new value and size
                final Collection<?> newValue = entity.get(propertyName);
                final Integer newSize = newValue.size();
                // fire change event
                if (Mutator.SETTER == Mutator.getValueByMethod(mutator.getMethod())) {
                    entity.getChangeSupport().firePropertyChange(propertyName, 0, 1);
                } else {
                    if (Mutator.INCREMENTOR == Mutator.getValueByMethod(mutator.getMethod())) {
                        final Object newAddedValue = mutator.getArguments()[0];
                        entity.getChangeSupport().firePropertyChange(propertyName, oldSize, newAddedValue);
                    } else { // is DECREMENTOR
                        final Object oldRemovedValue = mutator.getArguments()[0];
                        entity.getChangeSupport().firePropertyChange(propertyName, oldRemovedValue, newSize);
                    }
                }
                // update meta-property information
                final MetaProperty<Collection<?>> metaProperty = entity.getProperty(propertyName);
                if (metaProperty != null) {
                    // set previous value and recalculate meta-property properties based on the new value
                    metaProperty.setCollectionPrevSize(oldSize);
                    metaProperty.define(newValue);
                }

                // determine property and entity dirty state
                if (!metaProperty.isCalculated() && metaProperty.getValueChangeCount() > 0) {
                    metaProperty.setDirty(oldSize != newSize);
                    entity.setDirty(entity.isDirty() || metaProperty.isDirty());
                }

                // handle updating of the dependent properties (dependent properties error recovery).
                handleDependentProperties(metaProperty);
                return setterResult.getSetterReturningValue();
            }
        } catch (final Throwable e) {
            // if unhandled exception throws -> take it to the next level.
            logger.error("Unhandled exception while processing collectional property \"" + fullPropertyName + "\".", e);
            throw e;
        } finally {
            logger.debug("Finished processing collectional property \"" + fullPropertyName + "\".");
        }
    }

    /**
     * Executes the original mutator:
     * <ul>
     * <li>If the setter causes Exception (but not a Result type exception) then it is re-thrown;
     * <li>If the setter causes Result exception then method catches it and handles like DYNAMIC validation result returning [false + null as valueReturnedBySetter];
     * <li>If setter executes with no exceptions (success) then [true + valueReturnedBySetter] is returned.
     * </ul>
     * <p>
     * Additionally, in case where the target entity is of type {@link AbstractUnionEntity} and its setter executes successfully then a union rule is enforced by invoking
     * {@link AbstractUnionEntity#ensureUnion(String, AbstractEntity)}.
     *
     * @param entity
     * @param propertyName
     * @param mutator
     * @param wasValidAndNotEnforced
     * @param newAndOldValues
     * @return
     * @throws Throwable
     */
    private SetterResult proceedSetter(final AbstractEntity<?> entity, final String propertyName, final MethodInvocation mutator, final boolean wasValidAndNotEnforced, final Pair<Object, Object> newAndOldValues)
            throws Throwable {
        final MetaProperty metaProperty = entity.getProperty(propertyName);
        try {
            final Object setterReturningValue = proceed(mutator, metaProperty);
            // check if the entity a union entity, which grands some extra processing
            if (AbstractUnionEntity.class.isAssignableFrom(entity.getType()) && AbstractEntity.class.isAssignableFrom(entity.getPropertyType(propertyName))) {
                // if entity is of type AbstractUnionEntity then its properties can only be of type AbstractEntity
                ((AbstractUnionEntity) entity).ensureUnion(propertyName);
            }
            // setter proceeded successfully (no exception or result were thrown). -> update DYNAMIC validator by correct result if validator exists and if no warning was detected
            // :
            if (metaProperty.containsDynamicValidator() && !metaProperty.hasWarnings()) {
                metaProperty.setValidationResult(ValidationAnnotation.DYNAMIC, StubValidator.singleton, new Result(entity, "Dynamic validation (inside the setter) passed correctly."));
            }
            return new SetterResult(true, setterReturningValue);
        } catch (final Throwable ex) {
            if (ex instanceof Result) {
                if (!metaProperty.containsDynamicValidator()) {
                    metaProperty.putDynamicValidator();
                }
                // All validation proceeded successfully except the validation inside the setter (DYNAMIC validation).
                metaProperty.setValidationResult(ValidationAnnotation.DYNAMIC, StubValidator.singleton, (Result) ex);
                final boolean isWarning = ((Result) ex).isWarning();
                if (!isWarning) {
                    final Object oldValue = newAndOldValues.getValue();
                    final Object newValue = newAndOldValues.getKey();
                    // Important : some components (such as Autocompleter) use LastInvalidValue as updating value. So it HAVE TO BE correctly updated!
                    metaProperty.setLastInvalidValue(newValue);
                    // fire IncorrectAttemptListeners :
                    entity.getChangeSupport().firePropertyChange(propertyName, oldValue, newValue, wasValidAndNotEnforced ? CheckingStrategy.CHECK_EQUALITY
                            : CheckingStrategy.CHECK_NOTHING, true);
                }
                return new SetterResult(isWarning, null);
            } else {
                // the exception occurred in setter - is of unknown type - so throw it on the higher level.
                throw ex;
            }
        }
    }

    /**
     * Class that composes 'success' flag and 'returningValue' to indicate the results of the setter invocation.
     *
     * @author Jhou
     *
     */
    private class SetterResult {
        final boolean successful;
        final Object setterReturningValue;

        public SetterResult(final boolean successful, final Object setterReturningValue) {
            this.successful = successful;
            this.setterReturningValue = setterReturningValue;
        }

        public boolean isSuccessful() {
            return successful;
        }

        public Object getSetterReturningValue() {
            return setterReturningValue;
        }
    }

    /**
     * If there are some dependent properties for 'metaProperty' - then these dependent properties will be updated by the lastInvalidValue (only in the case of INVALID dependent
     * properties).
     *
     * @param metaProperty
     */
    private void handleDependentProperties(final MetaProperty metaProperty) {
        if (metaProperty.hasDependentProperties()) {
            final AbstractEntity<?> entity = metaProperty.getEntity();
            for (final String dependentPropertyName : metaProperty.getDependentPropertyNames()) {
                final MetaProperty dependentMetaProperty = entity.getProperty(dependentPropertyName);

                if (!dependentMetaProperty.onDependencyPath(metaProperty) && !metaProperty.onDependencyPath(dependentMetaProperty)) {
                    dependentMetaProperty.addToDependencyPath(metaProperty);
                    try {
                        if (!dependentMetaProperty.isValid()) { // is this an error recovery situation?
                            dependentMetaProperty.setEnforceMutator(true);
                            try {
                                entity.set(dependentPropertyName, dependentMetaProperty.getLastAttemptedValue());
                            } finally {
                                dependentMetaProperty.setEnforceMutator(false);
                            }
                        } else if (dependentMetaProperty.revalidate(true).isSuccessful()) { // otherwise simply re-validate
                            handleDependentProperties(dependentMetaProperty);
                        }
                    } finally {
                        dependentMetaProperty.removeFromDependencyPath(metaProperty);
                    }
                }
                logger.debug("Dependent property [" + dependentPropertyName + "] for property [" + metaProperty.getName() + "] successfully updated.");
            }
        }

    }

    /**
     * Determines correct newValue and oldValue. {@link Pair} is used to return a pair of values where the key represents newValue and the value represents oldValue.
     *
     * @param entity
     * @param metaProperty
     * @param newValue
     * @param mutator
     * @return
     * @throws Exception
     */
    private Pair<Object, Object> determineNewAndOldValues(final AbstractEntity<?> entity, final String propertyName, final Object newValue, final Method mutator) throws Exception {
        // setter?
        if (Mutator.SETTER == Mutator.getValueByMethod(mutator)) { // this covers both simple and collectional properties
            return new Pair<Object, Object>(newValue, entity.get(propertyName));
        }
        // incrementor?
        if (Mutator.INCREMENTOR == Mutator.getValueByMethod(mutator)) {
            return new Pair<Object, Object>(newValue, null);
        }
        // decrementor
        return new Pair<Object, Object>(newValue, null);
    }

}