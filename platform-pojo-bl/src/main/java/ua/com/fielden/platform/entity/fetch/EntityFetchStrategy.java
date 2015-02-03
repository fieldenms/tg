package ua.com.fielden.platform.entity.fetch;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils;
import ua.com.fielden.platform.entity.query.fluent.fetch;
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
    private final LinkedHashMap<String, EntityFetchStrategy<AbstractEntity<?>>> propertyStrategies;
    private fetch<T> fetchModel;

    /**
     * A factory method to create a default entity fetch strategy for entity-typed property.
     *
     * @param entityType
     *            -- the type of the property
     *
     * @return
     */
    public static <T extends AbstractEntity<?>> EntityFetchStrategy<T> createDefaultStrategy(final Class<T> entityType) {
        // TODO continue implementation -- version and id? fetchOnly analog
        return new EntityFetchStrategy<T>(entityType);
    }

    /**
     * Constructs empty {@link EntityFetchStrategy}.
     *
     * @param entityType
     */
    private EntityFetchStrategy(final Class<T> entityType) {
        this(entityType, new LinkedHashMap<>());
    }

    private EntityFetchStrategy(final Class<T> entityType, final LinkedHashMap<String, EntityFetchStrategy<AbstractEntity<?>>> propertyStrategies) {
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
    private EntityFetchStrategy<AbstractEntity<?>> strategyForFirstLevel(final String property) {
        final EntityFetchStrategy<AbstractEntity<?>> propStrategy = propertyStrategies.get(property);
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
        return (EntityFetchStrategy<M>) strategyForAllLevels(dotNotationProperty);
    }

    /**
     * Iterates through the dot-notation syntax to get the property fetch strategy (assuming it does exist).
     *
     * @param dotNotationProperty
     * @return
     */
    private EntityFetchStrategy<AbstractEntity<?>> strategyForAllLevels(final String dotNotationProperty) {
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

    /**
     * Validates <code>dotNotationProperty</code> as the entity-typed property inside <code>entityType</code>.
     *
     * @param dotNotationProperty
     */
    private static <M extends AbstractEntity<?>> void validateTypes(final EntityFetchStrategy<M> strategy1, final EntityFetchStrategy<M> strategy2) {
        if (!EntityUtils.equalsEx(strategy1.entityType(), strategy2.entityType())) {
            throw new IllegalArgumentException(String.format("Two strategies of incompatible entity types [%s and %s] is trying to be merged.", strategy1.entityType().getSimpleName(), strategy2.entityType().getSimpleName()));
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

    @Override
    public <M extends AbstractEntity<?>> IEntityFetchStrategy<T> with(final String dotNotationProperty, final IEntityFetchStrategy<M> propertyFetchStrategy) {
        if (propertyFetchStrategy == null) {
            throw new IllegalArgumentException(String.format("Please provide non-null property fetch strategy for property [%s] for type [%s]. Or use method 'with(dotNotationProperty)' for default property strategy.", dotNotationProperty, entityType.getSimpleName()));
        }
        final EntityFetchStrategy<T> copy = this.copy();
        copy.enhanceWith(dotNotationProperty, (EntityFetchStrategy<AbstractEntity<?>>) propertyFetchStrategy);
        return copy;
    }

    @Override
    public IEntityFetchStrategy<T> with(final IEntityFetchStrategy<T> otherStrategy) {
        validateTypes(this, (EntityFetchStrategy<T>) otherStrategy);
        final EntityFetchStrategy<T> copy = this.copy();
        final EntityFetchStrategy<T> that = (EntityFetchStrategy<T>) otherStrategy;
        for (final Map.Entry<String, EntityFetchStrategy<AbstractEntity<?>>> entry : that.propertyStrategies().entrySet()) {
            // no property name validation is required (it was done earlier)
            copy.enhanceWith0(entry.getKey(), entry.getValue());
        }
        return copy;
    }

    /**
     * Creates exact copy of entity fetch strategy.
     *
     * @return
     */
    private EntityFetchStrategy<T> copy() {
        final LinkedHashMap<String, EntityFetchStrategy<AbstractEntity<?>>> copiedPropertyStrategies = new LinkedHashMap<>();
        for (final Entry<String, EntityFetchStrategy<AbstractEntity<?>>> entry : propertyStrategies.entrySet()) {
            copiedPropertyStrategies.put(entry.getKey(), entry.getValue() == null ? null : entry.getValue().copy());
        }
        return new EntityFetchStrategy<T>(entityType, copiedPropertyStrategies);
    }

    /**
     * Enhances (mutably) this entity fetch strategy with dot-notation property (and validates before that).
     *
     * @param dotNotationProperty
     * @return
     */
    private EntityFetchStrategy<T> enhanceWith(final String dotNotationProperty) {
        validate(dotNotationProperty);
        return enhanceWith0(dotNotationProperty, null);
    }

    /**
     * Enhances (mutably) this entity fetch strategy with entity-typed dot-notation property strategy (and validates before that).
     *
     * @param dotNotationProperty
     * @return
     */
    private EntityFetchStrategy<T> enhanceWith(final String dotNotationProperty, final EntityFetchStrategy<AbstractEntity<?>> propertyStrategy) {
        validateEntityTyped(dotNotationProperty);
        return enhanceWith0(dotNotationProperty, propertyStrategy);
    }

    /**
     * Enhances (mutably) this entity fetch strategy with dot-notation property.
     *
     * @param dotNotationProperty
     * @param specifiedStrategy
     *            -- if not <code>null</code> -- additional strategy for the property to be merged with existing one, if <code>null</code> -- default strategy should be created
     *            (entity-typed property or regular)
     * @return
     */
    private EntityFetchStrategy<T> enhanceWith0(final String dotNotationProperty, final EntityFetchStrategy<AbstractEntity<?>> propertyStrategy) {
        if (PropertyTypeDeterminator.isDotNotation(dotNotationProperty)) {
            final Pair<String, String> firstAndRest = PropertyTypeDeterminator.firstAndRest(dotNotationProperty);
            final String firstName = firstAndRest.getKey();
            final String restDotNotation = firstAndRest.getValue();
            final boolean exists = propertyStrategies.containsKey(firstName);
            if (exists) {
                propertyStrategies.get(firstName).enhanceWith0(restDotNotation, propertyStrategy);
            } else {
                final Class<?> firstType = PropertyTypeDeterminator.determinePropertyType(entityType, firstName);
                propertyStrategies.put(firstName, createDefaultStrategy((Class<AbstractEntity<?>>) firstType).enhanceWith0(restDotNotation, propertyStrategy));
            }
        } else {
            final boolean exists = propertyStrategies.containsKey(dotNotationProperty);
            final Class<?> propertyType = PropertyTypeDeterminator.determinePropertyType(entityType, dotNotationProperty);
            if (exists) {
                if (EntityUtils.isEntityType(propertyType) && propertyStrategy != null) {
                    final EntityFetchStrategy<AbstractEntity<?>> merged = (EntityFetchStrategy<AbstractEntity<?>>) propertyStrategy.with(propertyStrategies.get(dotNotationProperty));
                    propertyStrategies.put(dotNotationProperty, merged);
                } // else -- regular property or entity-typed
            } else {
                if (EntityUtils.isEntityType(propertyType)) { // entity-typed
                    propertyStrategies.put(dotNotationProperty, propertyStrategy == null ? createDefaultStrategy((Class<AbstractEntity<?>>) propertyType) : propertyStrategy.copy());
                } else { // regular
                    propertyStrategies.put(dotNotationProperty, null);
                }
            }
        }
        return this;
    }

    private LinkedHashMap<String, EntityFetchStrategy<AbstractEntity<?>>> propertyStrategies() {
        return propertyStrategies;
    }

    private Class<T> entityType() {
        return entityType;
    }

    @Override
    public fetch<T> fetchModel() {
        if (fetchModel == null) {
            fetchModel = createFetchModel();
        }
        return fetchModel;
    }

    /**
     * Creates the fetch model from entity fetch strategy for entity retrieval process using EQL.
     *
     * @return
     */
    private fetch<T> createFetchModel() {
        fetch<T> fetchModel = EntityQueryUtils.fetchOnly(entityType);
        for (final Map.Entry<String, EntityFetchStrategy<AbstractEntity<?>>> entry : propertyStrategies.entrySet()) {
            final EntityFetchStrategy<AbstractEntity<?>> propModel = entry.getValue();
            if (propModel == null) {
                fetchModel = fetchModel.with(entry.getKey());
            } else {
                fetchModel = fetchModel.with(entry.getKey(), propModel.fetchModel());
            }
        }
        return fetchModel;
    }
}
