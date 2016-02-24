package ua.com.fielden.platform.entity.fetch;

import static java.lang.String.format;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.cond;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetch;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAggregates;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAllInclCalc;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAndInstrument;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchKeyAndDescOnly;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchOnly;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.orderBy;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.lang.reflect.Field;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.ActivatableAbstractEntity;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.OrderingModel;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.reflection.Reflector;

/**
 *
 * Produces an EQL fetch model based on the specified entity instance. Effectively it reconstructs the fetch model that could be used to re-fetch that entity instance with exactly
 * the same sub-graph.
 *
 * @author TG Team
 *
 */
public class FetchModelReconstructor {

    /**
     * Uses the DFS algorithm for reconstruction of a fetch model based on the provided entity instance. Graph traversal stops at <code>proxy</code> or <code>null</code> property
     * values.
     *
     * @param entity
     * @return
     */
    public static <T extends AbstractEntity<?>> fetch<T> reconstruct(final T entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Entity instance cannot be null");
        }
        if (!entity.isPersistent()) {
            throw new IllegalArgumentException(format("Entity type %s is not persistent", entity.getType()));
        }
        // declare data structures for DFS
        final Deque<AbstractEntity<?>> frontier = new LinkedList<>(); // to be used on LIFO mode
        // the set of explored entities utilises object identities in memory to differentiate equal entities represented by different objects
        final Set<Integer> explored = new HashSet<>();
        // a helper structure to keep fetch models for already explored property values
        // uses object identity instead of actual objects due to the fact that there could be several equal entities
        // but fetched with different fetch models
        final Map<Integer, fetch<?>> exploredFetchModels = new HashMap<>();

        // initialize data structures
        frontier.push(entity);

        return (fetch<T>) explore(frontier, explored, exploredFetchModels);
    }

    /**
     * Constructs entity fetch models recursively traversing the object graph using DFS algorithm.
     *
     * @param frontier
     * @param explored
     * @param exploredFetchModels
     * @return
     */
    private static fetch<?> explore(
            final Deque<AbstractEntity<?>> frontier,
            final Set<Integer> explored,
            final Map<Integer, fetch<?>> exploredFetchModels) {
        if (frontier.isEmpty()) {
            throw new IllegalStateException("There is nothing to process.");
        }

        final AbstractEntity<?> entity = frontier.pop();
        final int identity = System.identityHashCode(entity);
        if (explored.contains(identity)) {
            return exploredFetchModels.get(identity);
        }

        fetch<?> fetchModel = PropertyTypeDeterminator.isInstrumented(entity.getClass()) ? fetchAndInstrument(entity.getType()).with(AbstractEntity.ID) : fetch(entity.getType()).with(AbstractEntity.ID);
        explored.add(identity);
        exploredFetchModels.put(identity, fetchModel);

        final List<Field> retrievableNotProxiedPropFields = Finder.streamRealProperties(entity.getType())
                        .filter(field -> (entity instanceof AbstractUnionEntity) || Reflector.isPropertyRetrievable(entity, field) && !Reflector.isPropertyProxied(entity, field.getName()))
                        .collect(Collectors.toList());
        
        for (final Field propField : retrievableNotProxiedPropFields) {
            final String propName = propField.getName();
            final boolean isEntity = AbstractEntity.class.isAssignableFrom(propField.getType());
            
            if (isEntity) { // handle entity type properties
                final AbstractEntity<?> value = (AbstractEntity<?>) entity.get(propName);
                if (value != null) {
                    // produce fetch
                    frontier.push(value);
                    fetchModel = fetchModel.with(propName, explore(frontier, explored, exploredFetchModels));
                } else {
                    // fetch cannot be identified from null, so the default fetch is used
                    fetchModel = fetchModel.with(propName);
                }
            } else { // handle ordinary type properties
                fetchModel = fetchModel.with(propName);
            }
        }

        return fetchModel;
    }
}
