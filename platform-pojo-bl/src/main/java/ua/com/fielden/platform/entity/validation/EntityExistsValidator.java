package ua.com.fielden.platform.entity.validation;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.ActivatableAbstractEntity;
import ua.com.fielden.platform.entity.annotation.SkipEntityExistsValidation;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.reflection.ActivatableEntityRetrospectionHelper;

import java.lang.annotation.Annotation;
import java.util.Optional;
import java.util.Set;

import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY_NOT_ASSIGNED;
import static ua.com.fielden.platform.entity.ActivatableAbstractEntity.ACTIVE;
import static ua.com.fielden.platform.entity.proxy.MockNotFoundEntityMaker.getErrorMessage;
import static ua.com.fielden.platform.entity.proxy.MockNotFoundEntityMaker.isMockNotFoundValue;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.*;
import static ua.com.fielden.platform.error.Result.failure;
import static ua.com.fielden.platform.error.Result.successful;
import static ua.com.fielden.platform.reflection.ActivatableEntityRetrospectionHelper.isActivatableProperty;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getPropertyAnnotation;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitleAndDesc;
import static ua.com.fielden.platform.utils.EntityUtils.instrument;

/// This validator applies to entity-typed properties, ensuring that the new value is an entity that exists.
///
/// * If the validated property has [SkipEntityExistsValidation], this validator will be ignored.
/// * If the union-typed property has `@SkipEntityExistsValidation(skipNew = true)`, a union value with non-persisted
///   active entity will be considered valid iff the corresponding union member is also annotated so.
/// * The value must be persisted and non-dirty.
/// * If the property is union-typed, the new union value must be valid.
/// * If the property represents an activatable reference (either direct or via a union), and its enclosing entity is active,
///   the referenced entity must also be active.
///
@Singleton
public class EntityExistsValidator<T extends AbstractEntity<?>> implements IBeforeChangeEventHandler<T> {

    public static final String ERR_ENTITY_WAS_NOT_FOUND = "%s [%s] was not found.";
    public static final String ERR_WAS_NOT_FOUND = "%s was not found.";
    public static final String ERR_ENTITY_EXISTS_BUT_NOT_ACTIVE = "%s [%s] exists, but is not active.";
    public static final String ERR_DIRTY = "Dirty entity %s (%s) is not acceptable.";
    public static final String ERR_UNION_INVALID = "%s is invalid: %s";

    private final ICompanionObjectFinder coFinder;

    @Inject
    EntityExistsValidator(final ICompanionObjectFinder coFinder) {
        this.coFinder = coFinder;
    }

    @Override
    public Result handle(final MetaProperty<T> property, final T newValue, final Set<Annotation> mutatorAnnotations) {
        if (newValue == null) {
            return successful();
        }

        final AbstractEntity<?> entity = property.getEntity();

        if (isMockNotFoundValue(newValue)) {
            // TODO newValue.getDesc() is expected to contain a string newValue typed by a user, but this will change (refer, issue https://github.com/fieldenms/tg/issues/1933).
            // If a specific error message is present,then this error message is reported, otherwise a standard 'not found' message is reported.
            return failure(entity, getErrorMessage(newValue).orElseGet(() -> format(ERR_ENTITY_WAS_NOT_FOUND, entityTitle(newValue), newValue.getDesc())));
        }

        if (newValue instanceof AbstractUnionEntity newUnionValue) {
            return handleUnionEntityValue(entity, property.getName(), newUnionValue, newUnionValue.activeEntity());
        } else {
            return handleEntityValue(entity, property.getName(), newValue);
        }
    }

    /// Validates regular entity values (i.e. non-union).
    ///
    @SuppressWarnings("unchecked")
    private Result handleEntityValue(final AbstractEntity<?> entity, final CharSequence property, final AbstractEntity<?> value) {
        if (value instanceof PropertyDescriptor<?>) {
            return successful();
        }

        final IEntityDao valueCo = coFinder.find(value.getType());

        final var dirtyOrNewCheckResult = checkDirtyOrNew(entity, property, value);
        if (dirtyOrNewCheckResult.isPresent()) {
            return dirtyOrNewCheckResult.get();
        }

        final Optional<Result> existenceCheckResult = value instanceof ActivatableAbstractEntity<?> valueA && isActivatableProperty(entity.getType(), property)
                ? checkExistenceForActivatable(entity, valueA, valueCo)
                : checkExistenceWithoutActive(entity, value, valueCo);

        return existenceCheckResult.orElseGet(Result::successful);
    }

    /// Validates union-typed entity values.
    @SuppressWarnings("unchecked")
    private Result handleUnionEntityValue(
            final AbstractEntity<?> entity,
            final CharSequence property,
            final AbstractUnionEntity unionEntity,
            final AbstractEntity<?> unionMemberValue)
    {
        final var unionCo = (IEntityDao<AbstractUnionEntity>) coFinder.find(unionEntity.getType());

        // Union instance must be instrumented for validation.
        final var isValid = instrument(unionEntity, unionCo).isValid();
        if (!isValid.isSuccessful()) {
            return failure(entity, new Exception(ERR_UNION_INVALID.formatted(entityTitle(unionEntity), isValid.getEx().getMessage()), isValid.getEx()));
        }

        var dirtyOrNewCheckResult = checkDirtyOrNew(entity, property, unionMemberValue);
        if (dirtyOrNewCheckResult.isPresent()) {
            return dirtyOrNewCheckResult.get();
        }

        final IEntityDao unionMemberCo = coFinder.find(unionMemberValue.getType());
        final Optional<Result> existenceCheckResult = unionMemberValue instanceof ActivatableAbstractEntity<?> activatableUnionMemberValue && isActivatableUnionMember(entity, property, unionEntity)
                ? checkExistenceForActivatable(entity, activatableUnionMemberValue, unionMemberCo)
                : checkExistenceWithoutActive(entity, unionMemberValue, unionMemberCo);

        return existenceCheckResult.orElseGet(Result::successful);
    }

    private static boolean skipNewEntities(final Class<? extends AbstractEntity<?>> entityType, final CharSequence propertyName) {
        final var annot = getPropertyAnnotation(SkipEntityExistsValidation.class, entityType, propertyName.toString());
        return annot != null && annot.skipNew();
    }

    private static String entityTitle(final AbstractEntity<?> entity) {
        return getEntityTitleAndDesc(entity.getType()).getKey();
    }

    /// @param value  the entity whose dirty state and persistence status will be checked
    ///
    private Optional<Result> checkDirtyOrNew(
            final AbstractEntity<?> entity,
            final CharSequence property,
            final AbstractEntity<?> value)
    {
        // If an entity is uninstrumented, its dirty state is irrelevant and cannot be checked.
        if (value.isInstrumented() && value.isDirty()) {
            if (!value.isPersisted() && skipNewEntities(entity.getType(), property)) {
                return of(successful(entity));
            }
            // Let's differentiate between dirty and new instances.
            return of(failure(entity,
                              !value.isPersisted()
                              ? ERR_WAS_NOT_FOUND.formatted(entityTitle(value))
                              : ERR_DIRTY.formatted(value, entityTitle(value))));
        }
        else {
            return empty();
        }
    }

    /// A union member is activatable iff [ActivatableEntityRetrospectionHelper#isActivatableProperty(Class, CharSequence)] is true for:
    /// 1. Either the union-typed property, or
    /// 2. The union member property.
    ///
    /// In other words, to designate a union member property as non-activatable, both the union-typed property and the union member property
    /// must be designated as non-activatable.
    ///
    private static <U extends AbstractUnionEntity> boolean isActivatableUnionMember(
            final AbstractEntity<?> entity,
            final CharSequence property,
            final U union)
    {
        return isActivatableProperty(entity.getType(), property)
               || isActivatableProperty(union.getType(), union.activePropertyName());
    }

    private <V extends AbstractEntity<?>> Optional<Result> checkExistenceWithoutActive(
            final AbstractEntity<?> entity,
            final V value,
            final IEntityDao<V> valueCo)
    {
        return !valueCo.entityExists(value)
               ? of(makeNotExistsResult(entity, value))
               : empty();
    }

    private <V extends ActivatableAbstractEntity<?>> Optional<Result> checkExistenceForActivatable(
            final AbstractEntity<?> entity,
            final V value,
            final IEntityDao<V> valueCo)
    {
        // Activatability should be ignored if the enclosing entity is an inactive activatable or a union.
        // This special treatment of unions is for the sake of simplicity.
        // If we prohibited inactive entities for properties of unions, we would have to add the ability to ignore validation
        // errors of this nature when a union value is assigned to a union-typed property of an inactive entity
        // (to uphold the invariant that inactive entities can reference other inactive entities).
        if ((entity instanceof ActivatableAbstractEntity<?> it && !it.isActive()) || entity instanceof AbstractUnionEntity) {
            return checkExistenceWithoutActive(entity, value, valueCo);
        }
        else {
            final var qem = from(select(valueCo.getEntityType()).where().prop(ID).eq().val(value).model())
                            .with(fetchOnly(valueCo.getEntityType()).with(ACTIVE))
                            .lightweight()
                            .model();
            final var ent = valueCo.getEntity(qem);

            if (ent == null) {
                return of(makeNotExistsResult(entity, value));
            }
            else if (!ent.isActive()) {
                return of(failure(entity, ERR_ENTITY_EXISTS_BUT_NOT_ACTIVE.formatted(entityTitle(value), value)));
            }
            else {
                return empty();
            }
        }
    }

    private static Result makeNotExistsResult(final AbstractEntity<?> entity, final AbstractEntity<?> value) {
        return failure(entity,
                       KEY_NOT_ASSIGNED.equals(value.toString())
                       ? ERR_WAS_NOT_FOUND.formatted(entityTitle(value))
                       : ERR_ENTITY_WAS_NOT_FOUND.formatted(entityTitle(value), value));
    }

}
