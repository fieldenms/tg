package ua.com.fielden.platform.entity.fetch;

import static java.lang.String.format;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchOnly;

import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.query.fluent.fetch;

/**
 *
 * Produces an EQL fetch model based on the specified entity instance. Effectively it reconstructs the fetch model that could be used to re-fetch that entity instance with exactly
 * the same sub-graph.
 *
 * @author TG Team
 *
 */
public class FetchModelReconstructor<T extends AbstractEntity<?>> {

    /**
     * Uses the DFS algorithm for reconstruction of a fetch model based on the provided entity instance. Graph traversal stops at <code>proxy</code> or <code>null</code> property
     * values.
     *
     * @param entity
     * @return
     */
    public fetch<T> reconstruct(final T entity) {
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
    private fetch<?> explore(
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

        fetch<?> fetchModel = fetchOnly(entity.getType()).with(AbstractEntity.ID).with(AbstractEntity.VERSION);
        explored.add(identity);
        exploredFetchModels.put(identity, fetchModel);

        final List<MetaProperty<?>> retrievableNotProxiedProperties = entity.getProperties().values().stream().
                filter(p -> p.isRetrievable() && !p.isProxy()).
                collect(Collectors.toList());

        for (final MetaProperty<?> prop : retrievableNotProxiedProperties) {
            if (prop.isEntity()) { // handle entity type properties
                final AbstractEntity<?> value = (AbstractEntity<?>) prop.getValue();
                if (value != null) {
                    // produce fetch
                    frontier.push(value);
                    fetchModel = fetchModel.with(prop.getName(), explore(frontier, explored, exploredFetchModels));
                } else {
                    // fetch cannot be identified from null, so the default fetch is used
                    fetchModel = fetchModel.with(prop.getName());
                }
            } else { // handle ordinary type properties
                fetchModel = fetchModel.with(prop.getName());
            }
        }

        return fetchModel;
    }
}
