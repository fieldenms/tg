package ua.com.fielden.platform.entity.fetch;

import static java.lang.String.format;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchIdOnly;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchOnly;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchOnlyAndInstrument;
import static ua.com.fielden.platform.utils.EntityUtils.getEntityIdentity;
import static ua.com.fielden.platform.utils.EntityUtils.isOneToOne;

import java.lang.reflect.Field;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.annotation.Calculated;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.entity.query.fluent.fetch;
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
        final Set<String> explored = new HashSet<>();
        // a helper structure to keep fetch models for already explored property values
        // uses object identity instead of actual objects due to the fact that there could be several equal entities
        // but fetched with different fetch models
        final Map<String, fetch<?>> exploredFetchModels = new HashMap<>();

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
            final Set<String> explored,
            final Map<String, fetch<?>> exploredFetchModels) {
        if (frontier.isEmpty()) {
            throw new IllegalStateException("There is nothing to process.");
        }

        final AbstractEntity<?> entity = frontier.pop();
        final String identity = getEntityIdentity(entity);
        if (explored.contains(identity)) {
            return exploredFetchModels.get(identity);
        }

        fetch<?> fetchModel = PropertyTypeDeterminator.isInstrumented(entity.getClass()) ? fetchOnlyAndInstrument(entity.getType()) 
                              : entity.isIdOnlyProxy() ? fetchIdOnly(entity.getType()) : fetchOnly(entity.getType());
        explored.add(identity);
        exploredFetchModels.put(identity, fetchModel);

        final List<Field> retrievableNotProxiedPropFields = Finder.streamRealProperties(entity.getType())
                        .filter(field -> (entity instanceof AbstractUnionEntity) || Reflector.isPropertyRetrievable(entity, field) && !Reflector.isPropertyProxied(entity, field.getName()))
                        .collect(Collectors.toList());

        for (final Field propField : retrievableNotProxiedPropFields) {
            final String propName = propField.getName();
            final boolean isEntity = AbstractEntity.class.isAssignableFrom(propField.getType()) && !PropertyDescriptor.class.equals(propField.getType());

            if (isEntity) { // handle entity type properties
                final AbstractEntity<?> value = (AbstractEntity<?>) entity.get(propName);
                if (value != null) {
                    // produce fetch
                    frontier.push(value);
                    fetchModel = fetchModel.with(propName, explore(frontier, explored, exploredFetchModels));
                    exploredFetchModels.put(identity, fetchModel);
                } else {
                    // fetch cannot be identified from null, so the fetch id only strategy is the most suitable
                    // however, calculated entity-typed properties require the default fetch strategy
                    // refer https://github.com/fieldenms/tg/issues/2070 for more details
                    @SuppressWarnings("unchecked")
                    final Class<AbstractEntity<?>> valueType = (Class<AbstractEntity<?>>) propField.getType();
                    if (propField.isAnnotationPresent(Calculated.class) || isOneToOne(valueType)) {
                        // fetch id-only is not applicable for calculated entity-typed properties if they are used in Entity Masters
                        // however, at this stage, there is no way to identify that and the default fetch model should be used
                        fetchModel = fetchModel.with(propName);
                    } else {
                        fetchModel = fetchModel.with(propName, fetchIdOnly(valueType));
                    }
                    exploredFetchModels.put(identity, fetchModel);
                }
            } else { // handle ordinary type properties
                fetchModel = fetchModel.with(propName);
                exploredFetchModels.put(identity, fetchModel);
            }
        }

        return fetchModel;
    }

}
