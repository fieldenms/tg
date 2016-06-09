package ua.com.fielden.platform.entity.fetch;

import java.util.Set;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.fetch;

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
     * @param dotNotationProperties
     *            -- the name of properties ("dot-notation" syntax)
     *
     * @return new immutable {@link IFetchProvider} with included property(-ies)
     */
    IFetchProvider<T> with(final Set<String> dotNotationProperties);

    /**
     * Includes the property(-ies) into {@link IFetchProvider} (if it was not included already). If the property is of entity type (or collection of entities) then it will use
     * default {@link IFetchProvider} (with ID and version only).
     *
     * @param dotNotationProperty
     *            -- the name of the property ("dot-notation" syntax)
     * @param otherDotNotationProperties
     *            -- the name of other properties ("dot-notation" syntax)
     *
     * @return new immutable {@link IFetchProvider} with included property(-ies)
     */
    IFetchProvider<T> with(final String dotNotationProperty, final String... otherDotNotationProperties);

    /**
     * Merges the entity-typed property fetch provider into this {@link IFetchProvider}. If the property has had the fetch provider before -- it will be merged with new one
     * (union).
     *
     * @param dotNotationProperty
     *            -- the name of the property ("dot-notation" syntax)
     * @param propertyFetchProvider
     *            -- the new fetch provider for entity-typed property (maybe additional to existing)
     *
     * @return new immutable {@link IFetchProvider} with merged property fetch provider
     */
    <M extends AbstractEntity<?>> IFetchProvider<T> with(final String dotNotationProperty, final IFetchProvider<M> propertyFetchProvider);

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
     * @param dotNotationProperty
     *            -- the name of the property ("dot-notation" syntax)
     * @throws IllegalArgumentException
     *             -- if <code>dotNotationProperty</code> is not entity-typed property
     * @throws IllegalStateException
     *             -- if <code>dotNotationProperty</code> should not be fetched (as it is defined in this entity fetch strategy)
     * @return
     */
    <M extends AbstractEntity<?>> IFetchProvider<M> fetchFor(final String dotNotationProperty) throws IllegalArgumentException, IllegalStateException;

    /**
     * Returns <code>true</code> if the property should be fetched by this {@link IFetchProvider}, <code>false</code> otherwise.
     *
     * @param dotNotationProperty
     *            -- the name of the property ("dot-notation" syntax)
     * @return
     */
    boolean shouldFetch(final String dotNotationProperty);

    /**
     * Returns a flat representation (in a form of set) of all properties, that should be fetched.
     *
     * @return
     */
    Set<String> allProperties();

    /**
     * Excludes the property(-ies) from {@link IFetchProvider} (if they were included before that).
     *
     * @param dotNotationProperty
     *            -- the name of the property ("dot-notation" syntax)
     * @param otherDotNotationProperties
     *            -- the name of other properties ("dot-notation" syntax)
     *
     * @return new immutable {@link IFetchProvider} without specified property(-ies)
     */
    IFetchProvider<T> without(final String dotNotationProperty, final String... otherDotNotationProperties);

    <V extends AbstractEntity<?>> IFetchProvider<V> copy(final Class<V> managedType);
    
    /**
     * Returns <code>true</code> if this {@link IFetchProvider} requires instrumented instances, <code>false</code> otherwise.
     * 
     * @return
     */
    boolean instrumented();
}
