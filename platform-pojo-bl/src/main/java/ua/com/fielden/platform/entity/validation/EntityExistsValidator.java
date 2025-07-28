package ua.com.fielden.platform.entity.validation;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.ActivatableAbstractEntity;
import ua.com.fielden.platform.entity.EntityReferenceAlgebra;
import ua.com.fielden.platform.entity.annotation.SkipEntityExistsValidation;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
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
@Singleton
public class EntityExistsValidator<T extends AbstractEntity<?>> implements IBeforeChangeEventHandler<T> {

    public static final String ERR_ENTITY_WAS_NOT_FOUND = "%s [%s] was not found.";
    public static final String ERR_WAS_NOT_FOUND = "%s was not found.";
    public static final String ERR_ENTITY_EXISTS_BUT_NOT_ACTIVE = "%s [%s] exists, but is not active.";
    public static final String ERR_DIRTY = "Dirty entity %s (%s) is not acceptable.";
    private static final String ERR_UNION_INVALID = "%s is invalid: %s";

    private final ICompanionObjectFinder coFinder;
    private final EntityReferenceAlgebra entityReferenceAlgebra;

    @Inject
    EntityExistsValidator(final ICompanionObjectFinder coFinder,
                          final EntityReferenceAlgebra entityReferenceAlgebra)
    {
        this.coFinder = coFinder;
        this.entityReferenceAlgebra = entityReferenceAlgebra;
    }

    @SuppressWarnings("unchecked")
    private static <V extends AbstractEntity<?>> boolean skipNewEntities(
            final Class<? extends AbstractEntity<?>> entityType,
            final CharSequence propertyName,
            final V value,
            final IEntityDao<V> valueCo)
    {
        final var annot = getPropertyAnnotation(SkipEntityExistsValidation.class, entityType, propertyName.toString());
        if (annot != null && annot.skipNew()) {
            if (value instanceof AbstractUnionEntity union) {
                // Union instance must be instrumented to use `activeProperyName()`.
                // TODO Instrumentation will no longer be necessary after tg/2466 is implemented.
                final var instrumentedUnion = instrument(union, (IEntityDao<AbstractUnionEntity>) valueCo);
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

    @Override
    public Result handle(final MetaProperty<T> property, final T newValue, final Set<Annotation> mutatorAnnotations) {
        if (newValue == null) {
            return successful();
        }

        final AbstractEntity<?> entity = property.getEntity();

        // It does not make sense to validate properties of union entities.
        // If an entity has a union-typed property, this validator will use the union's active property value.
        if (entity instanceof AbstractUnionEntity) {
            return successful();
        }

        if (isMockNotFoundValue(newValue)) {
            // TODO newValue.getDesc() is expected to contain a string newValue typed by a user, but this will change (refer, issue https://github.com/fieldenms/tg/issues/1933).
            // If a specific error message is present,then this error message is reported, otherwise a standard 'not found' message is reported.
            return failure(entity, getErrorMessage(newValue).orElseGet(() -> format(ERR_ENTITY_WAS_NOT_FOUND, entityTitle(newValue), newValue.getDesc())));
        }

        return entityReferenceAlgebra.reference(entity, property.getName(), newValue, ops);
    }

    private final EntityReferenceAlgebra.Ops<Result> ops = new EntityReferenceAlgebra.Ops<>() {
        @SuppressWarnings("unchecked")
        @Override
        public Result apply(final AbstractEntity<?> entity, final CharSequence property, final AbstractEntity<?> value) {
            if (value instanceof PropertyDescriptor<?>) {
                return successful();
            }
            else {
                final var valueCo = (IEntityDao<AbstractEntity<?>>) coFinder.find(value.getType());

                var dirtyOrNewCheckResult = checkDirtyOrNew(entity, property, value, valueCo, value);
                if (dirtyOrNewCheckResult != null) {
                    return dirtyOrNewCheckResult;
                }

                final var existenceCheckResult = checkExistence(entity, property, value, valueCo);
                if (existenceCheckResult != null) {
                    return existenceCheckResult;
                }

                return successful();
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        public Result applyUnion(
                final AbstractEntity<?> entity,
                final CharSequence property, final AbstractUnionEntity union,
                final AbstractEntity<?> unionMember)
        {
            final var unionCo = (IEntityDao<AbstractUnionEntity>) coFinder.find(union.getType());

            // Union instance must be instrumented for validation.
            // TODO It may be unnecessary to check union entities for validity.
            //      It is likely that this approach was used only to detect violations of this validator by union members
            //      when this validator was applicable to properties of union entity types.
            final var isValid = instrument(union, unionCo).isValid();
            if (!isValid.isSuccessful()) {
                return failure(entity, new Exception(format(ERR_UNION_INVALID, entityTitle(union), isValid.getEx().getMessage()), isValid.getEx()));
            }

            final var unionMemberCo = (IEntityDao<AbstractEntity<?>>) coFinder.find(unionMember.getType());

            var dirtyOrNewCheckResult = checkDirtyOrNew(entity, property, union, unionCo, unionMember);
            if (dirtyOrNewCheckResult != null) {
                return dirtyOrNewCheckResult;
            }

            final var existenceCheckResult = checkExistence(entity, property, unionMember, unionMemberCo);
            return existenceCheckResult != null ? existenceCheckResult : successful();
        }
    };


    /// @param value  the value that is being assigned to the property
    /// @param flatValue  the entity whose dirty state and persistence status will be checked
    ///
    private <V extends AbstractEntity<?>> @Nullable Result checkDirtyOrNew(
            final AbstractEntity<?> entity,
            final CharSequence property,
            final V value,
            final IEntityDao<V> valueCo,
            final AbstractEntity<?> flatValue)
    {
        // If an entity is uninstrumented, its dirty state is irrelevant and cannot be checked.
        if (flatValue.isInstrumented() && flatValue.isDirty()) {
            if (!flatValue.isPersisted() && skipNewEntities(entity.getType(), property, value, valueCo)) {
                return successful(entity);
            }
            // let's differentiate between dirty and new instances
            return failure(entity,
                           !flatValue.isPersisted()
                                   ? format(ERR_WAS_NOT_FOUND, entityTitle(flatValue))
                                   : format(ERR_DIRTY, flatValue, entityTitle(flatValue)));
        }
        else return null;
    }

    private <V extends AbstractEntity<?>> @Nullable Result checkExistence(
            final AbstractEntity<?> entity,
            final CharSequence property,
            final V value,
            final IEntityDao<V> valueCo)
    {
        return entity instanceof ActivatableAbstractEntity<?> entityA
               && value instanceof ActivatableAbstractEntity<?> valueA
               && entity.getProperty(property.toString()).isActivatable()
                ? checkExistenceForActivatable(entityA, valueA, (IEntityDao<ActivatableAbstractEntity<?>>) valueCo)
                : checkExistenceWithoutActive(entity, value, valueCo);
    }

    private <V extends AbstractEntity<?>> @Nullable Result checkExistenceWithoutActive(
            final AbstractEntity<?> entity,
            final V value,
            final IEntityDao<V> valueCo)
    {
        if (!valueCo.entityExists(value)) {
            return makeNotExistsResult(entity, value);
        }
        else return null;
    }

    private <V extends ActivatableAbstractEntity<?>> @Nullable Result checkExistenceForActivatable(
            final ActivatableAbstractEntity<?> entity,
            final V value,
            final IEntityDao<V> valueCo)
    {
        // Activatability matters iff the enclosing entity is active.
        if (!entity.isActive()) {
            return checkExistenceWithoutActive(entity, value, valueCo);
        }
        else {
            final var qem = from(select(valueCo.getEntityType()).where().prop(ID).eq().val(value).model())
                    .with(fetchOnly(valueCo.getEntityType()).with(ACTIVE))
                    .lightweight()
                    .model();
            final var ent = valueCo.getEntity(qem);

            if (ent == null) {
                return makeNotExistsResult(entity, value);
            }
            else if (!ent.isActive()) {
                return failure(entity, format(ERR_ENTITY_EXISTS_BUT_NOT_ACTIVE, entityTitle(value), value));
            }
            else return null;
        }
    }

    private static Result makeNotExistsResult(final AbstractEntity<?> entity, final AbstractEntity<?> value) {
        return failure(entity,
                       KEY_NOT_ASSIGNED.equals(value.toString())
                               ? format(ERR_WAS_NOT_FOUND, entityTitle(value))
                               : format(ERR_ENTITY_WAS_NOT_FOUND, entityTitle(value), value));
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
