package ua.com.fielden.platform.entity.fetch;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;

/**
 * Default implementation for {@link IEntityFetchStrategy} contract.
 *
 * @author TG Team
 *
 */
public class EntityFetchStrategy<T extends AbstractEntity<?>> implements IEntityFetchStrategy<T> {
    private final Class<T> entityType;
    private final LinkedHashMap<String, EntityFetchStrategy> propertyStrategies;

    /**
     * Constructs empty {@link EntityFetchStrategy}.
     *
     * @param entityType
     */
    public EntityFetchStrategy(final Class<T> entityType) {
        this(entityType, new LinkedHashMap<>());
    }

    private EntityFetchStrategy(final Class<T> entityType, final LinkedHashMap<String, EntityFetchStrategy> propertyStrategies) {
        this.entityType = entityType;
        this.propertyStrategies = propertyStrategies;
    }

    @Override
    public boolean shouldFetch(final String dotNotationProperty) {
        validate(dotNotationProperty);
        if (PropertyTypeDeterminator.isDotNotation(dotNotationProperty)) {
            final Pair<String, String> firstAndRest = PropertyTypeDeterminator.firstAndRest(dotNotationProperty);
            return propertyStrategies.containsKey(firstAndRest.getKey())
                    && strategyForFirstLevel(firstAndRest.getKey()).shouldFetch(firstAndRest.getValue());
        } else {
            return propertyStrategies.containsKey(dotNotationProperty);
        }
    }

    /**
     * Getter for entity fetch strategy for first-level properties (not dot-notated).
     *
     * @param property
     * @return
     */
    private EntityFetchStrategy strategyForFirstLevel(final String property) {
        final EntityFetchStrategy propStrategy = propertyStrategies.get(property);
        if (propStrategy == null) {
            throw new IllegalArgumentException("The property strategy should not be null.");
        }
        return propStrategy;
    }

    @Override
    public <M extends AbstractEntity<?>> IEntityFetchStrategy<M> strategyFor(final String dotNotationProperty) throws IllegalArgumentException, IllegalStateException {
        validateEntityTyped(dotNotationProperty);
        if (!shouldFetch(dotNotationProperty)) {
            throw new IllegalStateException(String.format("The property [%s] was not declared as 'fetching' property in this entity [%s] fetch strategy (please extend it).", dotNotationProperty, entityType.getSimpleName()));
        }
        return strategyForAllLevels(dotNotationProperty);
    }

    /**
     * Iterates through the dot-notation syntax to get the property fetch strategy (assuming it does exist).
     *
     * @param dotNotationProperty
     * @return
     */
    private EntityFetchStrategy strategyForAllLevels(final String dotNotationProperty) {
        if (PropertyTypeDeterminator.isDotNotation(dotNotationProperty)) {
            final Pair<String, String> firstAndRest = PropertyTypeDeterminator.firstAndRest(dotNotationProperty);
            return strategyForFirstLevel(firstAndRest.getKey()).strategyForAllLevels(firstAndRest.getValue());
        } else {
            return strategyForFirstLevel(dotNotationProperty);
        }
    }

    /**
     * Validates <code>dotNotationProperty</code> as the property inside <code>entityType</code>.
     *
     * @param dotNotationProperty
     */
    private Class<?> validate(final String dotNotationProperty) {
        final Class<?> propType;
        try {
            propType = PropertyTypeDeterminator.determinePropertyType(entityType, dotNotationProperty);
        } catch (final Exception e) {
            throw new IllegalStateException(String.format("The property [%s] does not exist in entity type [%s].", dotNotationProperty, entityType.getSimpleName()));
        }

        if (!AnnotationReflector.isAnnotationPresentInHierarchy(IsProperty.class, entityType, dotNotationProperty)) {
            throw new IllegalStateException(String.format("The field [%s] is not TG property (@IsProperty annotation is missing) in entity type [%s].", dotNotationProperty, entityType.getSimpleName()));
        }
        return propType;
    }

    /**
     * Validates <code>dotNotationProperty</code> as the entity-typed property inside <code>entityType</code>.
     *
     * @param dotNotationProperty
     */
    private void validateEntityTyped(final String dotNotationProperty) {
        final Class<?> propertyType = validate(dotNotationProperty);
        if (!EntityUtils.isEntityType(propertyType)) {
            throw new IllegalStateException(String.format("The property [%s] is not entity-typed in type [%s].", dotNotationProperty, entityType.getSimpleName()));
        }
    }

    @Override
    public IEntityFetchStrategy<T> with(final String dotNotationProperty, final String... otherDotNotationProperties) {
        final EntityFetchStrategy<T> copy = this.copy();
        copy.enhanceWith(dotNotationProperty);
        for (final String prop : otherDotNotationProperties) {
            copy.enhanceWith(prop);
        }
        return copy;
    }

    /**
     * Creates exact copy of entity fetch strategy.
     *
     * @return
     */
    private EntityFetchStrategy<T> copy() {
        final LinkedHashMap<String, EntityFetchStrategy> copiedPropertyStrategies = new LinkedHashMap<>();
        for (final Entry<String, EntityFetchStrategy> entry : propertyStrategies.entrySet()) {
            copiedPropertyStrategies.put(entry.getKey(), entry.getValue() == null ? null : entry.getValue().copy());
        }
        return new EntityFetchStrategy<T>(entityType, copiedPropertyStrategies);
    }

    private EntityFetchStrategy<T> enhanceWith(final String dotNotationProperty) {
        validate(dotNotationProperty);
        return enhanceWith0(dotNotationProperty);
    }

    private EntityFetchStrategy<T> enhanceWith0(final String dotNotationProperty) {
        if (PropertyTypeDeterminator.isDotNotation(dotNotationProperty)) {
            final Pair<String, String> firstAndRest = PropertyTypeDeterminator.firstAndRest(dotNotationProperty);
            final String firstName = firstAndRest.getKey();
            final String restDotNotation = firstAndRest.getValue();
            final boolean exists = propertyStrategies.containsKey(firstName);
            if (exists) {
                propertyStrategies.get(firstName).enhanceWith0(restDotNotation);
            } else {
                final Class<?> firstType = PropertyTypeDeterminator.determinePropertyType(entityType, firstName);
                propertyStrategies.put(firstName, createDefaultStrategy(firstType).enhanceWith0(restDotNotation));
            }
        } else {
            final boolean exists = propertyStrategies.containsKey(dotNotationProperty);
            if (exists) {
                // nothing to do (entity-typed or regular)
            } else {
                final Class<?> propertyType = PropertyTypeDeterminator.determinePropertyType(entityType, dotNotationProperty);
                if (EntityUtils.isEntityType(propertyType)) { // entity-typed
                    propertyStrategies.put(dotNotationProperty, createDefaultStrategy(propertyType));
                } else { // regular
                    propertyStrategies.put(dotNotationProperty, null);
                }
            }
        }
        return this;
    }

    /**
     * Creates default entity fetch strategy for entity-typed property.
     *
     * @param propertyType
     *            -- the type of the property
     *
     * @return
     */
    private EntityFetchStrategy createDefaultStrategy(final Class<?> propertyType) {
        // TODO continue implementation -- version and id? fetchOnly analog
        return new EntityFetchStrategy(propertyType);
    }
}
