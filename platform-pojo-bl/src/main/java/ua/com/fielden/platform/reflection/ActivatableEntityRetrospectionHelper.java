package ua.com.fielden.platform.reflection;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractPersistentEntity;
import ua.com.fielden.platform.entity.ActivatableAbstractEntity;
import ua.com.fielden.platform.entity.annotation.DeactivatableDependencies;
import ua.com.fielden.platform.entity.annotation.SkipActivatableTracking;
import ua.com.fielden.platform.entity.annotation.SkipEntityExistsValidation;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.reflection.exceptions.ReflectionException;
import ua.com.fielden.platform.utils.ArrayUtils;

import java.lang.reflect.Field;
import java.util.concurrent.ExecutionException;

import static java.lang.String.format;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getAnnotation;
import static ua.com.fielden.platform.reflection.AnnotationReflector.isAnnotationPresent;
import static ua.com.fielden.platform.reflection.Finder.*;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.baseEntityType;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.determinePropertyType;
import static ua.com.fielden.platform.reflection.Reflector.isPropertyPersistent;
import static ua.com.fielden.platform.utils.EntityUtils.isActivatableEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isUnionEntityType;

/// A helper class for the retrospection of activatable entities and properties.
/// These are used upon entity saving and deletion.
///
public class ActivatableEntityRetrospectionHelper {

    private static final Cache<Class<? extends AbstractEntity<?>>, Cache<String, Boolean>> CACHE_ACTIVATABLE_PROP = CacheBuilder.newBuilder().initialCapacity(500).concurrencyLevel(50).build();

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

    /// This predicate is true if the specified property represents a special activatable reference that does not affect the reference count.
    ///
    public static boolean isSpecialActivatableToBeSkipped(final Class<? extends AbstractEntity<?>> entityType, final CharSequence prop) {
        return isSpecialActivatableToBeSkipped(findFieldByName(entityType, prop));
    }

    /// This predicate is true if the specified property is _activatable_.
    /// Such properties are processed during reference counting and validation of activatable entities.
    ///
    public static boolean isActivatableProperty(final Class<? extends AbstractEntity<?>> entityType, final CharSequence propName) {
        try {
            return CACHE_ACTIVATABLE_PROP
                    .get(entityType, () -> CacheBuilder.newBuilder().initialCapacity(10).concurrencyLevel(50).build())
                    .get(propName.toString(), () -> isActivatableProperty_(entityType, propName));
        } catch (final ExecutionException ex) {
            throw new ReflectionException(format("Could not determine whether property [%s.%s] is activatable.", entityType.getSimpleName(), propName), ex.getCause());
        }
    }

    public static boolean isActivatableProperty(final MetaProperty<?> mp) {
        return isActivatableProperty(mp.getEntity().getType(), mp.getName());
    }

    private static boolean isActivatableProperty_(final Class<? extends AbstractEntity<?>> entityType, final CharSequence propName) {
        final var prop = getFieldByName(entityType, propName.toString());
        final var propType = EntityMetadata.determinePropType(entityType, prop);
        // A property of an activatable entity type is considered "activatable" iff it is persistent
        // and @SkipEntityExistsValidation is absent or is present with skipActiveOnly == true.
        // There is also @SkipActivatableTracking, but it does not affect the activatable nature of the property -- only the counting of references.
        // TODO Properties whose type is a union entity type without activatable members should not be activatable.
        //      This change would serve as an optimisation, without changing semantics, because activatability of union-typed properties
        //      can be determined only from their actual values.
        if ((isActivatableEntityType(propType) || isUnionEntityType(propType)) && isPropertyPersistent(entityType, propName)) {
            final var seevAnnotation = getAnnotation(prop, SkipEntityExistsValidation.class);
            final boolean skipActiveOnly = seevAnnotation != null && seevAnnotation.skipActiveOnly();
            return !skipActiveOnly;
        }
        else {
            return false;
        }
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
