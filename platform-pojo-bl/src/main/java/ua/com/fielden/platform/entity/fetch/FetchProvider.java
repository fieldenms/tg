package ua.com.fielden.platform.entity.fetch;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CritOnly;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;

/**
 * Default implementation for {@link IFetchProvider} contract.
 *
 * @author TG Team
 *
 */
class FetchProvider<T extends AbstractEntity<?>> implements IFetchProvider<T> {
    private final Class<T> entityType;
    private final LinkedHashMap<String, FetchProvider<AbstractEntity<?>>> propertyProviders;
    private fetch<T> fetchModel;
    private final boolean withKeyAndDesc;
    private final Logger logger = Logger.getLogger(getClass());

    /**
     * Constructs empty {@link FetchProvider}.
     *
     * @param entityType
     */
    FetchProvider(final Class<T> entityType, final boolean withKeyAndDesc) {
        this(entityType, new LinkedHashMap<>(), withKeyAndDesc);
    }

    private FetchProvider(final Class<T> entityType, final LinkedHashMap<String, FetchProvider<AbstractEntity<?>>> propertyStrategies, final boolean withKeyAndDesc) {
        this.entityType = entityType;
        this.propertyProviders = propertyStrategies;
        this.withKeyAndDesc = withKeyAndDesc;
    }

    @Override
    public boolean shouldFetch(final String dotNotationProperty) {
        validate(dotNotationProperty);
        if (PropertyTypeDeterminator.isDotNotation(dotNotationProperty)) {
            final Pair<String, String> firstAndRest = PropertyTypeDeterminator.firstAndRest(dotNotationProperty);
            return propertyProviders.containsKey(firstAndRest.getKey())
                    && providerForFirstLevel(firstAndRest.getKey()).shouldFetch(firstAndRest.getValue());
        } else {
            return propertyProviders.containsKey(dotNotationProperty);
        }
    }

    /**
     * Getter for fetch provider for first-level properties (not dot-notated).
     *
     * @param property
     * @return
     */
    private FetchProvider<AbstractEntity<?>> providerForFirstLevel(final String property) {
        final FetchProvider<AbstractEntity<?>> propProvider = propertyProviders.get(property);
        if (propProvider == null) {
            throw new IllegalArgumentException("The property fetch provider should not be null.");
        }
        return propProvider;
    }

    @Override
    public <M extends AbstractEntity<?>> IFetchProvider<M> fetchFor(final String dotNotationProperty) throws IllegalArgumentException, IllegalStateException {
        validateEntityTyped(dotNotationProperty);
        if (!shouldFetch(dotNotationProperty)) {
            throw new IllegalStateException(String.format("The property [%s] was not declared as 'fetching' property in this entity [%s] fetch provider (please extend it).", dotNotationProperty, entityType.getSimpleName()));
        }
        return (FetchProvider<M>) providerForAllLevels(dotNotationProperty);
    }

    /**
     * Iterates through the dot-notation syntax to get the property fetch provider (assuming it does exist).
     *
     * @param dotNotationProperty
     * @return
     */
    private FetchProvider<AbstractEntity<?>> providerForAllLevels(final String dotNotationProperty) {
        if (PropertyTypeDeterminator.isDotNotation(dotNotationProperty)) {
            final Pair<String, String> firstAndRest = PropertyTypeDeterminator.firstAndRest(dotNotationProperty);
            return providerForFirstLevel(firstAndRest.getKey()).providerForAllLevels(firstAndRest.getValue());
        } else {
            return providerForFirstLevel(dotNotationProperty);
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
            throw new IllegalArgumentException(String.format("The property [%s] is not entity-typed in type [%s].", dotNotationProperty, entityType.getSimpleName()));
        }
    }

    /**
     * Validates whether the <code>entityType</code>s for fetch providers are the same.
     *
     * @param dotNotationProperty
     */
    private static <M extends AbstractEntity<?>> void validateTypes(final FetchProvider<M> provider1, final FetchProvider<M> provider2) {
        if (!EntityUtils.equalsEx(provider1.entityType(), provider2.entityType())) {
            throw new IllegalArgumentException(String.format("Two fetch providers of incompatible entity types [%s and %s] are trying to be merged.", provider1.entityType().getSimpleName(), provider2.entityType().getSimpleName()));
        }
    }

    @Override
    public IFetchProvider<T> with(final String dotNotationProperty, final String... otherDotNotationProperties) {
        final FetchProvider<T> copy = this.copy();
        copy.enhanceWith(dotNotationProperty);
        for (final String prop : otherDotNotationProperties) {
            copy.enhanceWith(prop);
        }
        return copy;
    }

    @Override
    public IFetchProvider<T> with(final Set<String> dotNotationProperties) {
        final FetchProvider<T> copy = this.copy();
        for (final String prop : dotNotationProperties) {
            copy.enhanceWith(prop);
        }
        return copy;
    }

    @Override
    public IFetchProvider<T> without(final String dotNotationProperty, final String... otherDotNotationProperties) {
        final FetchProvider<T> copy = this.copy();
        copy.removeIfExists(dotNotationProperty);
        for (final String prop : otherDotNotationProperties) {
            copy.removeIfExists(prop);
        }
        return copy;
    }

    @Override
    public <M extends AbstractEntity<?>> IFetchProvider<T> with(final String dotNotationProperty, final IFetchProvider<M> propertyFetchProvider) {
        if (propertyFetchProvider == null) {
            throw new IllegalArgumentException(String.format("Please provide non-null property fetch provider for property [%s] for type [%s]. Or use method 'with(dotNotationProperty)' for default property provider.", dotNotationProperty, entityType.getSimpleName()));
        }
        final FetchProvider<T> copy = this.copy(withKeyAndDesc);
        copy.enhanceWith(dotNotationProperty, (FetchProvider<AbstractEntity<?>>) propertyFetchProvider);
        return copy;
    }

    @Override
    public IFetchProvider<T> with(final IFetchProvider<T> otherStrategy) {
        validateTypes(this, (FetchProvider<T>) otherStrategy);
        final FetchProvider<T> that = (FetchProvider<T>) otherStrategy;

        final boolean mergedWithKeyAndDesc = this.withKeyAndDesc || that.withKeyAndDesc;

        final FetchProvider<T> copy = this.copy(mergedWithKeyAndDesc);
        for (final Map.Entry<String, FetchProvider<AbstractEntity<?>>> entry : that.propertyProviders().entrySet()) {
            // no property name validation is required (it was done earlier)
            copy.enhanceWith0(entry.getKey(), entry.getValue());
        }
        return copy;
    }

    /**
     * Creates exact copy of entity fetch provider.
     *
     * @return
     */
    private FetchProvider<T> copy() {
        return copy(withKeyAndDesc);
    }

    /**
     * Creates exact copy of entity fetch provider.
     *
     * @return
     */
    private FetchProvider<T> copy(final boolean withKeyAndDesc) {
        return new FetchProvider<T>(entityType, copyPropertyProviders(), withKeyAndDesc);
    }

    private LinkedHashMap<String, FetchProvider<AbstractEntity<?>>> copyPropertyProviders() {
        final LinkedHashMap<String, FetchProvider<AbstractEntity<?>>> copiedPropertyProviders = new LinkedHashMap<>();
        for (final Entry<String, FetchProvider<AbstractEntity<?>>> entry : propertyProviders.entrySet()) {
            copiedPropertyProviders.put(entry.getKey(), entry.getValue() == null ? null : entry.getValue().copy());
        }
        return copiedPropertyProviders;
    }

    @Override
    public <V extends AbstractEntity<?>> IFetchProvider<V> copy(final Class<V> managedType) {
        return new FetchProvider<V>(managedType, copyPropertyProviders(), withKeyAndDesc);
    }

    /**
     * Enhances (mutably) this entity fetch provider with dot-notation property (and validates before that).
     *
     * @param dotNotationProperty
     * @return
     */
    private FetchProvider<T> enhanceWith(final String dotNotationProperty) {
        if ("".equals(dotNotationProperty)) { // represents entity-itself (it is permitted to use such notation)
            return this;
        } else {
            validate(dotNotationProperty);
            return enhanceWith0(dotNotationProperty, null);
        }
    }

    /**
     * Removes the dot-notation property (mutably) from this entity fetch provider, if it exists (validates before that).
     *
     * @param dotNotationProperty
     * @return
     */
    private FetchProvider<T> removeIfExists(final String dotNotationProperty) {
        validate(dotNotationProperty);
        return removeIfExists0(dotNotationProperty);
    }

    /**
     * Enhances (mutably) this entity fetch provider with entity-typed dot-notation property provider (and validates before that).
     *
     * @param dotNotationProperty
     * @return
     */
    private FetchProvider<T> enhanceWith(final String dotNotationProperty, final FetchProvider<AbstractEntity<?>> propertyStrategy) {
        validateEntityTyped(dotNotationProperty);
        return enhanceWith0(dotNotationProperty, propertyStrategy);
    }

    /**
     * Enhances (mutably) this entity fetch provider with dot-notation property.
     *
     * @param dotNotationProperty
     * @param specifiedProvider
     *            -- if not <code>null</code> -- additional provider for the property to be merged with existing one, if <code>null</code> -- default provider should be created
     *            (entity-typed property or regular)
     * @return
     */
    private FetchProvider<T> enhanceWith0(final String dotNotationProperty, final FetchProvider<AbstractEntity<?>> propertyProvider) {
        if (PropertyTypeDeterminator.isDotNotation(dotNotationProperty)) {
            final Pair<String, String> firstAndRest = PropertyTypeDeterminator.firstAndRest(dotNotationProperty);
            final String firstName = firstAndRest.getKey();
            final String restDotNotation = firstAndRest.getValue();
            final boolean exists = propertyProviders.containsKey(firstName);
            if (exists) {
                propertyProviders.get(firstName).enhanceWith0(restDotNotation, propertyProvider);
            } else {
                final Class<?> firstType = PropertyTypeDeterminator.determinePropertyType(entityType, firstName);
                propertyProviders.put(firstName, createDefaultFetchProviderForEntityTypedProperty((Class<AbstractEntity<?>>) firstType).enhanceWith0(restDotNotation, propertyProvider));
            }
        } else {
            final boolean exists = propertyProviders.containsKey(dotNotationProperty);
            final Class<?> propertyType = PropertyTypeDeterminator.determinePropertyType(entityType, dotNotationProperty);
            if (exists) {
                if (EntityUtils.isEntityType(propertyType) && propertyProvider != null) {
                    final FetchProvider<AbstractEntity<?>> merged = (FetchProvider<AbstractEntity<?>>) propertyProviders.get(dotNotationProperty).with(propertyProvider);
                    propertyProviders.put(dotNotationProperty, merged);
                } // else -- regular property or entity-typed
            } else {
                if (EntityUtils.isEntityType(propertyType)) { // entity-typed
                    propertyProviders.put(dotNotationProperty, propertyProvider == null ? createDefaultFetchProviderForEntityTypedProperty((Class<AbstractEntity<?>>) propertyType) : propertyProvider.copy());
                } else { // regular
                    propertyProviders.put(dotNotationProperty, null);
                }
            }
        }
        return this;
    }

    private static FetchProvider<AbstractEntity<?>> createDefaultFetchProviderForEntityTypedProperty(final Class<AbstractEntity<?>> propertyType) {
        return new FetchProvider<AbstractEntity<?>>(propertyType, true);
    }

    /**
     * Removes the dot-notation property (mutably) from this entity fetch provider, if it exists. Does nothing if the property was not included.
     *
     * @param dotNotationProperty
     * @return
     */
    private FetchProvider<T> removeIfExists0(final String dotNotationProperty) {
        if (PropertyTypeDeterminator.isDotNotation(dotNotationProperty)) {
            final Pair<String, String> firstAndRest = PropertyTypeDeterminator.firstAndRest(dotNotationProperty);
            final String firstName = firstAndRest.getKey();
            final String restDotNotation = firstAndRest.getValue();
            final boolean exists = propertyProviders.containsKey(firstName);
            if (exists) {
                propertyProviders.get(firstName).removeIfExists0(restDotNotation);
            }
        } else {
            final boolean exists = propertyProviders.containsKey(dotNotationProperty);
            if (exists) {
                propertyProviders.remove(dotNotationProperty);
            }
        }
        return this;
    }

    private LinkedHashMap<String, FetchProvider<AbstractEntity<?>>> propertyProviders() {
        return propertyProviders;
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
        // need to exclude all crit-only properties from fetch model!
        final FetchProvider<T> providerWithoutCritOnlyProps = excludeCritOnlyProps(this);
        fetch<T> fetchModel = withKeyAndDesc ? EntityQueryUtils.fetchKeyAndDescOnly(providerWithoutCritOnlyProps.entityType) : EntityQueryUtils.fetchOnly(providerWithoutCritOnlyProps.entityType);
        for (final Map.Entry<String, FetchProvider<AbstractEntity<?>>> entry : providerWithoutCritOnlyProps.propertyProviders.entrySet()) {
            final FetchProvider<AbstractEntity<?>> propModel = entry.getValue();
            if (propModel == null) {
                fetchModel = fetchModel.with(entry.getKey());
            } else {
                fetchModel = fetchModel.with(entry.getKey(), propModel.fetchModel());
            }
        }
        return fetchModel;
    }

    /**
     * Excludes all crit-only properties from fetch provider, if any. Returns new instance.
     *
     * @param fetchProvider
     * @return
     */
    private FetchProvider<T> excludeCritOnlyProps(final FetchProvider<T> fetchProvider) {
        final List<Field> critOnlyFields = Finder.findProperties(entityType, CritOnly.class);
        final List<String> critOnlyProps = critOnlyFields.stream().map(field -> field.getName()).collect(Collectors.toList());
        return critOnlyProps.size() > 0 ? (FetchProvider<T>) fetchProvider.without(critOnlyProps.get(0), critOnlyProps.subList(1, critOnlyProps.size()).toArray(new String[0])) : fetchProvider;
    }

    @Override
    public Set<String> allProperties() {
        final Set<String> allProperties = new LinkedHashSet<>();
        for (final Map.Entry<String, FetchProvider<AbstractEntity<?>>> entry : propertyProviders.entrySet()) {
            allProperties.add(entry.getKey());
            if (entry.getValue() != null) {
                allProperties.addAll(prepend(entry.getKey(), entry.getValue().allProperties()));
            }
        }
        return allProperties;
    }

    private static Set<String> prepend(final String prefix, final Set<String> props) {
        final LinkedHashSet<String> newProps = new LinkedHashSet<>();
        for (final String prop : props) {
            newProps.add(prefix + "." + prop);
        }
        return newProps;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((entityType == null) ? 0 : entityType.hashCode());
        result = prime * result + (withKeyAndDesc ? 1231 : 1237);
        result = prime * result + ((propertyProviders == null) ? 0 : propertyProviders.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FetchProvider other = (FetchProvider) obj;
        if (entityType == null) {
            if (other.entityType != null) {
                return false;
            }
        } else if (!entityType.equals(other.entityType)) {
            return false;
        }
        if (withKeyAndDesc != other.withKeyAndDesc) {
            return false;
        }
        if (propertyProviders == null) {
            if (other.propertyProviders != null) {
                return false;
            }
        } else if (!propertyProviders.equals(other.propertyProviders)) {
            return false;
        }
        return true;
    }

}
