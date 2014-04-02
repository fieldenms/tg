package ua.com.fielden.platform.reflection;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.MapTo;

/**
 * A convenient utility class that provides a number of method for traversing entity type hierarchies.
 *
 * @author TG Team
 *
 */
public class TypeFilter {
    private TypeFilter() {}

    /**
     * A contract modelling a predicate function.
     *
     * @author TG Team
     *
     */
    public static interface Predicate<T> {
        boolean apply(final T value);
    }

    /**
     * Filters out those types from the provided list that do not satisfy the predicate.
     *
     * @param entityTypes -- a list of types to filter
     * @param predicate -- a predicate that to be satisfied for a type to be chosen for a resultant list
     * @return
     */
    public static List<Class<? extends AbstractEntity<?>>> filter(final List<Class<? extends AbstractEntity<?>>> entityTypes, final Predicate<Class<?>> predicate) {
        final List<Class<? extends AbstractEntity<?>>> result = new ArrayList<>();

        for (final Class<? extends AbstractEntity<?>> type: entityTypes) {
            if (predicate.apply(type)) {
                result.add(type);
            }
        }

        return result;
    }

    /**
     * A predicate to check if an entity type has a persisted property of the specified type in its hierarchy.
     */
    public static class EntityHasPropertyOfType implements Predicate<Class<?>> {
        private final Class<?> propertyType;

        public EntityHasPropertyOfType(final Class<?> propertyType) {
            this.propertyType = propertyType;
        }

        @Override
        public boolean apply(final Class<?> type) {
            // iterate over an entity type hierarchy in order to check each type for an existence of property of the specified type
            for (Class<?> klass = type; klass != Object.class; klass = klass.getSuperclass()) {
                // iterate over a list of properties to compare their type to the target one
                for (final Field field : klass.getDeclaredFields()) {
                    if (field.getType() == propertyType &&
                        field.isAnnotationPresent(IsProperty.class) &&
                        field.isAnnotationPresent(MapTo.class)) { // the first matching property is fine
                        return true;
                    }
                }
            }
            return false;
        }

    }

}
