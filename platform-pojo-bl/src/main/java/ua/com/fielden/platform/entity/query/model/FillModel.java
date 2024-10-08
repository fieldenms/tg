package ua.com.fielden.platform.entity.query.model;

import ua.com.fielden.platform.types.tuples.T2;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.stream.Stream;

/**
 * A fill model describes how to fill an entity, i.e., populate its properties with values.
 * <p>
 * The main application of fill models is during retrieval of persisted entity instances with the purpose of populating
 * plain properties.
 *
 * <h2> Relation to fetch models </h2>
 * Unlike a fetch model, whose structure resembles a graph, <b>a fill model's structure is akin to a map</b>. This difference
 * deserves a more detailed explanation.
 * <p>
 * When an entity is retrieved from a database, what is retrieved is, in fact, a graph whose shape is described by a fetch
 * model. Such a graph may have arbitrary depth. For example, a fetch model can specify that an entity-typed property should
 * be retrieved with its own fetch model. This recursive nature of fetch models is possible because during retrieval from
 * a database <i>we know where to look for a data source of entity-typed properties</i> -- in a corresponding table.
 * <p>
 * Fill models, on the other hand, do not have the same recursive nature as fetch models. This is because there is <i>nowhere
 * to look for a data source of entity-typed properties</i>, fill models have no access to a database. Therefore, it wouldn't
 * make sense to specify a fill model for an entity-typed property -- how would we instantiate an entity for that property?
 *
 * @see FillModels
 */
public interface FillModel {

    Map<String, Object> asMap();

    /**
     * If a property is present in this fill model, returns a value to populate the property with, otherwise - an empty optional.
     *
     * @param property  simple property name
     */
    default Optional<Object> getValue(CharSequence property) {
        return Optional.ofNullable(asMap().get(property.toString()));
    }

    /**
     * If a property is present in this fill model, returns a value to populate the property with, otherwise throws.
     *
     * @param property  simple property name
     */
    default Object requireValue(final CharSequence property) {
        return getValue(property)
                .orElseThrow(() -> new FillModelException("Requested property [%s] is absent in fill model.".formatted(property)));
    }

    default boolean contains(CharSequence property) {
        return asMap().containsKey(property.toString());
    }

    /**
     * Returns all property-value pairs present in this fill model.
     */
    default Stream<T2<String, Object>> values() {
        return values(T2::t2);
    }

    /**
     * Returns all property-value pairs present in this fill model.
     *
     * @param fn  transforms a property-value pair
     */
    default <X> Stream<X> values(BiFunction<? super String, Object, X> fn) {
        return asMap().entrySet().stream().map(entry -> fn.apply(entry.getKey(), entry.getValue()));
    }

    /**
     * Returns all properties present in this fill model.
     */
    default Set<String> properties() {
        return asMap().keySet();
    }

    default boolean isEmpty() {
        return asMap().isEmpty();
    }

    /**
     * Executes an action for each property-value pair in this fill model.
     */
    default void forEach(BiConsumer<? super String, Object> fn) {
        asMap().forEach(fn);
    }

    /**
     * Builds a fill model incrementally.
     */
    interface Builder {

        /**
         * Specifies that the named property should be filled with the given value.
         *
         * @param value  must not be null
         */
        Builder set(CharSequence property, Object value);

        /**
         * Includes all property-value pairs from the given fill model.
         */
        Builder include(FillModel fillModel);

        /**
         * Bulds a fill model, throwing an exception if any property was specified more than once.
         */
        FillModel build();

        /**
         * Bulds a fill model, using the last value for any property that was specified more than once.
         */
        FillModel buildKeepingLast();
    }

}
