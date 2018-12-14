package ua.com.fielden.platform.entity.validation;

import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY_NOT_ASSIGNED;
import static ua.com.fielden.platform.entity.ActivatableAbstractEntity.ACTIVE;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchOnly;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.error.Result.failure;
import static ua.com.fielden.platform.error.Result.successful;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getAnnotation;
import static ua.com.fielden.platform.reflection.Finder.findFieldByName;
import static ua.com.fielden.platform.utils.EntityUtils.isPropertyDescriptor;
import static ua.com.fielden.platform.web.utils.EntityResourceUtils.isMockNotFoundEntity;

import java.lang.annotation.Annotation;
import java.util.Optional;
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
import ua.com.fielden.platform.entity_centre.review.criteria.EntityQueryCriteria;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;

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
    private static final String WAS_NOT_FOUND_ERR = "%s was not found.";
    public static final String EXISTS_BUT_NOT_ACTIVE_ERR = "%s [%s] exists, but is not active.";

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
        final Optional<Boolean> isPropertyDescriptorOpt;
        if (co == null) {
            isPropertyDescriptorOpt = of(isPropertyDescriptor(type));
            if (!isPropertyDescriptorOpt.get()) {
                throw new IllegalStateException("EntityExistsValidator is not fully initialised: companion object is missing");
            }
        } else {
            isPropertyDescriptorOpt = empty();
        }
        final boolean isPropertyDescriptor = isPropertyDescriptorOpt.orElse(false);
        
        final AbstractEntity<?> entity = property.getEntity();
        try {
            if (newValue == null) {
                return successful(entity);
            } else if (newValue.isInstrumented() && newValue.isDirty()) { // if entity uninstrumented its dirty state is irrelevant and cannot be checked
                final SkipEntityExistsValidation seevAnnotation =  getAnnotation(findFieldByName(entity.getType(), property.getName()), SkipEntityExistsValidation.class);
                if (seevAnnotation != null && seevAnnotation.skipDirtyOnly()) {
                    return successful(entity);
                }
                final String entityTitle = TitlesDescsGetter.getEntityTitleAndDesc(newValue.getType()).getKey();
                return failure(entity, format("EntityExists validator: dirty entity %s (%s) is not acceptable.", newValue, entityTitle));
            }
            
            if (property.criteriaParent) {
                return successful(entity);
            }
            // the notion of existence is different for activatable and non-activatable entities,
            // where for activatable entities to exists mens also to be active
            final boolean exists;
            final boolean activeEnough; // Does not have to 100% active - see below
            if (!property.isActivatable()) { // is property value represents non-activatable?
                exists = isPropertyDescriptor && !isMockNotFoundEntity(newValue) || co.entityExists(newValue);
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
                final String entityTitle = TitlesDescsGetter.getEntityTitleAndDesc(newValue.getType()).getKey();
                if (!exists) {
                    return failure(entity, isPropertyDescriptor || KEY_NOT_ASSIGNED.equals(newValue.toString()) ? format(WAS_NOT_FOUND_ERR, entityTitle) : format(WAS_NOT_FOUND_CONCRETE_ERR, entityTitle, newValue.toString()));
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
