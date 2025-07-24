package ua.com.fielden.platform.entity.validation;

import static java.lang.String.format;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY_NOT_ASSIGNED;
import static ua.com.fielden.platform.entity.AbstractEntity.VERSION;
import static ua.com.fielden.platform.entity.ActivatableAbstractEntity.ACTIVE;
import static ua.com.fielden.platform.entity.proxy.MockNotFoundEntityMaker.getErrorMessage;
import static ua.com.fielden.platform.entity.proxy.MockNotFoundEntityMaker.isMockNotFoundValue;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchOnly;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.error.Result.failure;
import static ua.com.fielden.platform.error.Result.successful;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getAnnotation;
import static ua.com.fielden.platform.reflection.Finder.findFieldByName;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitleAndDesc;
import static ua.com.fielden.platform.utils.EntityUtils.copy;
import static ua.com.fielden.platform.utils.EntityUtils.isPropertyDescriptor;

import java.lang.annotation.Annotation;
import java.util.Set;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.ActivatableAbstractEntity;
import ua.com.fielden.platform.entity.annotation.SkipEntityExistsValidation;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.error.Result;

/**
 * Validator that checks entity value for existence using an {@link IEntityDao} instance.
 *
 * IMPORTANT: value null is considered valid.
 *
 * @author TG Team
 *
 */
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

    /** Indicates whether new entities should be skipped in this validator. It retrieves corresponding field and annotation to determine this information. */
    private static boolean skipNewEntities(final Class<? extends AbstractEntity<?>> entityType, final String propertyName) {
        final var seevAnnotation = getAnnotation(findFieldByName(entityType, propertyName), SkipEntityExistsValidation.class);
        return seevAnnotation != null && seevAnnotation.skipNew();
    }

    /** Retrieves entity title from non-empty {@code newValue}'s type. It retrieves corresponding type annotation to determine this information. */
    private String entityTitle(final T newValue) {
        return getEntityTitleAndDesc(newValue.getType()).getKey();
    }

    @Override
    public Result handle(final MetaProperty<T> property, final T newValue, final Set<Annotation> mutatorAnnotations) {
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

        final AbstractEntity<?> entity = property.getEntity();
        try {
            if (newValue == null) {
                return successful(entity);
            } else if (newValue instanceof AbstractUnionEntity) {
                // If union-typed property is marked with @SkipEntityExistsValidation, this validator will be completely bypassed.
                // If union-typed property is marked with @SkipEntityExistsValidation(skipNew = true), a union instance with non-persisted active entity will be skipped.
                //   However, a union entity must be valid and it is only possible if @SkipEntityExistsValidation[(skipNew = true)] is also present on specific union properties.
                // If union-typed property is marked with @SkipEntityExistsValidation(skipActiveOnly = true), full validation will be performed at this stage (see also https://github.com/fieldenms/tg/issues/1450).
                if (isMockNotFoundValue(newValue)) { // mock represents an invalid value
                    // if a specific error message is present,then this error message is reported
                    // otherwise, a standard 'not found' message is reported 
                    // FIXME newValue.getDesc() is expected to contain a string value typed by a user, but this will change (refer, issue https://github.com/fieldenms/tg/issues/1933).
                    return failure(entity, getErrorMessage(newValue).orElseGet(() -> format(ERR_ENTITY_WAS_NOT_FOUND, entityTitle(newValue), newValue.getDesc())));
                } else {
                    // need to create an instrumented copy of newValue, if it is uninstrumented, to enforce validation
                    final T newValueToCheck;
                    if (!newValue.isInstrumented()) {
                        copy(newValue, newValueToCheck = co.new_(), ID, VERSION);  // KEY and DESC are not considered as "real" props for union -- no need to skip them for the unistrumented instance
                    } else {
                        newValueToCheck = newValue;
                    }
                    final var isValid = newValueToCheck.isValid();
                    if (!isValid.isSuccessful()) {
                        return failure(entity, new Exception(format(ERR_UNION_INVALID, entityTitle(newValueToCheck), isValid.getEx().getMessage()), isValid.getEx()));
                    } else if (!newValueToCheck.isPersisted() && !skipNewEntities(entity.getType(), property.getName()) || newValueToCheck.isPersisted() && !co.entityExists(newValueToCheck)) {
                        return failure(format(ERR_ENTITY_WAS_NOT_FOUND, entityTitle(newValueToCheck), newValueToCheck.toString())); // key is present if isValid is successful; that's why newValue.toString() is safe
                    } else {
                        return successful(entity);
                    }
                }
            } else if (newValue.isInstrumented() && newValue.isDirty()) { // if entity uninstrumented its dirty state is irrelevant and cannot be checked
                if (!newValue.isPersisted() && skipNewEntities(entity.getType(), property.getName())) { // isPersisted check is anticipated to be much lighter than skipNewEntities (getting field and annotation)
                    return successful(entity);
                }
                // let's differentiate between dirty and new instances
                return failure(entity, !newValue.isPersisted() ? format(ERR_WAS_NOT_FOUND, entityTitle(newValue)) : format(ERR_DIRTY, newValue, entityTitle(newValue)));
            }

            // the notion of existence is different for activatable and non-activatable entities,
            // where for activatable entities to exist means also to be active
            final boolean exists;
            final boolean activeEnough; // Does not have to be 100% active - see below
            final boolean isMockNotFoundValue = isMockNotFoundValue(newValue);
            if (isMockNotFoundValue) {
                exists = false;
                activeEnough = true;
            }
            // TODO Union-typed properties are considered activatable
            else if (!property.isActivatable()) { // is property value represents non-activatable?
                exists = isPropertyDescriptor || co.entityExists(newValue);
                activeEnough = true;
            } else { // otherwise, property value is activatable
                final Class<T> entityType = co.getEntityType();
                final EntityResultQueryModel<T> query = select(entityType).where().prop("id").eq().val(newValue.getId()).model();
                final fetch<T> fm = fetchOnly(entityType).with(ACTIVE);
                final QueryExecutionModel<T, EntityResultQueryModel<T>> qem = from(query).with(fm).lightweight().model();
                final T ent = co.getEntity(qem);
                exists = (ent != null);
                // two possible cases:
                // 1. if the property owning entity is itself an inactive activatable then the inactive property value is appropriate
                // 2. otherwise, the activatable value should also be active
                if ((entity instanceof ActivatableAbstractEntity) && !entity.<Boolean>get(ACTIVE)) {
                    activeEnough = true;
                } else {
                    activeEnough = ((ent != null) && ent.<Boolean> get(ACTIVE));
                }
            }

            if (!exists || !activeEnough) {
                if (!exists) {
                    if (isMockNotFoundValue) {
                        // using newValue.getDesc() depends on the fact the it contains the value typed by the user
                        return failure(entity, format(ERR_ENTITY_WAS_NOT_FOUND, entityTitle(newValue), newValue.getDesc()));
                    }
                    return failure(entity, isPropertyDescriptor || KEY_NOT_ASSIGNED.equals(newValue.toString()) ? format(ERR_WAS_NOT_FOUND, entityTitle(newValue)) : format(ERR_ENTITY_WAS_NOT_FOUND, entityTitle(newValue), newValue.toString()));
                } else {
                    return failure(entity, format(ERR_ENTITY_EXISTS_BUT_NOT_ACTIVE, entityTitle(newValue), newValue.toString()));
                }
            } else {
                return successful(entity);
            }
        } catch (final Exception e) {
            return failure(entity, e);
        }
    }
}
