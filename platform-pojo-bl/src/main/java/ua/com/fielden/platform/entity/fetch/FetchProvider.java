package ua.com.fielden.platform.entity.fetch;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.AbstractEntity.VERSION;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchKeyAndDescOnly;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchKeyAndDescOnlyAndInstrument;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchNone;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchNoneAndInstrument;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchOnly;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchOnlyAndInstrument;
import static ua.com.fielden.platform.entity.query.fluent.fetch.FetchCategory.ID_AND_VERSION;
import static ua.com.fielden.platform.entity.query.fluent.fetch.FetchCategory.KEY_AND_DESC;
import static ua.com.fielden.platform.entity.query.fluent.fetch.FetchCategory.NONE;
import static ua.com.fielden.platform.reflection.Finder.getKeyMembers;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.determinePropertyType;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.firstAndRest;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.isDotNotation;
import static ua.com.fielden.platform.utils.EntityUtils.isEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isPersistedEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isSyntheticBasedOnPersistentEntityType;
import static ua.com.fielden.platform.utils.Pair.pair;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CritOnly;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.fluent.fetch.FetchCategory;
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
    /**
     * EQL {@link FetchCategory} to be used when converting this fetch provider to EQL fetch model. At this stage only three categories are supported:
     * {@link FetchCategory#KEY_AND_DESC}, {@link FetchCategory#ID_AND_VERSION} and {@link FetchCategory#NONE}.
     * <p>
     * Important: please note that {@link FetchCategory#KEY_AND_DESC} (see EntityRetrievalModel.includeKeyAndDescOnly) and {@link FetchCategory#ID_AND_VERSION} (see EntityRetrievalModel.includeIdAndVersionOnly) 
     * categories include many more properties then their names state. Please, use {@link FetchCategory#NONE} in combination with methods {@link #with(String, IFetchProvider)} if more granular approach is needed.
     */
    private final FetchCategory fetchCategory;
    private final boolean instrumented;

    /**
     * Constructs empty {@link FetchProvider}.
     *
     * @param entityType
     * @param fetchCategory -- EQL fetch category
     * @param instrumented -- indicates whether fetched instances should be instrumented
     *
     */
    FetchProvider(final Class<T> entityType, final FetchCategory fetchCategory, final boolean instrumented) {
        this(entityType, new LinkedHashMap<>(), fetchCategory, instrumented);
    }

    /**
     * Full {@link FetchProvider}'s constructor for internal purposes.
     * 
     * @param entityType
     * @param propertyProviders -- fetch providers for properties mapped by names (or <code>null</code>s for regular props)
     * @param fetchCategory -- EQL fetch category
     * @param instrumented -- indicates whether fetched instances should be instrumented
     */
    private FetchProvider(final Class<T> entityType, final LinkedHashMap<String, FetchProvider<AbstractEntity<?>>> propertyProviders, final FetchCategory fetchCategory, final boolean instrumented) {
        this.entityType = entityType;
        this.propertyProviders = propertyProviders;
        this.fetchCategory = fetchCategory;
        this.instrumented = instrumented;
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

        if (!AnnotationReflector.isAnnotationPresentInHierarchy(IsProperty.class, entityType, dotNotationProperty) && !ID.equals(dotNotationProperty) && !VERSION.equals(dotNotationProperty)) { // ID and VERSION do not have @IsProperty but can be added explicitly when FetchCategory.NONE is used
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
        final FetchProvider<T> copy = this.copy(fetchCategory, instrumented);
        copy.enhanceWith(dotNotationProperty, (FetchProvider<AbstractEntity<?>>) propertyFetchProvider);
        return copy;
    }
    
    /**
     * Merges two fetch categories taking into account that NONE < ID_AND_VERSION < KEY_AND_DESC.
     * 
     * @param fetchCategory1
     * @param fetchCategory2
     * @return
     */
    private static FetchCategory mergeFetchCategories(final FetchCategory fetchCategory1, final FetchCategory fetchCategory2) {
        if (KEY_AND_DESC == fetchCategory1 || KEY_AND_DESC == fetchCategory2) {
            return KEY_AND_DESC;
        } else if (ID_AND_VERSION == fetchCategory1 || ID_AND_VERSION == fetchCategory2) {
            return ID_AND_VERSION;
        } else {
            return NONE;
        }
    }

    @Override
    public IFetchProvider<T> with(final IFetchProvider<T> otherStrategy) {
        validateTypes(this, (FetchProvider<T>) otherStrategy);
        final FetchProvider<T> that = (FetchProvider<T>) otherStrategy;

        final FetchCategory mergedFetchCategory = mergeFetchCategories(this.fetchCategory, that.fetchCategory);
        final boolean mergedInstrumented = this.instrumented || that.instrumented;

        final FetchProvider<T> copy = this.copy(mergedFetchCategory, mergedInstrumented);
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
        return copy(fetchCategory, instrumented);
    }

    /**
     * Creates exact copy of entity fetch provider.
     *
     * @return
     */
    private FetchProvider<T> copy(final FetchCategory fetchCategory, final boolean instrumented) {
        return new FetchProvider<>(entityType, copyPropertyProviders(), fetchCategory, instrumented);
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
        return new FetchProvider<>(managedType, copyPropertyProviders(), fetchCategory, instrumented);
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
        if (isDotNotation(dotNotationProperty)) {
            final Pair<String, String> firstAndRest = firstAndRest(dotNotationProperty);
            final String firstName = firstAndRest.getKey();
            final String restDotNotation = firstAndRest.getValue();
            final boolean exists = propertyProviders.containsKey(firstName);
            if (exists) {
                propertyProviders.get(firstName).enhanceWith0(restDotNotation, propertyProvider);
            } else {
                final Class<?> firstType = determinePropertyType(entityType, firstName);
                propertyProviders.put(firstName, createDefaultFetchProviderForEntityTypedProperty((Class<AbstractEntity<?>>) firstType, defaultChildFetchCategory()).enhanceWith0(restDotNotation, propertyProvider));
            }
        } else {
            final boolean exists = propertyProviders.containsKey(dotNotationProperty);
            final Class<?> propertyType = determinePropertyType(entityType, dotNotationProperty);
            if (exists) {
                if (isEntityType(propertyType) && propertyProvider != null) {
                    final FetchProvider<AbstractEntity<?>> merged = (FetchProvider<AbstractEntity<?>>) propertyProviders.get(dotNotationProperty).with(propertyProvider);
                    propertyProviders.put(dotNotationProperty, merged);
                } // else -- regular property or entity-typed
            } else {
                if (isEntityType(propertyType)) { // entity-typed
                    propertyProviders.put(dotNotationProperty, propertyProvider == null ? createDefaultFetchProviderForEntityTypedProperty((Class<AbstractEntity<?>>) propertyType, defaultChildFetchCategory()) : propertyProvider.copy());
                } else { // regular
                    propertyProviders.put(dotNotationProperty, null);
                }
            }
        }
        return this;
    }
    
    /**
     * In most cases, {@link FetchProvider} is used in a way where some properties are added using constructs without explicit indication of property's own fetch provider.
     * This means that methods {@link #with(String, String...)} and {@link #with(Set)} are used and method {@link #with(String, IFetchProvider)} is not used.
     * <p>
     * This situation requires some reasonable defaults for fetch category.
     * For most cases we use {@link FetchCategory#KEY_AND_DESC} to fetch all the necessary information.
     * <p>
     * The exception from that rule is when parent has {@link FetchCategory#NONE} category: we use the same lean category for child fetch providers including those that are constructed
     * on dot-notated pathway. This ensures the leanest possible tree children when using methods {@link #with(String, String...)} and {@link #with(Set)}. However, there is possibility
     * to provide "thick" child even for lean parent: just use explicit {@link #with(String, IFetchProvider)} method.
     * 
     * @return
     */
    private FetchCategory defaultChildFetchCategory() {
        return NONE == fetchCategory ? NONE : KEY_AND_DESC;
    }

    /**
     * Creates default fetch provider for entity-typed property with type <code>propertyType</code> and concrete <code>fetchCategory</code>.
     * 
     * @param propertyType
     * @param fetchCategory
     * @return
     */
    private static FetchProvider<AbstractEntity<?>> createDefaultFetchProviderForEntityTypedProperty(final Class<AbstractEntity<?>> propertyType, final FetchCategory fetchCategory) {
        // default fetch provider for entity-typed property should be without instrumentation
        return new FetchProvider<>(propertyType, fetchCategory, false);
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
        final Class<T> entityType = providerWithoutCritOnlyProps.entityType;
        fetch<T> fetchModel = instrumented ?
                (KEY_AND_DESC == fetchCategory ? fetchKeyAndDescOnlyAndInstrument(entityType) : (ID_AND_VERSION == fetchCategory ? fetchOnlyAndInstrument(entityType) : fetchNoneAndInstrument(entityType))) :
                (KEY_AND_DESC == fetchCategory ? fetchKeyAndDescOnly(entityType)              : (ID_AND_VERSION == fetchCategory ? fetchOnly(entityType)              : fetchNone(entityType)));
        for (final Map.Entry<String, FetchProvider<AbstractEntity<?>>> entry : providerWithoutCritOnlyProps.propertyProviders.entrySet()) {
            final FetchProvider<AbstractEntity<?>> propModel = entry.getValue();
            if (propModel == null || PropertyDescriptor.class.isAssignableFrom(determinePropertyType(entityType, entry.getKey()))) {
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
        result = prime * result + ((fetchCategory == null) ? 0 : fetchCategory.hashCode());
        result = prime * result + (instrumented ? 1231 : 1237);
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
        if (fetchCategory != other.fetchCategory) {
            return false;
        }
        if (instrumented != other.instrumented) {
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

    @Override
    public String toString() {
        return " " + entityType.getSimpleName() + " {\n"
                + "    instrumented = " + instrumented + "\n"
                + "    fetchCategory = " + fetchCategory + "\n"
                + "    props = " + propertyProviders.toString() + "\n"
                + "}\n\n";
    }

    @Override
    public boolean instrumented() {
        return instrumented;
    }
    
    /**
     * Adds (mutably) key properties to the fetch provider of <code>dotNotationProperty</code> in case it is entity-typed.
     * <p>
     * This method is based on a premise that property already has its provider (with NONE fetch category) if it is entity-typed.
     * 
     * @param dotNotationProperty
     * @return
     */
    @Override
    public FetchProvider<T> addKeysTo(final String dotNotationProperty) {
        if (isDotNotation(dotNotationProperty) || !"".equals(dotNotationProperty)) { // is dot-notation or simple property (not "" aka 'this')
            final Pair<String, String> firstAndRest = isDotNotation(dotNotationProperty) ? firstAndRest(dotNotationProperty) : pair(dotNotationProperty, "");
            final String firstName = firstAndRest.getKey();
            final String restDotNotation = firstAndRest.getValue();
            final boolean shouldFetch = propertyProviders.containsKey(firstName);
            if (shouldFetch) {
                final FetchProvider<AbstractEntity<?>> provider = propertyProviders.get(firstName);
                if (provider == null) {
                    if (isDotNotation(dotNotationProperty)) {
                        throw new FetchProviderException(format("Property provider for %s is somehow empty.", firstName));
                    }
                    // otherwise, nothing to add here (regular property)
                    return this;
                } else if (NONE != provider.fetchCategory) {
                    throw new FetchProviderException(format("Property provider for %s has fetch category other than NONE.", firstName));
                } else {
                    provider.addKeysTo(restDotNotation);
                    return this;
                }
            } else {
                throw new FetchProviderException(format("Property %s was not included into this provider.", firstName));
            }
        } else { // dotNotationProperty = "" (aka 'this')
            if (NONE != fetchCategory) {
                throw new FetchProviderException("Root provider has fetch category other than NONE.");
            } 
            
            // only composite parts for composite entity and only 'key' otherwise
            final List<String> keyMemberNames = getKeyMembers(entityType).stream().map(Field::getName).collect(toList());
            for (final String keyMemberName: keyMemberNames) {
                enhanceWith(keyMemberName);
                addKeysTo(keyMemberName);
            }
            
            if (isPersistedEntityType(entityType)) {
                // ID is needed at this stage to perform query -- that's why we must include it (later this property can be trimmed, for example during serialisation)
                enhanceWith(ID);
                // VERSION is needed not to convert entity values to IdOnlyProxies -- that's why we must include it (later this property can be trimmed, for example during serialisation)
                enhanceWith(VERSION);
            } else if (isSyntheticBasedOnPersistentEntityType(entityType)) {
                // ID is needed at this stage to perform query -- that's why we must include it (later this property can be trimmed, for example during serialisation)
                enhanceWith(ID);
            }
            return this;
        }
    }
    
}