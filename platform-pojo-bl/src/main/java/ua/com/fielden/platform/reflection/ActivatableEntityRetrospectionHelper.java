package ua.com.fielden.platform.reflection;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractPersistentEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.ActivatableAbstractEntity;
import ua.com.fielden.platform.entity.annotation.DeactivatableDependencies;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.SkipActivatableTracking;
import ua.com.fielden.platform.entity.meta.MetaProperty;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static ua.com.fielden.platform.reflection.AnnotationReflector.isAnnotationPresent;
import static ua.com.fielden.platform.reflection.Finder.streamRealProperties;

/**
 * A helper class providing functions (aka static methods) for the retrospection of activatable entities and properties.
 * These are used upon entity saving and deletion.
 * 
 * @author TG Team
 *
 */
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
        return isSpecialActivatableToBeSkipped(Finder.findFieldByName(prop.getEntity().getType(), prop.getName()));
    }

    /// A helper method to determine which of the provided properties should be handled upon save from the perspective of activatable entity logic (update of refCount).
    ///
    /// A remark: the proxied activatable properties need to be handled from the perspective of activatable entity logic (update of refCount).
    ///
    private static boolean shouldProcessAsActivatable(final AbstractEntity<?> entity, final Set<String> keyMembers, final MetaProperty<?> prop) {
        // let's first identify whether entity belongs to the deactivatable type of the referenced property type
        // if so, it should not inflict any ref counts for this property
        final var type = (Class<? extends AbstractEntity<?>>) prop.getType();
        final DeactivatableDependencies ddAnnotation = type.getAnnotation(DeactivatableDependencies.class);
        boolean belongsToDeactivatableDependencies;
        if (ddAnnotation != null) {
            // if the main type belongs to dependent deactivatables of the type for the current property,
            // and that property is a key member then such property should be excluded from standard processing of dirty activatables
            belongsToDeactivatableDependencies = keyMembers.contains(prop.getName()) && Arrays.asList(ddAnnotation.value()).contains(entity.getType());
        } else {
            belongsToDeactivatableDependencies = false;
        }
        // null values correspond to dereferencing and should be allowed only for already persisted entities
        // checking prop.isProxy() is really just to prevent calling prop.getValue() on proxied properties, which fails with StrictProxyException
        // this also assumes that proxied properties might actually have a value and need to be included for further processing
        // values for proxied properties are then retrieved in a lazy fashion by Hibernate
        return !belongsToDeactivatableDependencies && (prop.isProxy() || prop.getValue() != null || entity.isPersisted());
    }

    /// Collects properties that represent not dirty activatable properties.
    ///
    public static List<String> collectActivatableNotDirtyProperties(final AbstractEntity<?> entity, final Set<String> keyMembers) {
        if (entity.isInstrumented()) {
            return entity.getProperties().values()
                    .stream()
                    // proxied property is considered to be not dirty in this context
                    .filter(prop -> prop.isProxy() || !prop.isDirty())
                    .filter(MetaProperty::isActivatable)
                    .filter(prop -> shouldProcessAsActivatable(entity, keyMembers, prop))
                    .filter(prop -> !isSpecialActivatableToBeSkipped(prop))
                    .map(MetaProperty::getName)
                    .toList();
        }
        else {
            // TODO Why DeactivatableDependencies are not checked here?
            return streamRealProperties(entity.getType(), MapTo.class)
                    .filter(field -> (ActivatableAbstractEntity.class.isAssignableFrom(field.getType()) || AbstractUnionEntity.class.isAssignableFrom(field.getType()))
                                     && !isSpecialActivatableToBeSkipped(field))
                    .map(Field::getName)
                    .toList();
        }
    }

}
