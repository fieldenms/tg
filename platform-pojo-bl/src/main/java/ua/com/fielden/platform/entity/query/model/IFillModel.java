package ua.com.fielden.platform.entity.query.model;

import ua.com.fielden.platform.entity.AbstractEntity;
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
 * Unlike a fetch model, whose structure resembles a graph, <b>a fill model's structure is akin to a map</b>.
 * This difference deserves a more detailed explanation.
 * <p>
 * When an entity is retrieved from a database, what is retrieved is, in fact, a graph whose shape is described by a fetch model.
 * Such a graph may have arbitrary depth.
 * For example, a fetch model can specify that an entity-typed property should be retrieved with its own fetch model.
 * This recursive nature of fetch models is possible because during retrieval from a database <i>we know where to look for a data source of entity-typed properties</i>.
 * <p>
 * Fill models, on the other hand, do not have the same recursive nature as fetch models.
 * This is because there is <i>nowhere to look for a data source of entity-typed properties</i>, fill models have no access to a database.
 * Therefore, it does not make sense to specify a fill model for an entity-typed property.
 */
public interface IFillModel<T extends AbstractEntity<?>> {

    /**
     * If {@code propName} is present in this fill model, returns a value to populate the property with, otherwise – an empty optional.
     *
     * @param propName  a simple property name
     */
    Optional<Object> getValue(CharSequence propName);

    /**
     * Checks if {@code propName} is already present in the fill model.
     * @param propName
     * @return
     */
    boolean contains(CharSequence propName);

    /**
     * Returns all property-value pairs present in this fill model.
     */
    Map<String, Object> values();

    /**
     * Returns all properties present in this fill model.
     */
    Set<String> properties();

    /**
     * Identifies whether a fill model has any properties to fill.
     *
     * @return
     */
    boolean isEmpty();

    /**
     * Fills {@code entity} based on the property-value pairs in this fill model.
     */
    T fill(final T entity);

    /**
     * An empty fill model.
     * It should be used in situations where no "filling" of the property values is needed.
     */
    IFillModel EMPTY_FILL_MODEL = new IFillModel<>() {
        @Override
        public Optional<Object> getValue(CharSequence propName) {
            return Optional.empty();
        }

        @Override
        public boolean contains(CharSequence propName) {
            return false;
        }

        @Override
        public Map<String, Object> values() {
            return Map.of();
        }

        @Override
        public Set<String> properties() {
            return Set.of();
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public AbstractEntity<?> fill(final AbstractEntity<?> entity) {
            return entity;
        }
    };
}
