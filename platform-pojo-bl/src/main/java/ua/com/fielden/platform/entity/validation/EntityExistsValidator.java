package ua.com.fielden.platform.entity.validation;

import static java.lang.String.format;
import static ua.com.fielden.platform.entity.ActivatableAbstractEntity.ACTIVE;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchOnly;

import java.lang.annotation.Annotation;
import java.util.Set;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.ActivatableAbstractEntity;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.query.fluent.fetch;
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
    public Result handle(final MetaProperty<T> property, final T newValue, final T oldValue, final Set<Annotation> mutatorAnnotations) {
        final IEntityDao<T> co = coFinder.find(type);
        if (co == null) {
            throw new IllegalStateException("Entity exists validator is not fully initialise: companion object is missing");
        }

        final AbstractEntity<?> entity = property.getEntity();
        try {
            if (newValue == null) {
                return Result.successful(entity);
            } else if (newValue.isDirty()) {
                return Result.failure(entity, format("EntityExists validator: dirty entity %s (%s) is not acceptable.", newValue, newValue.getType().getName()));
            }

            // the notion of existence is different for activatable and non-activatable entities,
            // where for activatable entities to exists mens also to be active
            final boolean exists;
            if (!property.isActivatable()) { // is property value represents non-activatable?
                exists = co.entityExists(newValue);
            } else { // otherwise, property value is activatable
                final Class<T> entityType = co.getEntityType();
                final fetch<T> fm = fetchOnly(entityType).with(ACTIVE);
                final T ent = co.findByEntityAndFetch(fm, newValue);
                // two possible cases:
                // 1. if the property owning entity is itself an inactive activatable then the inactive property value is appropriate
                // 2. otherwise, the activatable value should also be active
                if (entity instanceof ActivatableAbstractEntity && !entity.<Boolean>get(ACTIVE)) {
                    exists = (ent != null);
                } else {
                    exists = (ent != null && ent.<Boolean>get(ACTIVE));
                }
            }

            if (!exists) {
                return Result.failure(entity, format("EntityExists validator: Could not find entity %s (%s)", newValue, newValue.getType().getName()));
            } else {
                return Result.successful(entity);
            }
        } catch (final Exception e) {
            return Result.failure(entity, e);
        }
    }

}
