package ua.com.fielden.platform.companion;

import static ua.com.fielden.platform.types.tuples.T2.t2;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractPersistentEntity;
import ua.com.fielden.platform.entity.ActivatableAbstractEntity;
import ua.com.fielden.platform.entity.annotation.DeactivatableDependencies;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.types.tuples.T2;

/**
 * A helper class providing functions (aka static methods) for the retrospection of activatable entities and properties.
 * These are used upon entity saving and deletion.
 * 
 * @author TG Team
 *
 */
public class ActivatableEntityRetrospectionHelper {
    private ActivatableEntityRetrospectionHelper() {}
    
    /**
     * Determines whether the specified property does not represent a special activatable entity that does not affect the reference count and should be skipped upon updating reference counts. 
     * 
     * @param prop
     * @return
     */
    public static boolean isNotSpecialActivatableToBeSkipped(final MetaProperty<?> prop) {
        return !AbstractPersistentEntity.CREATED_BY.equals(prop.getName()) && 
               !AbstractPersistentEntity.LAST_UPDATED_BY.equals(prop.getName());
    }

    /**
     * A helper method to determine which of the provided properties should be handled upon save from the perspective of activatable entity logic (update of refCount).
     * <p>
     * A remark: the proxied activatable properties need to be handled from the perspective of activatable entity logic (update of refCount).
     *
     * @param entity
     * @param keyMembers
     * @param result
     * @param prop
     */
    public static <T extends AbstractEntity<?>> void addToResultIfApplicableFromActivatablePerspective(final T entity, final Set<String> keyMembers, final Set<MetaProperty<? extends ActivatableAbstractEntity<?>>> result, final MetaProperty<?> prop) {
        // let's first identify whether entity belongs to the deactivatable type of the referenced property type
        // if so, it should not inflict any ref counts for this property
        final Class<? extends ActivatableAbstractEntity<?>> type = (Class<? extends ActivatableAbstractEntity<?>>) prop.getType();
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
        if (!belongsToDeactivatableDependencies && (prop.isProxy() || prop.getValue() != null || entity.isPersisted())) {
            result.add((MetaProperty<? extends ActivatableAbstractEntity<?>>) prop);
        }
    }

    /**
     * Collects properties that represent not dirty activatable properties.
     *
     * @param entity
     * @return
     */
     public static final <T extends AbstractEntity<?>> Set<T2<String, Class<ActivatableAbstractEntity<?>>>> collectActivatableNotDirtyProperties(final T entity, final Set<String> keyMembers) {
        if (entity.isInstrumented()) {
            final Set<MetaProperty<? extends ActivatableAbstractEntity<?>>> result = new HashSet<>();
            for (final MetaProperty<?> prop : entity.getProperties().values()) {
                // proxied property is considered to be not dirty in this context
                final boolean notDirty = prop.isProxy() || !prop.isDirty(); 
                if (notDirty && prop.isActivatable() && isNotSpecialActivatableToBeSkipped(prop)) {
                    addToResultIfApplicableFromActivatablePerspective(entity, keyMembers, result, prop);
                }
            }
            return result.stream()
                    .map(prop -> t2(prop.getName(), (Class<ActivatableAbstractEntity<?>>) prop.getType()))
                    .collect(Collectors.toSet());
        } else {
            return Finder.streamRealProperties(entity.getType(), MapTo.class)
                    .filter(field -> ActivatableAbstractEntity.class.isAssignableFrom(field.getType()))
                    .map(field -> t2(field.getName(), (Class<ActivatableAbstractEntity<?>>) field.getType()))
                    .collect(Collectors.toSet());
        }
    }


}
