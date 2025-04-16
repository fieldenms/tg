package ua.com.fielden.platform.entity.validation;

import java.util.HashMap;
import java.util.Map;

/**
 * This is a class representing domain validation configuration specifying the relationships between entity properties and the corresponding domain validators.
 */
public final class DomainValidationConfig {
    /**
     * A storage for validators where the key represents entity class and the corresponding value is a map between property names and a corresponding validator.
     */
    private final Map<Class<?>, Map<String, IBeforeChangeEventHandler>> domainValidators = new HashMap<>();

    /**
     * Return domain validator associated with property of the specified type (or one of its super types if it is not registered with property of the specified one).
     * The returned value is null if no association was found.
     * 
     * @param entityType
     * @param propertyName
     * @return
     */
    public IBeforeChangeEventHandler getValidator(final Class<?> entityType, final String propertyName) {
        final Map<Class<?>, Map<String, IBeforeChangeEventHandler>> allValidators = getAllValidatorsFor(entityType);
        IBeforeChangeEventHandler validator = null;
        Class<?> runningEntityType = entityType;
        while (validator == null && runningEntityType != Object.class) {
            final Map<String, IBeforeChangeEventHandler> validators = allValidators.get(runningEntityType);
            validator = validators != null ? validators.get(propertyName) : null;
            runningEntityType = runningEntityType.getSuperclass();
        }
        return validator;
    }

    /**
     * Associates an instance of domain validator with property of a certain entity type.
     * 
     * @param entityType
     * @param propertyName
     * @param domainValidator
     * @return
     */
    public DomainValidationConfig setValidator(final Class<?> entityType, final String propertyName, final IBeforeChangeEventHandler domainValidator) {
        final Map<String, IBeforeChangeEventHandler> map = domainValidators.computeIfAbsent(entityType, key -> new HashMap<>());
        map.put(propertyName, domainValidator); // this put replaces a validator if there was already one associated with the specified property
        return this;
    }

    /**
     * Returns all domain validators for the specified entity type and all of its supertypes.
     * 
     * @param entityType
     * @return
     */
    private Map<Class<?>, Map<String, IBeforeChangeEventHandler>> getAllValidatorsFor(final Class<?> entityType) {
        final Map<Class<?>, Map<String, IBeforeChangeEventHandler>> allValidators = new HashMap<>();
        Class<?> runningEntityType = entityType;
        while (runningEntityType != null) {
            final Map<String, IBeforeChangeEventHandler> validators = domainValidators.get(runningEntityType);
            if (validators != null) {
                allValidators.put(runningEntityType, validators);
            }
            runningEntityType = runningEntityType.getSuperclass();
        }
        return allValidators;
    }

}
