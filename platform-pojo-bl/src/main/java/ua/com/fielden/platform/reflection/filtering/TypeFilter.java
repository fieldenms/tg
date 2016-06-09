package ua.com.fielden.platform.reflection.filtering;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;

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

}
