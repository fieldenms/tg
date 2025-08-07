package ua.com.fielden.platform.reflection;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractPersistentEntity;
import ua.com.fielden.platform.entity.annotation.DeactivatableDependencies;
import ua.com.fielden.platform.entity.annotation.SkipActivatableTracking;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.utils.ArrayUtils;

import java.lang.reflect.Field;

import static ua.com.fielden.platform.reflection.AnnotationReflector.getAnnotation;
import static ua.com.fielden.platform.reflection.AnnotationReflector.isAnnotationPresent;
import static ua.com.fielden.platform.reflection.Finder.findFieldByName;
import static ua.com.fielden.platform.reflection.Finder.getKeyMembers;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.baseEntityType;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.determinePropertyType;

/// A helper class for the retrospection of activatable entities and properties.
/// These are used upon entity saving and deletion.
///
public class ActivatableEntityRetrospectionHelper {
    private ActivatableEntityRetrospectionHelper() {}

    /// This predicate is true if the specified property represents a special activatable reference that does not affect the reference count.
    ///
    public static boolean isSpecialActivatableToBeSkipped(final Field prop) {
        return AbstractPersistentEntity.CREATED_BY.equals(prop.getName()) ||
               AbstractPersistentEntity.LAST_UPDATED_BY.equals(prop.getName()) ||
               isAnnotationPresent(prop, SkipActivatableTracking.class);
    }

    /// This predicate is true if the specified property represents a special activatable reference that does not affect the reference count.
    ///
    public static boolean isSpecialActivatableToBeSkipped(final MetaProperty<?> prop) {
        return isSpecialActivatableToBeSkipped(findFieldByName(prop.getEntity().getType(), prop.getName()));
    }

    /// This predicate is true if the specified property is a _backreference_ from a deactivatable dependency.
    /// I.e., if the property is a key member and the type of the property has [deactivatable dependencies][DeactivatableDependencies]
    /// that include `entityType`.
    ///
    /// For example, consider activatable entity `Manager` that has a key member `person: Person`.
    /// `Person` is activatable and includes `Manager` in its `@DeactivatableDependencies`.
    /// When a `Manager` is being deactivated, `Manager.person` will be a candidate for processing due to the activatable nature of `Person`.
    /// However, `Manager` is a specialisation of `Person`, since `Manager.person` is a key member and `Person` includes `Manager` in its `@DeactivatableDependencies`.
    /// Activation/deactivation of a `Manager` should not affect `refCount` for `Person`.
    /// That is why, property `Manager.person` needs to be excluded from activatable processing.
    ///
    public static boolean isDeactivatableDependencyBackref(final Class<? extends AbstractEntity<?>> entityType, final CharSequence prop) {
        return isDeactivatableDependencyBackref_(entityType, prop, determinePropertyType(entityType, prop));
    }

    public static boolean isDeactivatableDependencyBackref(final MetaProperty<?> mp) {
        return isDeactivatableDependencyBackref_(mp.getEntity().getType(), mp.getName(), mp.getType());
    }

    private static boolean isDeactivatableDependencyBackref_(final Class<? extends AbstractEntity<?>> entityType, final CharSequence prop, final Class<?> propType) {
        final var ddAnnotation = getAnnotation(propType, DeactivatableDependencies.class);
        if (ddAnnotation != null) {
            final var baseEntityType = baseEntityType(entityType);
            final var isKeyMember = getKeyMembers(baseEntityType).stream().anyMatch(km -> km.getName().contentEquals(prop));
            return isKeyMember && ArrayUtils.contains(ddAnnotation.value(), baseEntityType);
        }
        else {
            return false;
        }
    }

}
