package ua.com.fielden.platform.entity.ioc;

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
    private Object proceed(final MethodInvocation mi, final MetaProperty property) throws Throwable {
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
            logger.error("Method " + method.getName() + " is not a valid mutator and thus can not be observed.");
            throw new IllegalStateException("Method " + method.getName() + " is not a valid mutator and thus can not be observed.");
        }
        // get the entity on which mutator is invoked
        final AbstractEntity<?> entity = (AbstractEntity<?>) invocation.getThis();
        logger.debug("Method is invoked on entity of type " + entity.getType().getName() + ".");
        final String propertyName = Mutator.deducePropertyNameFromMutator(method);
        final String fullPropertyName = entity.getType().getName() + "." + propertyName;
        logger.debug("Property name is \"" + fullPropertyName + "\".");
        final MetaProperty property = entity.getProperty(propertyName);
        // check if entity is not in the initialisation mode during which no property validation should be performed
        if (entity.isInitialising()) {
            logger.debug("Skip further logic: Initialisation of entity " + (AbstractUnionEntity.class.isAssignableFrom(entity.getClass()) ? entity.getClass() : entity)
                    + " is in progress.");
            return proceed(invocation, property);
        }
        // check if entity can be modified at all
        final Result editableResult = entity.isEditable();
        if (!editableResult.isSuccessful()) {
            logger.warn("Entity " + entity + " is not editable and none of its properties should be modified.");
            throw editableResult;
        }
        // proceed with property processing
        synchronized (entity) {
            entity.lock(); // this locking is required to prevent entity validation until all individual properties complete their modification (either successfully or not)
            try { // unlocking try-finally block
                if (property == null) { // this is possible only if key is set to dynamic composite key and the meta-property factory is not yet assigned... thus no observation is
                    // required at this stage
                    logger.debug("Skip further logic: Property \"" + fullPropertyName + "\" for intercepted mutator has no meta-property.");
                    return proceed(invocation, property);
                }
                final boolean wasValid = property.isValid(); // this flag is needed for correct change event firing
                logger.debug("Property \"" + fullPropertyName + "\" was valid: " + wasValid);
                final Pair<Object, Object> newAndOldValues = determineNewAndOldValues(entity, propertyName, invocation.getArguments()[0], method);
                final Object newValue = newAndOldValues.getKey();
                final Object oldValue = newAndOldValues.getValue();
                // logger.debug("Property \"" + fullPropertyName + "\" new value is \"" + newValue + "\", old value is \"" + oldValue + "\".");

                // perform validation and possibly setting ONLY in this three cases :
                logger.debug("Checking if validation is needed for \"" + fullPropertyName + "\"...");
                if (property.isEnforceMutator() || // enforcement happens in case of dependent properties
                        !wasValid || // here is the error recovery (forces validation + setter + necessarily firePropertyChange(!) )
                        wasValid && property.isCollectional() || // here validation + setter + firePropertyChange(see "processMutatorForCollectionalProperty" method) forces for
                        // collectional properties
                        wasValid && !property.isCollectional() && !EntityUtils.equalsEx(oldValue, newValue)) {
                    // //////////////////////////////////////////////////
                    // /////////////// validation ///////////////////////
                    // //////////////////////////////////////////////////
                    logger.debug("Check if property \"" + fullPropertyName + "\" has validators...");
                    if (property.hasValidators()) {
                        logger.debug("Execute validation for property \"" + fullPropertyName + "\".");
                        final Result result = property.validate(newValue, oldValue, property.getValidationAnnotations(), false);
                        if (!result.isSuccessful()) {
                            logger.debug("Property \"" + fullPropertyName + "\" validation failed: " + property.getFirstFailure());
                            // IMPORTANT : it fires ONLY the PropertyChangeOrIncorrectAttemptListeners!!!
                            entity.getChangeSupport().firePropertyChange(propertyName, oldValue, newValue, propertyWasValidAndNotEnforced(property, wasValid) ? CheckingStrategy.CHECK_EQUALITY
                                    : CheckingStrategy.CHECK_NOTHING, true);
                            // This return is a tricky one, since there in no really information as to what should be returned by the original method call.
                            // However, so far, validation was associated only with entity setters, which may return void or an entity instance.
                            return entity;
                        } else if (property.hasWarnings()) {
                            logger.debug("Property \"" + fullPropertyName + "\" validation complains about warning: " + property.getFirstWarning());
                        }
                    }
                    // validation was successful -- proceed with observing logic.
                    // //////////////////////////////////////////////////
                    // /////////////// observing ////////////////////////
                    // //////////////////////////////////////////////////
                    logger.debug("Property \"" + fullPropertyName + "\" validation succeeded. Proceeding with observing logic.");
                    // the observed setter could either be index or simple: setCollectionPropertyValue(index, value) or setPropertyValue(value)
                    if (!property.isCollectional() && Mutator.SETTER == Mutator.getValueByMethod(method)) { // covers setter for a simple property
                        return processSimpleProperty(entity, propertyName, invocation, propertyWasValidAndNotEnforced(property, wasValid), newAndOldValues);
                    } else if (property.isCollectional()) { // covers collectional property mutators
                        return processMutatorForCollectionalProperty(entity, propertyName, invocation, propertyWasValidAndNotEnforced(property, wasValid), newAndOldValues);
                    }
                    final String errorMsg = "Method " + entity.getType().getName() + "." + method.getName() + " is not recognised neither as a simple nor collectional mutator.";
                    logger.error(errorMsg);
                    throw new IllegalStateException(errorMsg);
                } else { // here the attempt of setting the same value was performed for the "valid" "simple" property
                    logger.debug("Validation for property \"" + fullPropertyName + "\" is not needed and nor further processing is required (mutator is not invoked).");
                    // the inner setter does not invoke - just return possible setter returning value (it possibly can be "entity")
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
        final int oldSize = collection == null ? 0 : collection.size();
        try {
            // try to proceed setter - if unhandled exception throws -> take it to the next level.
            final SetterResult setterResult = proceedSetter(entity, propertyName, mutator, wasValidAndNotEnforced, newAndOldValues);
            if (!setterResult.isSuccessful()) {
                // DYNAMIC validation didn't pass -> return "possible" setter returning value.
                return entity;
            } else {
                // get new value and size
                final Collection<?> newValue = (Collection<?>) entity.get(propertyName);
                final int newSize = newValue.size();
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
                final MetaProperty metaProperty = entity.getProperty(propertyName);
                if (metaProperty != null) {
                    // set previous value and recalculate meta-property properties based on the new value
                    metaProperty.setPrevValue(oldSize).define(newValue);
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