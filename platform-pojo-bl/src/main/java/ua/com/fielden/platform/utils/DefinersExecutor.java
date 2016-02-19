package ua.com.fielden.platform.utils;

import static ua.com.fielden.platform.entity.AbstractEntity.COMMON_PROPS;

import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.serialisation.jackson.serialisers.EntityJsonSerialiser;

/**
 *
 * Produces an EQL fetch model based on the specified entity instance. Effectively it reconstructs the fetch model that could be used to re-fetch that entity instance with exactly
 * the same sub-graph.
 *
 * @author TG Team
 *
 */
public class DefinersExecutor {

    /**
     * Uses the DFS algorithm for reconstruction of a fetch model based on the provided entity instance. Graph traversal stops at <code>proxy</code> or <code>null</code> property
     * values.
     *
     * @param entity
     * @return
     */
    public static <T extends AbstractEntity<?>> T execute(final T entity) {
        if (entity == null) {
            return null;
        }
        // declare data structures for DFS
        final Deque<AbstractEntity<?>> frontier = new LinkedList<>(); // to be used on LIFO mode
        // the set of explored entities utilises object identities in memory to differentiate equal entities represented by different objects
        final Set<Integer> explored = new HashSet<>();

        // initialize data structures
        frontier.push(entity);

        explore(frontier, explored);
        
        return entity;
    }
    
    /**
     * Uses the DFS algorithm for reconstruction of a fetch model based on the provided entity instance. Graph traversal stops at <code>proxy</code> or <code>null</code> property
     * values.
     *
     * @param entity
     * @return
     */
    public static <T extends AbstractEntity<?>> void execute(final LinkedHashSet<T> entities) {
        if (entities == null) {
            return;
        }
        // declare data structures for DFS
        final Deque<AbstractEntity<?>> frontier = new LinkedList<>(); // to be used on LIFO mode
        // the set of explored entities utilises object identities in memory to differentiate equal entities represented by different objects
        final Set<Integer> explored = new HashSet<>();

        // initialize data structures
        for (final T entity : entities) {
            if (entity != null) {
                frontier.push(entity);
            }
        }

        explore(frontier, explored);
    }

    /**
     * Executes definers recursively traversing the object graph using DFS algorithm.
     *
     * @param frontier
     * @param explored
     * @return
     */
    private static void explore(
            final Deque<AbstractEntity<?>> frontier,
            final Set<Integer> explored) {
        if (frontier.isEmpty()) {
            throw new IllegalStateException("There is nothing to process.");
        }

        final AbstractEntity<?> entity = frontier.pop();
        final int identity = System.identityHashCode(entity);
        if (explored.contains(identity)) {
            return;
        }

        entity.beginInitialising();
        explored.add(identity);

        // TODO review: keys should be first and the all other properties in order of property definition
        final List<MetaProperty<?>> notProxiedProperties = entity.getProperties().values().stream().
                filter(p -> !p.isProxy()).
                collect(Collectors.toList());

        final boolean unionEntity = entity instanceof AbstractUnionEntity;
        
        for (final MetaProperty metaProp : notProxiedProperties) {
            if (metaProp != null) {
                final boolean notCommonPropOfUnionEntity = !(COMMON_PROPS.contains(metaProp.getName()) && unionEntity);
                final Object propertyValue = entity.get(metaProp.getName());
                
                final boolean notProxied = !EntityJsonSerialiser.isValueProxied(propertyValue);
                if (notCommonPropOfUnionEntity && notProxied) {
                    if (metaProp.isCollectional()) { // handle entity type properties
                        if (propertyValue != null) {
                            final Collection collection = (Collection) propertyValue;
                            collection.forEach(item -> {
                                if (item != null && item instanceof AbstractEntity) {
                                    final AbstractEntity<?> value = (AbstractEntity<?>) item;
                                    // produce fetch
                                    frontier.push(value);
                                    explore(frontier, explored);
                                }
                            });
                            
                            handleOriginalValueAndACE(metaProp, propertyValue);
                        } else {
                            handleOriginalValueAndACE(metaProp, propertyValue);
                        }
                    } else if (metaProp.isEntity()) { // handle entity type properties
                        if (propertyValue != null) {
                            final AbstractEntity<?> value = (AbstractEntity<?>) propertyValue;
                            // produce fetch
                            frontier.push(value);
                            explore(frontier, explored);
                        }
                        handleOriginalValueAndACE(metaProp, propertyValue);
                    } else { // handle ordinary type properties
                        handleOriginalValueAndACE(metaProp, propertyValue);
                    }
                }
            }
        }
        if (!unionEntity) {
            entity.setDirty(false);
        }
        entity.endInitialising();
    }

    private static void handleOriginalValueAndACE(final MetaProperty metaProp, final Object propertyValue) {
        if (metaProp.getEntity().isPersisted()) { // TODO the check for persistance is arrived from EntityJsonDeserialiser
            metaProp.setOriginalValue(propertyValue);
        }
        metaProp.define(propertyValue);
    }
    
//    /**
//     * Performs {@link AbstractEntity} instance's post-creation actions such as original values setting, definers invoking, dirtiness resetting etc.
//     *
//     * @param instance
//     * @return
//     */
//    public static AbstractEntity<?> handleMetaProperties(final AbstractEntity<?> instance) {
//        final boolean unionEntity = instance instanceof AbstractUnionEntity;
//        if (!unionEntity && instance.getProperties().containsKey("key")) {
//            final Object keyValue = instance.get("key");
//            if (keyValue != null) {
//                // handle property "key" assignment
//                instance.set("key", keyValue);
//            }
//        }
//
//        for (final MetaProperty metaProp : instance.getProperties().values()) {
//            final boolean notNull = metaProp != null;
//            final boolean notCommonPropOfUnionEntity = notNull && !(COMMON_PROPS.contains(metaProp.getName()) && unionEntity);
//            final boolean notProxied = notNull && !(proxiedProps.contains(metaProp.getName()));
//            if (notNull && notCommonPropOfUnionEntity && notProxied) {
//                final Object newOriginalValue = instance.get(metaProp.getName());
//                metaProp.setOriginalValue(newOriginalValue);
//                metaProp.define(newOriginalValue);
//            }
//        }
//        if (!unionEntity) {
//            instance.setDirty(false);
//        }
//
//        return instance;
//    }
}
