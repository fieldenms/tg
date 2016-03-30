package ua.com.fielden.platform.utils;

import static java.lang.String.format;
import static ua.com.fielden.platform.entity.AbstractEntity.COMMON_PROPS;

import java.lang.reflect.Field;
import java.util.Arrays;
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
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.reflection.Reflector;
import ua.com.fielden.platform.reflection.exceptions.ReflectionException;

/**
 * Finalises initialisation of the specified entity instance by traversing an object graph to execute ACE handlers and assign original property values. 
 * Method {@link #execute(LinkedHashSet)} can be conveniently used to finalise initialisation of multiple dependent entities.
 * <p>
 * The process of finalising entity initialisation consists of:<br>
 * <ul>
 *  <li>Execution of ACE handlers.
 *  <li>(re)Setting of property original values.
 *  <li>Completion of the entity initialisation phase by invoking {@link AbstractEntity#endInitialising()}.
 * </ul>
 * 
 * @author TG Team
 *
 */
public class DefinersExecutor {

    /**
     * Employs the DFS algorithm to traverse the object graph starting with a node represented by <code>entity</code>. 
     * The boundary of an object graph is outlined by <code>proxied</code> properties and <code>non-entity typed</code> properties.
     *
     * @param entity -- an instance to finalise the initialisation for.
     * 
     * @return
     */
    public static <T extends AbstractEntity<?>> T execute(final T entity) {
        if (entity != null) {
            execute(Arrays.asList(entity));
        }
        return entity;
    }

    /**
     * The same as {@link #execute(AbstractEntity)}, but for a list of entities.
     * 
     * @return
     */
    public static <T extends AbstractEntity<?>> List<T> execute(final List<T> entities) {
        if (entities == null || entities.isEmpty()) {
            return entities;
        }
        // declare data structures for DFS
        final Deque<AbstractEntity<?>> frontier = new LinkedList<>(); // to be used on LIFO mode
        // the set of explored entities utilises object identities in memory to differentiate equal entities represented by different objects
        final Set<Integer> explored = new HashSet<>();
        
        // The same mutable set of entity identities 'explored' will be used for every 'explore(frontier, explored)' call for each top-level graph node (entity).
        // This will ensure that the same shared nodes will not be traversed more than once for different sub-graphs for each top-level graph node (entity).
        // Trivial example: two entities of TgFuelUsage could (and should) share single instance of TgVehicle (property 'vehicle') when retrieving via EQL.
        for (final AbstractEntity<?> entity: entities) {
            if (entity != null) {
                if (!frontier.isEmpty()) {
                    throw new DefinersExecutorException("After full exploration of previous top-level node entity (if any) 'frontier' is necessary to be empty.");
                }
                frontier.push(entity);
                explore(frontier, explored);
            }
        }
        return entities;
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
            throw new DefinersExecutorException("There is nothing to process.");
        }

        final AbstractEntity<?> entity = frontier.pop();
        final int identity = System.identityHashCode(entity);
        if (explored.contains(identity)) {
            return;
        }

        if (!entity.isInitialising()) {
            throw new DefinersExecutorException(format("Entity [%s] of type [%s] is not in the 'initialising' phase.", entity, entity.getClass()));
        }
        
        explored.add(identity);

        // collect properties to process
        final List<Field> notProxiedPropFields = Finder.streamRealProperties(entity.getType())
                .filter(field -> !Reflector.isPropertyProxied(entity, field.getName()) && !isValueProxied(entity, field)) 
                .collect(Collectors.toList());

        final boolean unionEntity = entity instanceof AbstractUnionEntity;
        final boolean isInstrumented = PropertyTypeDeterminator.isInstrumented(entity.getClass());
        final boolean isEntityPersisted = isInstrumented ? entity.isPersisted() : false;

        // FIXME please, consider applicability of the following logic (legacy code from EntityUtils.handleMetaProperties method):
        //------------------------------------------------------------------
        //              if (!unionEntity && instance.getProperties().containsKey("key")) {
        //                  final Object keyValue = instance.get("key");
        //                  if (keyValue != null) {
        //                      // handle property "key" assignment
        //                      instance.set("key", keyValue);
        //                  }
        //              }
        //------------------------------------------------------------------

        for (final Field propField : notProxiedPropFields) {
            final String propName = propField.getName();
            final boolean isEntity = AbstractEntity.class.isAssignableFrom(propField.getType());
            final boolean isCollectional = Collection.class.isAssignableFrom(propField.getType());

            final boolean notCommonPropOfUnionEntity = !(COMMON_PROPS.contains(propName) && unionEntity);
            final Object propertyValue = entity.get(propName);

            if (notCommonPropOfUnionEntity) {
                if (isCollectional) { // handle collectional properties
                    if (propertyValue != null) {
                        final Collection<?> collection = (Collection<?>) propertyValue;
                        for (final Object item: collection) {
                            if (item != null && item instanceof AbstractEntity) {
                                final AbstractEntity<?> value = (AbstractEntity<?>) item;
                                frontier.push(value);
                                explore(frontier, explored);
                            }
                        }
                    }
                } else if (isEntity) { // handle entity-typed properties
                    if (propertyValue != null) {
                        final AbstractEntity<?> value = (AbstractEntity<?>) propertyValue;
                        // produce fetch
                        frontier.push(value);
                        explore(frontier, explored);
                    }
                }
                
                // original values and execution of ACE handlers is relevant only for instrumented entities
                if (isInstrumented) {
                    handleOriginalValueAndACE(entity.getProperty(propName), propertyValue, isEntityPersisted);
                }
            }
        }

        entity.endInitialising();
    }
    

    private static boolean isValueProxied(final AbstractEntity<?> entity, final Field field) {
        Object value;
        try {
            field.setAccessible(true);
            value = field.get(entity);
            field.setAccessible(false);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new DefinersExecutorException("Could not filter property by value during checking if it is id-only proxy.", new ReflectionException(format("Could not obtain value for property [%s] in entity [%s].", field.getName(), entity.getType().getName())));
        }
        
        if (value == null || !(value instanceof AbstractEntity)) {
            return false;
        }
        
        return ((AbstractEntity<?>) value).isIdOnlyProxy();
    }

    private static void handleOriginalValueAndACE(final MetaProperty metaProp, final Object propertyValue, final boolean isEntityPersisted) {
        if (isEntityPersisted) {
            // this is very important -- original values for non-persistent entities should be left unchanged
            metaProp.setOriginalValue(propertyValue);
        }
        metaProp.define(propertyValue);
    }
}
