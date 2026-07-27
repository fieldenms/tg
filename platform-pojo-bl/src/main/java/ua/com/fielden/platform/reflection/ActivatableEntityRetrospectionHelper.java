package ua.com.fielden.platform.reflection;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import jakarta.annotation.Nullable;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractPersistentEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.ActivatableAbstractEntity;
import ua.com.fielden.platform.entity.annotation.DeactivatableDependencies;
import ua.com.fielden.platform.entity.annotation.SkipActivatableTracking;
import ua.com.fielden.platform.entity.annotation.SkipEntityExistsValidation;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.reflection.exceptions.ReflectionException;
import ua.com.fielden.platform.utils.ArrayUtils;

import java.lang.reflect.Field;
import java.util.concurrent.ExecutionException;

import static ua.com.fielden.platform.reflection.AnnotationReflector.getAnnotation;
import static ua.com.fielden.platform.reflection.AnnotationReflector.isAnnotationPresent;
import static ua.com.fielden.platform.reflection.Finder.*;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.baseEntityType;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.determinePropertyType;
import static ua.com.fielden.platform.reflection.Reflector.isPropertyCalculated;
import static ua.com.fielden.platform.reflection.Reflector.isPropertyPersistent;
import static ua.com.fielden.platform.utils.EntityUtils.*;

/// A helper class for the retrospection of activatable entities and properties.
/// These are used upon entity saving and deletion.
///
public class ActivatableEntityRetrospectionHelper {

    private static final Cache<Class<? extends AbstractEntity<?>>, Cache<String, Boolean>> CACHE_ACTIVATABLE_PROP = CacheBuilder.newBuilder().initialCapacity(500).concurrencyLevel(50).build();

    public static final String ERR_ACTIVATABLE_PROPERTY_DETERMINATION = "Could not determine whether property [%s.%s] is activatable.";

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

    /// This predicate is `true` if the specified property is _activatable_.
    /// It does not take into account the persistent nature of `propName` and as such should mainly be used for the purpose of validation.
    /// Method [#isActivatablePersistentProperty(Class, CharSequence)] should be used where activatable reference counting and activatable dependency processing is required.
    ///
    public static boolean isActivatableProperty(final Class<? extends AbstractEntity<?>> entityType, final CharSequence propName) {
        try {
            return CACHE_ACTIVATABLE_PROP
                    .get(entityType, () -> CacheBuilder.newBuilder().initialCapacity(10).concurrencyLevel(50).build())
                    .get(propName.toString(), () -> isActivatableProperty_(entityType, propName));
        } catch (final ExecutionException ex) {
            throw new ReflectionException(ERR_ACTIVATABLE_PROPERTY_DETERMINATION.formatted(entityType.getSimpleName(), propName), ex.getCause());
        }
    }

    /// The same as [#isActivatableProperty(Class, CharSequence)], but with additional condition for `propName` to represent a persistent property.
    /// Such properties are processed during reference counting, deactivation of activatable entities, and for tracking activatable dependencies.
    ///
    public static boolean isActivatablePersistentProperty(final Class<? extends AbstractEntity<?>> entityType, final CharSequence propName) {
        return isPropertyPersistent(entityType, propName) && isActivatableProperty(entityType, propName);
    }

    private static boolean isActivatableProperty_(final Class<? extends AbstractEntity<?>> entityType, final CharSequence propName) {
        final var prop = getFieldByName(entityType, propName.toString());
        final var propType = EntityMetadata.determinePropType(entityType, prop);
        // A property of an activatable entity type is considered "activatable" iff:
        // 1. Property type is an activatable entity or a union entity.
        // 2. @SkipEntityExistsValidation is absent or is present with skipActiveOnly == false.
        // 3. Property is not calculated (both persistent and plain are applicable).
        //
        // Note 1: There is also @SkipActivatableTracking,
        //         but it does not affect the activatable nature of the property -- only the counting of references.
        // Note 2: @CritOnly properties (mainly relevant for SINGLE) in the context of persistent entity are considered activatable.
        //         This is to support generative entities (those implementing [WithCreatedByUser] and companion implementing [IGenerator]).
        // Note 3: Properties whose type is a union entity type without activatable members are not activatable.
        //         Activatability of union-typed properties can be determined only from their actual values.
        if (isActivatableEntityOrUnionType(propType) && !isPropertyCalculated(entityType, propName)) {
            final var seevAnnotation = getAnnotation(prop, SkipEntityExistsValidation.class);
            final boolean skipActiveOnly = seevAnnotation != null && seevAnnotation.skipActiveOnly();
            return !skipActiveOnly;
        }
        else {
            return false;
        }
    }

    /// This predicate is holds if the specified reference is a _backreference_ from a deactivatable dependency.
    /// For example, if one of the following holds:
    /// 1. `prop` is a key member in `entityType`, and the type of `prop` has [deactivatable dependencies][DeactivatableDependencies]
    ///    that include `entityType`, or
    /// 2. `prop` is a union-typed key member in `entityType`, and the type of the active entity (in the union `value`)
    ///    has [deactivatable dependencies][DeactivatableDependencies] that include `entityType`.
    ///
    /// As an example of the first condition, consider activatable entity `Manager` that has a key member `person: Person`.
    /// Entity `Person` is activatable and includes `Manager` in its `@DeactivatableDependencies`.
    /// When a `Manager` is being deactivated, `Manager.person` will be a candidate for processing due to the activatable nature of `Person`.
    /// However, `Manager` is a specialisation of `Person` because `Manager.person` is a key member and `Person` includes `Manager` in its `@DeactivatableDependencies`.
    /// Activation/deactivation of a `Manager` should not affect `refCount` for `Person`.
    /// That is why, property `Manager.person` needs to be excluded from activatable processing.
    ///
    public static boolean isDeactivatableDependencyBackref(
            final Class<? extends AbstractEntity<?>> entityType,
            final CharSequence prop,
            final Object value)
    {
        return isDeactivatableDependencyBackref_(entityType, prop, determinePropertyType(entityType, prop), value);
    }

    private static boolean isDeactivatableDependencyBackref_(
            final Class<? extends AbstractEntity<?>> entityType,
            final CharSequence prop,
            final Class<?> propType,
            final Object value)
    {
        if (value == null || !isEntityType(propType)) {
            return false;
        }

        final @Nullable Class<? extends AbstractEntity<?>> referencedType;

        if (isUnionEntityType(propType)) {
            final var union = (AbstractUnionEntity) value;
            referencedType = union.activeEntity() == null ? null : union.activeEntity().getType();
        }
        else {
            referencedType = (Class<? extends AbstractEntity<?>>) propType;
        }

        if (referencedType == null) {
            return false;
        }

        final var ddAnnotation = getAnnotation(referencedType, DeactivatableDependencies.class);
        if (ddAnnotation != null) {
            final var baseEntityType = baseEntityType(entityType);
            final var isKeyMember = getKeyMembers(baseEntityType).stream().anyMatch(km -> km.getName().contentEquals(prop));
            return isKeyMember && ArrayUtils.contains(ddAnnotation.value(), baseEntityType);
        }
        else {
            return false;
        }
    }

    /// A predicate to test whether a property value represents an activatable reference.
    ///
    /// The implementation processes union-typed values using the two-level approach,
    /// where a union-typed property is considered activatable if it or the active union member property is activatable.
    ///
    /// @param entityType  the entity type that declares the property
    /// @param propName  the name of the property
    /// @param propValue  the value assigned to the property
    /// @param coFinder  used for find a union entity companion
    ///
    public static boolean isActivatableReference(
            final Class<? extends ActivatableAbstractEntity<?>> entityType,
            final String propName,
            final Object propValue,
            final ICompanionObjectFinder coFinder)
    {
        if (propValue == null) {
            return false;
        }

        final var prop = findFieldByName(entityType, propName);
        if (!isEntityType(prop.getType())) {
            return false;
        }
        else if (isDeactivatableDependencyBackref(entityType, propName, propValue)) {
            return false;
        }
        else {
            return isActivatableReferenceWithoutPreconditions(entityType, propName, (AbstractEntity<?>) propValue, coFinder);
        }
    }

    /// This is more of a helper predicate to extract the logic for checking activatable references without any pre-conditions,
    /// which are generally required in specific places.
    /// Generally speaking this predicate should not be used by itself in the application logic.
    ///
    /// If `propName` is not union-typed, the predicate checks the property itself.
    /// Otherwise, the active union member is also considered.
    ///
    public static boolean isActivatableReferenceWithoutPreconditions(
            final Class<? extends ActivatableAbstractEntity<?>> entityType,
            final String propName,
            final AbstractEntity<?> propValue,
            final ICompanionObjectFinder coFinder)
    {
        if (propValue != null && isActivatablePersistentProperty(entityType, propName) && !isSpecialActivatableToBeSkipped(entityType, propName)) {
            return true;
        }
        else if (propValue != null && isUnionEntityType(propValue.getType())) {
            final var unionValue = (AbstractUnionEntity) propValue;
            final var unionType = unionValue.getType();
            final var activePropName = unionValue.activePropertyName();
            return isActivatableProperty(unionType, activePropName)
                    && !isSpecialActivatableToBeSkipped(unionType, activePropName);
        }
        else {
            return false;
        }
    }

}
