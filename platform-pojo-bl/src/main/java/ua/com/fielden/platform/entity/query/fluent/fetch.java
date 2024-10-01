package ua.com.fielden.platform.entity.query.fluent;

import static java.lang.String.format;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.AbstractEntity.VERSION;
import static ua.com.fielden.platform.entity.query.fluent.fetch.FetchCategory.ALL;
import static ua.com.fielden.platform.entity.query.fluent.fetch.FetchCategory.DEFAULT;
import static ua.com.fielden.platform.entity.query.fluent.fetch.FetchCategory.ID_AND_VERSION;
import static ua.com.fielden.platform.entity.query.fluent.fetch.FetchCategory.ID_ONLY;
import static ua.com.fielden.platform.entity.query.fluent.fetch.FetchCategory.KEY_AND_DESC;
import static ua.com.fielden.platform.reflection.Finder.isPropertyPresent;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.determinePropertyType;

import java.util.*;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.exceptions.EqlException;
import ua.com.fielden.platform.utils.CollectionUtil;

/**
 * Represents an entity graph that describes the shape of an entity to be fetched.
 *
 * @param <T> entity type
 * @see FetchCategory
 * @see ua.com.fielden.platform.entity.query.IRetrievalModel
 */
public class fetch<T extends AbstractEntity<?>> {
    public static final String ERR_MISMATCH_BETWEEN_PROPERTY_AND_FETCH_MODEL_TYPES = "Mismatch between actual type [%s] of property [%s] in entity type [%s] and its fetch model type [%s]!";

    /**
     * Standard fetch categories.
     */
    public enum FetchCategory {
        /**
         *
         * Equivalent to {@link #ALL} but also includes calculated properties.
         */
        ALL_INCL_CALC,
        /**
         * <ul>
         *   <li> collectional properties are excluded;
         *   <li> non-retrievable properties are excluded;
         *   <li> calculated properties are excluded (unless they have a component type);
         *   <li> everything else is included.
         * </ul>
         */
        ALL,
        /**
         * Equivalent to {@link #ALL} but with narrower sub-fetch models - only simple keys and key members may have
         * a sub-fetch model other than {@link #ID_ONLY}.
         */
        DEFAULT,
        /**
         * <ul>
         *   <li> if entity is persistent, includes {@link #ID_AND_VERSION};
         *   <li> if entity is synthetic-based-on-persistent, includes {@code id};
         *   <li> if entity has property {@code desc}, includes it;
         *   <li> includes {@code key};
         * </ul>
         */
        KEY_AND_DESC,
        /**
         * A slightly broader fetch model than {@link #ID_ONLY}.
         * If entity's nature is
         * <ul>
         *   <li> persistent
         *     <ul>
         *       <li> {@code id} and {@code version} are included;
         *       <li> if entity is activatable, {@code refCount} and {@code active} are included;
         *       <li> if entity has a group of "last updated by" properties (see {@link ua.com.fielden.platform.entity.AbstractPersistentEntity}),
         *            they are included;
         *     </ul>
         *   <li> other
         *     <ul>
         *       <li> if entity has an entity-typed key, {@code id} is included (it is not clearly understood why, but,
         *            most likely, to support synthetic entities with entity-typed keys).
         *     </ul>
         * </ul>
         */
        ID_AND_VERSION,
        /**
         * Sole property {@code id} is included.
         */
        ID_ONLY,
        /**
         * No properties are included.
         */
        NONE
    }

    private final Class<T> entityType;
    private final Map<String, fetch<? extends AbstractEntity<?>>> includedPropsWithModels = new HashMap<>();
    private final Set<String> includedProps = new HashSet<>();
    private final Set<String> excludedProps = new HashSet<>();
    private final FetchCategory fetchCategory;
    private final boolean instrumented;

    /**
     * Used mainly for serialisation.
     */
    protected fetch() {
        this(null, null);
    }

    public fetch(final Class<T> entityType, final FetchCategory fetchCategory, final boolean instrumented) {
        this.entityType = entityType;
        this.fetchCategory = fetchCategory;
        this.instrumented = instrumented;
    }

    public fetch(final Class<T> entityType, final FetchCategory fetchCategory) {
        this(entityType, fetchCategory, false);
    }
    
    private void validate(final String propName) {
        checkForExistence(propName);
        checkForDuplicate(propName);
    }
    
    private void checkForDuplicate(final String propName) {
        if (includedPropsWithModels.containsKey(propName) || includedProps.contains(propName) || excludedProps.contains(propName)) {
            throw new IllegalArgumentException("Property [" + propName + "] is already present within fetch model!");
        }
    }
    
    private void checkForExistence(final String propName) {
        if (entityType != EntityAggregates.class &&
                !ID.equals(propName) &&
                !VERSION.equals(propName) &&
                !isPropertyPresent(entityType, propName)) {
            throw new IllegalArgumentException("Property [" + propName + "] is not present within [" + entityType.getSimpleName() + "] entity!");
        }
    }

    private static <T extends AbstractEntity<?>> fetch<T> copy(final fetch<T> fromFetch) {
        final fetch<T> result = new fetch<>(fromFetch.entityType, fromFetch.fetchCategory, fromFetch.isInstrumented());
        result.includedPropsWithModels.putAll(fromFetch.includedPropsWithModels);
        result.includedProps.addAll(fromFetch.includedProps);
        result.excludedProps.addAll(fromFetch.excludedProps);
        return result;
    }

    /**
     * Adds the property to this fetch model.
     * </p>
     * It is an error if the property is already included in or excluded from this fetch model.
     * It is an error if the property doesn't exist in the entity type associated with this fetch model.
     *
     * @param propName this could be the name of a:
     *                 primitive property (e.g. {@code desc}, {@code numberOfPages}),
     *                 entity property ({@code station}),
     *                 composite type property ({@code cost}, {@code cost.amount}),
     *                 union entity property ({@code location}, {@code location.workshop}),
     *                 collectional property ({@code slots}),
     *                 one-to-one association property ({@code financialDetails}).
     * @return a new fetch model that includes the given property
     */
    public fetch<T> with(final CharSequence propName) {
        validate(propName.toString());
        final fetch<T> result = copy(this);
        result.includedProps.add(propName.toString());
        return result;
    }

    /**
     * Adds all given properties to this fetch model.
     *
     * @return a new fetch model that includes the given properties
     * @see #with(CharSequence)
     */
    public fetch<T> with(final CharSequence propName, final CharSequence... propNames) {
        validate(propName.toString());
        for (final var name : propNames) {
            validate(name.toString());
        }
        final fetch<T> result = copy(this);
        result.includedProps.add(propName.toString());
        for (final var name : propNames) {
            result.includedProps.add(name.toString());
        }
        return result;
    }

    /**
     * Adds all given properties to this fetch model.
     *
     * @return a new fetch model that includes the given properties
     * @see #with(CharSequence)
     */
    public fetch<T> with(final Iterable<? extends CharSequence> propNames) {
        propNames.forEach(p -> validate(p.toString()));
        final fetch<T> result = copy(this);
        propNames.forEach(p -> result.includedProps.add(p.toString()));
        return result;
    }

    /**
     * Excludes the property from this fetch model.
     * <p>
     * It is an error if the property is already included in or excluded from this fetch model.
     * It is an error if the property doesn't exist in the entity type associated with this fetch model.
     *
     * @param propName this could be the name of a:
     *                 primitive property (e.g. {@code desc}, {@code numberOfPages}),
     *                 entity property ({@code station}),
     *                 composite type property ({@code cost}, {@code cost.amount}),
     *                 union entity property ({@code location}, {@code location.workshop}),
     *                 collectional property ({@code slots}),
     *                 one-to-one association property ({@code financialDetails}).
     * @return a new fetch model that excludes the given property
     */
    public fetch<T> without(final CharSequence propName) {
        validate(propName.toString());
        final fetch<T> result = copy(this);
        result.excludedProps.add(propName.toString());
        return result;
    }

    /**
     * Excludes all given properties from this fetch model.
     *
     * @return a new fetch model that excludes the given properties
     * @see #without(CharSequence)
     */
    public fetch<T> without(final CharSequence propName, final CharSequence... propNames) {
        validate(propName.toString());
        for (final var name : propNames) {
            validate(name.toString());
        }
        final fetch<T> result = copy(this);
        result.excludedProps.add(propName.toString());
        for (final var name : propNames) {
            result.excludedProps.add(name.toString());
        }
        return result;
    }

    /**
     * Excludes all given properties from this fetch model.
     *
     * @return a new fetch model that excludes the given properties
     * @see #without(CharSequence)
     */
    public fetch<T> without(final Iterable<? extends CharSequence> propNames) {
        propNames.forEach(p -> validate(p.toString()));
        final fetch<T> result = copy(this);
        propNames.forEach(p -> result.excludedProps.add(p.toString()));
        return result;
    }

    /**
     * Adds the property to this fetch model and associates the given fetch model with it.
     * <p>
     * It is an error if the property's type does not match the entity type associated with the given fetch model.
     * <p>
     * The resulting fetch model represents a graph that contains the given fetch model as a subgraph.
     *
     * @return a new fetch model that contains the given property
     */
    public fetch<T> with(final CharSequence propName, final fetch<? extends AbstractEntity<?>> fetchModel) {
        validate(propName.toString());
        // if the entityType is not an aggregate entity then we must validate that the type of propName and the type of fetchModel match
        if (entityType != EntityAggregates.class) {
            final Class<?> propType = determinePropertyType(entityType, propName);
            if (propType != fetchModel.entityType) {
                throw new EqlException(format(ERR_MISMATCH_BETWEEN_PROPERTY_AND_FETCH_MODEL_TYPES,
                                              propType, propName, entityType.getSimpleName(), fetchModel.getEntityType().getSimpleName()));
            }
        }

        final fetch<T> result = copy(this);
        result.includedPropsWithModels.put(propName.toString(), fetchModel);
        return result;
    }

    public Class<T> getEntityType() {
        return entityType;
    }

    public boolean isInstrumented() {
        return instrumented;
    }

    public Map<String, fetch<? extends AbstractEntity<?>>> getIncludedPropsWithModels() {
        return unmodifiableMap(includedPropsWithModels);
    }

    public Set<String> getIncludedProps() {
        return unmodifiableSet(includedProps);
    }

    public Set<String> getExcludedProps() {
        return unmodifiableSet(excludedProps);
    }

    public FetchCategory getFetchCategory() {
        return fetchCategory;
    }

    @Override
    public int hashCode() {
        return Objects.hash(entityType,
                            excludedProps,
                            fetchCategory,
                            includedProps,
                            includedPropsWithModels,
                            instrumented);
    }

    @Override
    public boolean equals(final Object obj) {
        return obj == this
               || obj instanceof fetch that
                  && fetchCategory == that.fetchCategory
                  && instrumented == that.instrumented
                  && Objects.equals(entityType, that.entityType)
                  && Objects.equals(excludedProps, that.excludedProps)
                  && Objects.equals(includedProps, that.includedProps)
                  && Objects.equals(includedPropsWithModels, that.includedPropsWithModels);
    }

    @Override
    public String toString() {
        return toString(1, "  ");
    }

    public String toString(final int level, final String indentElt) {
        final var sb = new StringBuilder();

        sb.append("fetch {\n");
        final var indent = indentElt.repeat(level);
        sb.append(indent).append("entity: ").append(entityType.getSimpleName()).append("\n");
        sb.append(indent).append("category: ").append(fetchCategory).append("\n");
        sb.append(indent).append("instrumented: ").append(instrumented).append("\n");
        if (!includedProps.isEmpty()) {
            sb.append(indent).append(format("included: [%s]", CollectionUtil.toString(includedProps, ","))).append('\n');
        }
        if (!excludedProps.isEmpty()) {
            sb.append(indent).append(format("excluded: [%s]", CollectionUtil.toString(excludedProps, ","))).append('\n');
        }
        if (!includedPropsWithModels.isEmpty()) {
            sb.append(indent).append("entities: {/n");
            final var entitiesIndent = " ".repeat(level + 1);
            includedPropsWithModels.forEach((name, model) -> sb.append(entitiesIndent).append("\"%s\": %s".formatted(name, model.toString(level + 2, indentElt))));
            sb.append('\n').append(indentElt).append("}\n");
        }
        sb.append(indentElt.repeat(level - 1)).append('}');

        return sb.toString();
    }

    private FetchCategory getMergedFetchCategory(final fetch<?> second) {
        if (fetchCategory == ALL || second.fetchCategory == ALL) {
            return ALL;
        }

        if (fetchCategory == DEFAULT || second.fetchCategory == DEFAULT) {
            return DEFAULT;
        }

        if (fetchCategory == KEY_AND_DESC || second.fetchCategory == KEY_AND_DESC) {
            return KEY_AND_DESC;
        }

        if (fetchCategory == ID_AND_VERSION || second.fetchCategory == ID_AND_VERSION) {
            return ID_AND_VERSION;
        }

        return ID_ONLY;
    }

    public fetch<?> unionWith(final fetch<?> second) {
        if (second == null) {
            return this;
        }

        final FetchCategory resultCategory = getMergedFetchCategory(second);
        final fetch<T> result = new fetch<>(getEntityType(), resultCategory, (isInstrumented() || second.isInstrumented()));
        result.includedProps.addAll(includedProps);
        result.includedProps.addAll(second.includedProps);
        result.excludedProps.addAll(excludedProps);
        result.excludedProps.addAll(second.excludedProps);
        includedPropsWithModels.forEach((prop, fetch) -> result.includedPropsWithModels.put(prop, fetch.unionWith(second.getIncludedPropsWithModels().get(prop))));

        second.includedPropsWithModels.entrySet().stream()
                .filter(entry -> !result.includedPropsWithModels.containsKey(entry.getKey()))
                .forEach(entry -> result.includedPropsWithModels.put(entry.getKey(), entry.getValue().unionWith(getIncludedPropsWithModels().get(entry.getKey()))));

        return result;
    }
}
