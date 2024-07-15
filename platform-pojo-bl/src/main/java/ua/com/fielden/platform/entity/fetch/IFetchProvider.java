package ua.com.fielden.platform.entity.fetch;

import static java.util.stream.Collectors.toSet;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Stream;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.fluent.fetch.FetchCategory;
import ua.com.fielden.platform.processors.metamodel.IConvertableToPath;

/**
 * Defines a contract for strategy of entity fullness. This contract controls which branches (property sub-trees) of the entity tree is crucial for some domain process or entity UI
 * representation.
 * <p>
 * Note: all methods throw {@link IllegalArgumentException} in case of malformed <code>dotNotationProperties</code> (if the property does not exist etc.).
 *
 * @author TG Team
 *
 */
public interface IFetchProvider<T extends AbstractEntity<?>> {
    /**
     * Returns corresponding fetch model for retrieving entities with this {@link IFetchProvider}.
     *
     * @return
     */
    fetch<T> fetchModel();

    ////////////////////////// CONSTRUCTION //////////////////////////

    /**
     * Includes the property(-ies) into {@link IFetchProvider} (if it was not included already). If the property is of entity type (or collection of entities) then it will use
     * default {@link IFetchProvider} (with ID and version only).
     *
     * @param dotNotationProperties -- names of properties ("dot-notation" syntax)
     *
     * @return new immutable {@link IFetchProvider} with included property(-ies)
     */
    IFetchProvider<T> with(final Set<String> dotNotationProperties);

    /**
     * Includes the property(-ies) into {@link IFetchProvider} (if it was not included already). If the property is of entity type (or collection of entities) then it will use
     * default {@link IFetchProvider} (with ID and version only).
     *
     * @param dotNotationProperties -- names of properties ("dot-notation" syntax from meta-models e.g. Station_.zone().sector().division())
     *
     * @return new immutable {@link IFetchProvider} with included property(-ies)
     */
    default IFetchProvider<T> with(final Collection<IConvertableToPath> dotNotationProperties) { // used Collection instead of Set to preserve 'with' overloaded name; this API (with collection of properties) is not used very often
        return with(dotNotationProperties.stream().map(IConvertableToPath::toPath).collect(toSet()));
    }

    /**
     * Includes the property(-ies) into {@link IFetchProvider} (if it was not included already). If the property is of entity type (or collection of entities) then it will use
     * default {@link IFetchProvider} (with ID and version only).
     *
     * @param dotNotationProperty -- name of the property ("dot-notation" syntax)
     * @param otherDotNotationProperties -- names of other properties ("dot-notation" syntax)
     *
     * @return new immutable {@link IFetchProvider} with included property(-ies)
     */
    IFetchProvider<T> with(final String dotNotationProperty, final String... otherDotNotationProperties);

    /**
     * Includes the property(-ies) into {@link IFetchProvider} (if it was not included already). If the property is of entity type (or collection of entities) then it will use
     * default {@link IFetchProvider} (with ID and version only).
     *
     * @param dotNotationProperty -- name of the property ("dot-notation" syntax from meta-models e.g. Station_.zone().sector().division())
     * @param otherDotNotationProperties -- names of other properties ("dot-notation" syntax from meta-models e.g. Station_.zone().sector().division())
     *
     * @return new immutable {@link IFetchProvider} with included property(-ies)
     */
    default IFetchProvider<T> with(final IConvertableToPath dotNotationProperty, final IConvertableToPath... otherDotNotationProperties) {
        return with(dotNotationProperty.toPath(), Stream.of(otherDotNotationProperties).map(IConvertableToPath::toPath).toArray(String[]::new));
    }

    /**
     * Merges the entity-typed property fetch provider into this {@link IFetchProvider}. If the property has had the fetch provider before -- it will be merged with new one
     * (union).
     *
     * @param dotNotationProperty -- the name of the property ("dot-notation" syntax)
     * @param propertyFetchProvider -- the new fetch provider for entity-typed property (maybe additional to existing)
     *
     * @return new immutable {@link IFetchProvider} with merged property fetch provider
     */
    <M extends AbstractEntity<?>> IFetchProvider<T> with(final String dotNotationProperty, final IFetchProvider<M> propertyFetchProvider);

    /**
     * Merges the entity-typed property fetch provider into this {@link IFetchProvider}. If the property has had the fetch provider before -- it will be merged with new one
     * (union).
     *
     * @param dotNotationProperty -- the name of the property ("dot-notation" syntax from meta-models e.g. Station_.zone().sector().division())
     * @param propertyFetchProvider -- the new fetch provider for entity-typed property (maybe additional to existing)
     *
     * @return new immutable {@link IFetchProvider} with merged property fetch provider
     */
    default <M extends AbstractEntity<?>> IFetchProvider<T> with(final IConvertableToPath dotNotationProperty, final IFetchProvider<M> propertyFetchProvider) {
        return with(dotNotationProperty.toPath(), propertyFetchProvider);
    }

    /**
     * Creates the union of this fetch provider with <code>otherFetchProvider</code>.
     *
     * @param otherFetchProvider
     *            -- other fetch provider for the same entity type
     *
     * @throws IllegalArgumentException
     *             -- if <code>otherFetchProvider</code> has different entity type
     *
     * @return new immutable {@link IFetchProvider} as a result of union
     */
    IFetchProvider<T> with(final IFetchProvider<T> otherFetchProvider);

    ////////////////////////// QUERY THE STATE //////////////////////////
    /**
     * Returns the fetch provider for the entity-typed property.
     *
     * @param dotNotationProperty -- name of the property ("dot-notation" syntax)
     * @throws IllegalArgumentException -- if {@code dotNotationProperty} is not entity-typed property
     * @throws IllegalStateException -- if {@code dotNotationProperty} should not be fetched (as it is defined in this entity fetch strategy)
     * @return
     */
    <M extends AbstractEntity<?>> IFetchProvider<M> fetchFor(final String dotNotationProperty) throws IllegalArgumentException, IllegalStateException;

    /**
     * Returns the fetch provider for the entity-typed property.
     *
     * @param dotNotationProperty -- name of the property ("dot-notation" syntax from meta-models e.g. Station_.zone().sector().division())
     * @throws IllegalArgumentException -- if {@code dotNotationProperty} is not entity-typed property
     * @throws IllegalStateException -- if {@code dotNotationProperty} should not be fetched (as it is defined in this entity fetch strategy)
     * @return
     */
    default <M extends AbstractEntity<?>> IFetchProvider<M> fetchFor(final IConvertableToPath dotNotationProperty) throws IllegalArgumentException, IllegalStateException {
        return fetchFor(dotNotationProperty.toPath());
    }

    /**
     * Returns {@code true} if the property should be fetched by this {@link IFetchProvider}, {@code false} otherwise.
     *
     * @param dotNotationProperty -- the name of the property ("dot-notation" syntax)
     * @return
     */
    boolean shouldFetch(final String dotNotationProperty);

    /**
     * Returns {@code true} if the property should be fetched by this {@link IFetchProvider}, {@code false} otherwise.
     *
     * @param dotNotationProperty -- the name of the property ("dot-notation" syntax from meta-models e.g. Station_.zone().sector().division())
     * @return
     */
    default boolean shouldFetch(final IConvertableToPath dotNotationProperty) {
        return shouldFetch(dotNotationProperty.toPath());
    }

    /**
     * Returns a flat representation (in a form of set) of all properties, that should be fetched.
     *
     * @return
     */
    Set<String> allProperties();

    /**
     * Excludes the property(-ies) from {@link IFetchProvider} (if they were included before that).
     *
     * @param dotNotationProperty -- name of the property ("dot-notation" syntax)
     * @param otherDotNotationProperties -- names of other properties ("dot-notation" syntax)
     *
     * @return new immutable {@link IFetchProvider} without specified property(-ies)
     */
    IFetchProvider<T> without(final String dotNotationProperty, final String... otherDotNotationProperties);

    /**
     * Excludes the property(-ies) from {@link IFetchProvider} (if they were included before that).
     *
     * @param dotNotationProperty -- name of the property ("dot-notation" syntax from meta-models e.g. Station_.zone().sector().division())
     * @param otherDotNotationProperties -- names of other properties ("dot-notation" syntax from meta-models e.g. Station_.zone().sector().division())
     *
     * @return new immutable {@link IFetchProvider} without specified property(-ies)
     */
    default IFetchProvider<T> without(final IConvertableToPath dotNotationProperty, final IConvertableToPath... otherDotNotationProperties) {
        return without(dotNotationProperty.toPath(), Stream.of(otherDotNotationProperties).map(IConvertableToPath::toPath).toArray(String[]::new));
    }

    /**
     * Copies {@link IFetchProvider} with new {@code managedType}.
     * <p>
     * The new type must be consistent with original type. I.e. it can be generated version of the same type (with calculated properties such as totals).
     * 
     * @param <V>
     * @param managedType
     * @return
     */
    <V extends AbstractEntity<?>> IFetchProvider<V> copy(final Class<V> managedType);

    /**
     * Returns <code>true</code> if this {@link IFetchProvider} requires instrumented instances, <code>false</code> otherwise.
     * 
     * @return
     */
    boolean instrumented();

    /**
     * Returns entity type behind this {@link IFetchProvider}.
     */
    Class<T> entityType();

    /**
     * Adds property (mutably) to the fetch provider and its key sub-properties in case it is entity-typed.
     * <p>
     * This method uses lean {@link FetchCategory#NONE} on dot-notation pathways.
     * 
     * @param dotNotationProperty
     * @param withDesc -- specifies whether {@link AbstractEntity#DESC} property needs to be added, iff {@code dotNotationProperty} is entity-typed and has {@link AbstractEntity#DESC} property
     * @return
     */
    IFetchProvider<T> addPropWithKeys(final String dotNotationProperty, final boolean withDesc);

    /**
     * Adds property (mutably) to the fetch provider and its key sub-properties in case it is entity-typed.
     * <p>
     * This method uses lean {@link FetchCategory#NONE} on dot-notation pathways.
     * 
     * @param dotNotationProperty
     * @param withDesc -- specifies whether {@link AbstractEntity#DESC} property needs to be added, iff {@code dotNotationProperty} is entity-typed and has {@link AbstractEntity#DESC} property
     * @return
     */
    default IFetchProvider<T> addPropWithKeys(final IConvertableToPath dotNotationProperty, final boolean withDesc) {
        return addPropWithKeys(dotNotationProperty.toPath(), withDesc);
    }

}