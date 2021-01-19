package ua.com.fielden.platform.entity.validation;

import static java.lang.String.format;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY_NOT_ASSIGNED;
import static ua.com.fielden.platform.entity.ActivatableAbstractEntity.ACTIVE;
import static ua.com.fielden.platform.entity.proxy.MockNotFoundEntityMaker.isMockNotFoundValue;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchOnly;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.error.Result.failure;
import static ua.com.fielden.platform.error.Result.successful;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getAnnotation;
import static ua.com.fielden.platform.reflection.Finder.findFieldByName;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitleAndDesc;
import static ua.com.fielden.platform.utils.EntityUtils.isPropertyDescriptor;

import java.lang.annotation.Annotation;
import java.util.Set;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.entity.AbstractEntity;
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

    public static final String WAS_NOT_FOUND_CONCRETE_ERR = "%s [%s] was not found.";
    public static final String ERR_WAS_NOT_FOUND = "%s was not found.";
    public static final String EXISTS_BUT_NOT_ACTIVE_ERR = "%s [%s] exists, but is not active.";
    public static final String ERR_DIRTY = "Dirty entity %s (%s) is not acceptable.";

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
            } else if (newValue.isInstrumented() && newValue.isDirty()) { // if entity uninstrumented its dirty state is irrelevant and cannot be checked
                final SkipEntityExistsValidation seevAnnotation =  getAnnotation(findFieldByName(entity.getType(), property.getName()), SkipEntityExistsValidation.class);
                if (seevAnnotation != null && seevAnnotation.skipNew() && !newValue.isPersisted()) {
                    return successful(entity);
                }
                final String entityTitle = getEntityTitleAndDesc(newValue.getType()).getKey();
                // let's differentiate between dirty and new instances
                return failure(entity, !newValue.isPersisted() ? format(ERR_WAS_NOT_FOUND, entityTitle) : format(ERR_DIRTY, newValue, entityTitle));
            }
            
            // the notion of existence is different for activatable and non-activatable entities,
            // where for activatable entities to exists mens also to be active
            final boolean exists;
            final boolean activeEnough; // Does not have to be 100% active - see below
            final boolean isMockNotFoundValue = isMockNotFoundValue(newValue);
            if (isMockNotFoundValue) {
                exists = false;
                activeEnough = true;
            } else if (!property.isActivatable()) { // is property value represents non-activatable?
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
                final String entityTitle = getEntityTitleAndDesc(newValue.getType()).getKey();
                if (!exists) {
                    if (isMockNotFoundValue) {
                        // using newValue.getDesc() depends on the fact the it contains the value typed by the user
                        return failure(entity, format(WAS_NOT_FOUND_CONCRETE_ERR, entityTitle, newValue.getDesc()));
                    }
                    return failure(entity, isPropertyDescriptor || isMockNotFoundValue || KEY_NOT_ASSIGNED.equals(newValue.toString()) ? format(ERR_WAS_NOT_FOUND, entityTitle) : format(WAS_NOT_FOUND_CONCRETE_ERR, entityTitle, newValue.toString()));
                } else {
                    return failure(entity, format(EXISTS_BUT_NOT_ACTIVE_ERR, entityTitle, newValue.toString()));
                }
            } else {
                return successful(entity);
            }
        } catch (final Exception e) {
            return failure(entity, e);
        }
    }
}
