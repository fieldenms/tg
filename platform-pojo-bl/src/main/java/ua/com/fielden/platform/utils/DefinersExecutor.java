package ua.com.fielden.platform.utils;

import static ua.com.fielden.platform.entity.AbstractEntity.COMMON_PROPS;

import java.util.ArrayList;
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
 * Executes finalising of specified entity instances.
 * <p>
 * Finalising process consists of:<br>
 * 1. definers (ACEs) execution<br>
 * 2. resetting of original values<br>
 * 3. resetting of dirtiness
 *
 * @author TG Team
 *
 */
public class DefinersExecutor {

    /**
     * Uses the DFS algorithm for <code>entity</code> finalising process. Graph traversal stops at <code>proxy</code> or <code>non-entity typed</code> property
     * values.
     *
     * @param entity -- the entity to be finalised
     * 
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
     * Uses the DFS algorithm for <code>entities</code> finalising process. Graph traversal stops at <code>proxy</code> or <code>non-entity typed</code> property
     * values.
     *
     * @param entities -- entities to be finalised. The order of entities is important, this means that the entities set could contain those entities that are the part of graph
     * for previously appeared entity, but first entity, that is passed into method {@link #execute(List, Deque, Set)} inside <code>restOfEntities</code>, should be <code>top-level</code> 
     * to guarantee correct order of meta-properties handling.
     * 
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
        execute(new ArrayList<>(entities), frontier, explored);
    }
    
    /**
     * Takes the first unexplored entity from <code>restOfEntities</code> list and explores its graph. Then executes the same 
     * logic for the rest entities without first one.
     * 
     * @param restOfEntities
     * @param frontier
     * @param explored
     */
    private static <T extends AbstractEntity<?>> void execute(
            final List<T> restOfEntities, 
            final Deque<AbstractEntity<?>> frontier,
            final Set<Integer> explored) {
        
        final List<T> list = new ArrayList<>(restOfEntities);
        for (int index = 0; index < list.size(); index++) {
            final T entity = list.get(index);
            if (entity != null) {
                final int identity = System.identityHashCode(entity);
                if (!explored.contains(identity)) {
                    frontier.push(entity);
                    explore(frontier, explored);
                    
                    if (index + 1 <= list.size()) {
                        final List<T> restList = new ArrayList<>(list.subList(index + 1, list.size()));
                        execute(restList, frontier, explored);
                    }
                }
            }
        }
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

        // TODO beginInitialising() and endInitialising() calls have been arrived from TgJackson
        // TODO needs to be investigated whether applicable in EQL instantiation process
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
                        }
                    } else if (metaProp.isEntity()) { // handle entity type properties
                        if (propertyValue != null) {
                            final AbstractEntity<?> value = (AbstractEntity<?>) propertyValue;
                            // produce fetch
                            frontier.push(value);
                            explore(frontier, explored);
                        }
                    } else { // handle ordinary type properties
                    }
                    handleOriginalValueAndACE(metaProp, propertyValue);
                }
            }
        }
        // TODO entity.setDirty(false) call has been arrived from EntityUtils.handleMetaProperties
        // TODO JSON deserialisation does include information for dirtiness, that is why, most likely we should have a switch 'shouldResetDirtiness'
        // if (!unionEntity) {
        //    entity.setDirty(false);
        // }
        
        // TODO beginInitialising() and endInitialising() calls have been arrived from TgJackson
        // TODO needs to be investigated whether applicable in EQL instantiation process
        entity.endInitialising();
    }

    private static void handleOriginalValueAndACE(final MetaProperty metaProp, final Object propertyValue) {
        // TODO the check for 'persisted' has been arrived from EntityJsonDeserialiser
        // TODO needs to be investigated whether applicable in EQL instantiation process
        if (metaProp.getEntity().isPersisted()) {
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
// TODO please, consider adding the following block into main logic!
//------------------------------------------------------------------
//        final boolean unionEntity = instance instanceof AbstractUnionEntity;
//        if (!unionEntity && instance.getProperties().containsKey("key")) {
//            final Object keyValue = instance.get("key");
//            if (keyValue != null) {
//                // handle property "key" assignment
//                instance.set("key", keyValue);
//            }
//        }
//------------------------------------------------------------------
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
