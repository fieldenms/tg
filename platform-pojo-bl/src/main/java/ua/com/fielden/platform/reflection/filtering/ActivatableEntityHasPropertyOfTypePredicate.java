package ua.com.fielden.platform.reflection.filtering;

import java.lang.reflect.Field;

import ua.com.fielden.platform.entity.ActivatableAbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.reflection.filtering.TypeFilter.Predicate;

/**
 * A predicate to check if an entity is of type {@link ActivatableAbstractEntity} and has a persisted property of the specified type in its hierarchy.
 */
public class ActivatableEntityHasPropertyOfTypePredicate implements Predicate<Class<?>> {
    private final Class<?> propertyType;

    public ActivatableEntityHasPropertyOfTypePredicate(final Class<?> propertyType) {
        this.propertyType = propertyType;
    }

    @Override
    public boolean apply(final Class<?> type) {
        if (ActivatableAbstractEntity.class.isAssignableFrom(type)) {
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
        }
        return false;
    }

}