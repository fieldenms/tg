package ua.com.fielden.platform.entity.fetch;

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
public interface IEntityFetchStrategy<T extends AbstractEntity<?>> {
    /**
     * Returns corresponding fetch model for retrieving entities with this entity fetch strategy.
     *
     * @return
     */
    fetch<T> fetchModel();

    ////////////////////////// CONSTRUCTION //////////////////////////

    /**
     * Includes the property(-ies) into entity fetch strategy (if it was not included already). If the property is of entity type (or collection of entities) then it will use
     * default entity fetch strategy (with ID and version only).
     *
     * @param dotNotationProperty
     *            -- the name of the property ("dot-notation" syntax)
     * @param otherDotNotationProperties
     *            -- the name of other properties ("dot-notation" syntax)
     *
     * @return new immutable {@link IEntityFetchStrategy} with included property
     */
    IEntityFetchStrategy<T> with(final String dotNotationProperty, final String... otherDotNotationProperties);

    /**
     * Merges the entity-typed property fetch strategy into this entity fetch strategy. If the property has the fetch strategy before -- it will be merged with new one (union).
     *
     * @param dotNotationProperty
     *            -- the name of the property ("dot-notation" syntax)
     * @param propertyFetchStrategy
     *            -- the strategy for entity-typed property (maybe additional to existing)
     *
     * @return new immutable {@link IEntityFetchStrategy} with included property
     */
    <M extends AbstractEntity<?>> IEntityFetchStrategy<T> with(final String dotNotationProperty, final IEntityFetchStrategy<M> propertyFetchStrategy);

    /**
     * Creates the union of entity fetch strategy with <code>otherStrategy</code>.
     *
     * @param otherStrategy
     *            -- other fetch strategy for the same entity type
     *
     * @return new immutable {@link IEntityFetchStrategy} as a result of union
     */
    IEntityFetchStrategy<T> with(final IEntityFetchStrategy<T> otherStrategy);

    ////////////////////////// QUERY THE STATE //////////////////////////
    /**
     * Returns the fetch strategy for the entity-typed property.
     *
     * @param dotNotationProperty
     *            -- the name of the property ("dot-notation" syntax)
     * @throws IllegalArgumentException
     *             -- if <code>dotNotationProperty</code> is not entity-typed property
     * @throws IllegalStateException
     *             -- if <code>dotNotationProperty</code> should not be fetched (as it is defined in this entity fetch strategy)
     * @return
     */
    <M extends AbstractEntity<?>> IEntityFetchStrategy<M> strategyFor(final String dotNotationProperty) throws IllegalArgumentException, IllegalStateException;

    /**
     * Returns the fetch strategy for the entity-typed property.
     *
     * @param dotNotationProperty
     *            -- the name of the property ("dot-notation" syntax)
     * @throws IllegalArgumentException
     *             -- if <code>dotNotationProperty</code> is not entity-typed
     * @throws IllegalStateException
     *             -- if <code>dotNotationProperty</code> is not fetched
     * @return
     */
    boolean shouldFetch(final String dotNotationProperty);

}
