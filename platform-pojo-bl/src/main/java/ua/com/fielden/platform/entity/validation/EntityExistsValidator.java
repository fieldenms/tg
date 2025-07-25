package ua.com.fielden.platform.entity.validation;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.ActivatableAbstractEntity;
import ua.com.fielden.platform.entity.annotation.SkipEntityExistsValidation;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.error.Result;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.util.Set;

import static java.lang.String.format;
import static ua.com.fielden.platform.entity.AbstractEntity.*;
import static ua.com.fielden.platform.entity.ActivatableAbstractEntity.ACTIVE;
import static ua.com.fielden.platform.entity.proxy.MockNotFoundEntityMaker.getErrorMessage;
import static ua.com.fielden.platform.entity.proxy.MockNotFoundEntityMaker.isMockNotFoundValue;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.*;
import static ua.com.fielden.platform.error.Result.failure;
import static ua.com.fielden.platform.error.Result.successful;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getPropertyAnnotation;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitleAndDesc;
import static ua.com.fielden.platform.utils.EntityUtils.copy;
import static ua.com.fielden.platform.utils.EntityUtils.isPropertyDescriptor;

/// This validator applies to entity-typed properties, ensuring that the new value is an entity that exists.
///
/// * If the validated property has [SkipEntityExistsValidation], this validator will be ignored.
/// * If the union-typed property has `@SkipEntityExistsValidation(skipNew = true)`, a union value with non-persisted
///   active entity will be considered valid iff the corresponding union member is also annotated so.
/// * The new value must be persisted and non-dirty.
/// * If the property is union-typed, the new union value must be valid.
/// * If the property represents an activatable reference (either direct or via a union), and its enclosing entity is active,
///   the referenced entity must also be active.
///
/// @author TG Team
///
public class EntityExistsValidator<T extends AbstractEntity<?>> implements IBeforeChangeEventHandler<T> {

    public static final String ERR_ENTITY_WAS_NOT_FOUND = "%s [%s] was not found.";
    public static final String ERR_WAS_NOT_FOUND = "%s was not found.";
    public static final String ERR_ENTITY_EXISTS_BUT_NOT_ACTIVE = "%s [%s] exists, but is not active.";
    public static final String ERR_DIRTY = "Dirty entity %s (%s) is not acceptable.";
    private static final String ERR_UNION_INVALID = "%s is invalid: %s";

    private final Class<T> type;
    private final ICompanionObjectFinder coFinder;

    protected EntityExistsValidator() {
        type = null;
        coFinder = null;
    }

    public EntityExistsValidator(final Class<T> type, final ICompanionObjectFinder coFinder) {
        this.type = type;
        this.coFinder = coFinder;
    }

    @SuppressWarnings("unchecked")
    private static <E extends AbstractEntity<?>> boolean skipNewEntities(
            final Class<? extends AbstractEntity<?>> entityType,
            final String propertyName,
            final E value,
            final IEntityDao<E> co)
    {
        final var annot = getPropertyAnnotation(SkipEntityExistsValidation.class, entityType, propertyName);
        if (annot != null && annot.skipNew()) {
            if (value instanceof AbstractUnionEntity union) {
                // Union instance must be instrumented to use `activeProperyName()`.
                // TODO Instrumentation will no longer be necessary after tg/2466 is implemented.
                final var instrumentedUnion = instrument(union, (IEntityDao<AbstractUnionEntity>) co);
                final var memberAnnot = getPropertyAnnotation(SkipEntityExistsValidation.class, union.getType(), instrumentedUnion.activePropertyName());
                return memberAnnot != null && memberAnnot.skipNew();
            }
            else return true;
        }
        else return false;
    }

    private static String entityTitle(final AbstractEntity<?> entity) {
        return getEntityTitleAndDesc(entity.getType()).getKey();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Result handle(final MetaProperty<T> property, final T newValue, final Set<Annotation> mutatorAnnotations) {
        final AbstractEntity<?> entity = property.getEntity();

        // It does not make sense to validate properties of union entities.
        // If an entity has a union-typed property, this validator will use the union's active property value.
        if (entity instanceof AbstractUnionEntity) {
            return successful();
        }

        final IEntityDao<T> co = coFinder.find(type);
        final boolean isPropertyDescriptor;
        if (co == null) {
            isPropertyDescriptor = isPropertyDescriptor(type);
            if (!isPropertyDescriptor) {
                throw new IllegalStateException("EntityExistsValidator is not fully initialised: companion object is missing");
            }
        } else {
            isPropertyDescriptor = false;
        }

        try {
            if (newValue == null) {
                return successful(entity);
            }
            else if (newValue instanceof AbstractUnionEntity union) {
                // Union instance must be instrumented for validation.
                // TODO It may be unnecessary to check union entities for validity.
                //      It is likely that this approach was used only to detect violations of this validator by union members
                //      when this validator was applicable to properties of union entity types.
                final var isValid = instrument(union, (IEntityDao<AbstractUnionEntity>) co).isValid();
                if (!isValid.isSuccessful()) {
                    return failure(entity, new Exception(format(ERR_UNION_INVALID, entityTitle(newValue), isValid.getEx().getMessage()), isValid.getEx()));
                }

                return handle_(property, newValue, entity, co, isPropertyDescriptor);
            }
            else {
                return handle_(property, newValue, entity, co, isPropertyDescriptor);
            }
        } catch (final Exception e) {
            return failure(entity, e);
        }
    }

    private Result handle_(
            final MetaProperty<T> property,
            final T newValue,
            final AbstractEntity<?> entity,
            final IEntityDao<T> co,
            final boolean isPropertyDescriptor)
    {
        return handle_(property, newValue, extractEntity(newValue), entity, co, isPropertyDescriptor);
    }

    /// @param  flatNewValue  flattened `newValue` -- if `newValue` is a union, then this is the result of [AbstractUnionEntity#activeEntity()]
    ///
    @SuppressWarnings("unchecked")
    private <E extends AbstractEntity<?>> Result handle_(
            final MetaProperty<T> property,
            final T newValue,
            final E flatNewValue,
            final AbstractEntity<?> entity,
            final IEntityDao<T> co,
            final boolean isPropertyDescriptor)
    {
        final IEntityDao<E> flatCo = (IEntityDao<E>) (flatNewValue == newValue ? co : coFinder.find(flatNewValue.getType()));

        // If an entity is uninstrumented, its dirty state is irrelevant and cannot be checked.
        if (flatNewValue.isInstrumented() && flatNewValue.isDirty()) {
            if (!flatNewValue.isPersisted() && skipNewEntities(entity.getType(), property.getName(), newValue, co)) {
                return successful(entity);
            }
            // let's differentiate between dirty and new instances
            return failure(entity, !flatNewValue.isPersisted() ? format(ERR_WAS_NOT_FOUND, entityTitle(flatNewValue)) : format(ERR_DIRTY, flatNewValue, entityTitle(flatNewValue)));
        }

        final boolean exists;
        final boolean activeEnough;
        final boolean isMockNotFoundValue = isMockNotFoundValue(newValue);
        if (isMockNotFoundValue) {
            exists = false;
            activeEnough = true;
        }
        else if (!property.isActivatable()) {
            exists = isPropertyDescriptor || flatCo.entityExists(flatNewValue);
            activeEnough = true;
        }
        else {
            final var query = select(flatCo.getEntityType()).where().prop(ID).eq().val(flatNewValue).model();
            final var fetch = flatNewValue instanceof ActivatableAbstractEntity<?>
                    ? fetchOnly(flatCo.getEntityType()).with(ACTIVE)
                    : fetchOnly(flatCo.getEntityType());
            final var qem = from(query).with(fetch).lightweight().model();
            final E ent = flatCo.getEntity(qem);
            exists = ent != null;
            // If the enclosing entity is active, the referenced entity, if activatable, must also be active.
            activeEnough = !(entity instanceof ActivatableAbstractEntity<?> entityA)
                           || !entityA.isActive()
                           || !(ent instanceof ActivatableAbstractEntity<?> entA)
                           || entA.isActive();
        }

        if (!exists) {
            if (isMockNotFoundValue) {
                // TODO newValue.getDesc() is expected to contain a string value typed by a user, but this will change (refer, issue https://github.com/fieldenms/tg/issues/1933).
                // If a specific error message is present,then this error message is reported, otherwise a standard 'not found' message is reported.
                return failure(entity, getErrorMessage(newValue).orElseGet(() -> format(ERR_ENTITY_WAS_NOT_FOUND, entityTitle(newValue), newValue.getDesc())));
            }
            else return failure(entity,
                                isPropertyDescriptor || KEY_NOT_ASSIGNED.equals(flatNewValue.toString())
                                        ? format(ERR_WAS_NOT_FOUND, entityTitle(flatNewValue))
                                        : format(ERR_ENTITY_WAS_NOT_FOUND, entityTitle(flatNewValue), flatNewValue));
        }
        else if (!activeEnough) {
            return failure(entity, format(ERR_ENTITY_EXISTS_BUT_NOT_ACTIVE, entityTitle(flatNewValue), flatNewValue));
        }
        else return successful(entity);
    }

    private static @Nullable AbstractEntity<?> extractEntity(final AbstractEntity<?> entity) {
        return switch (entity) {
            case AbstractUnionEntity union -> union.activeEntity();
            case AbstractEntity<?> it -> it;
            case null -> null;
        };
    }

    private static <U extends AbstractUnionEntity> U instrument(final U union, final IEntityDao<U> co) {
        final U instrumentedUnion;
        if (union.isInstrumented()) {
            instrumentedUnion = union;
        }
        else {
            copy(union, instrumentedUnion = co.new_(), ID, VERSION);
        }
        return instrumentedUnion;
    }

}
