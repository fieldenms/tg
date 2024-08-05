package ua.com.fielden.platform.entity.fetch;

import java.util.Set;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.fluent.fetch.FetchCategory;

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
    IFetchProvider<T> with(final Set<? extends CharSequence> dotNotationProperties);

    /**
     * Includes the property(-ies) into {@link IFetchProvider} (if it was not included already). If the property is of entity type (or collection of entities) then it will use
     * default {@link IFetchProvider} (with ID and version only).
     *
     * @param dotNotationProperty -- name of the property ("dot-notation" syntax)
     * @param otherDotNotationProperties -- names of other properties ("dot-notation" syntax)
     *
     * @return new immutable {@link IFetchProvider} with included property(-ies)
     */
    IFetchProvider<T> with(final CharSequence dotNotationProperty, final CharSequence... otherDotNotationProperties);

    /**
     * Merges the entity-typed property fetch provider into this {@link IFetchProvider}. If the property has had the fetch provider before -- it will be merged with new one
     * (union).
     *
     * @param dotNotationProperty -- the name of the property ("dot-notation" syntax)
     * @param propertyFetchProvider -- the new fetch provider for entity-typed property (maybe additional to existing)
     *
     * @return new immutable {@link IFetchProvider} with merged property fetch provider
     */
    <M extends AbstractEntity<?>> IFetchProvider<T> with(final CharSequence dotNotationProperty, final IFetchProvider<M> propertyFetchProvider);

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
    <M extends AbstractEntity<?>> IFetchProvider<M> fetchFor(final CharSequence dotNotationProperty) throws IllegalArgumentException, IllegalStateException;

    /**
     * Returns {@code true} if the property should be fetched by this {@link IFetchProvider}, {@code false} otherwise.
     *
     * @param dotNotationProperty -- the name of the property ("dot-notation" syntax)
     * @return
     */
    boolean shouldFetch(final CharSequence dotNotationProperty);

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
    IFetchProvider<T> without(final CharSequence dotNotationProperty, final CharSequence... otherDotNotationProperties);

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
    IFetchProvider<T> addPropWithKeys(final CharSequence dotNotationProperty, final boolean withDesc);

}
