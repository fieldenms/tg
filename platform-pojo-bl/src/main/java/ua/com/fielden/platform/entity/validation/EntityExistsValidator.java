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
            }

            // entity value should either be an actual entity instance or an instance of a corresponding key
            final boolean exists;
            if (newValue instanceof ActivatableAbstractEntity) {
                // if entity is activatable then it should both exists and be active to pass validation
                final Class<T> entityType = co.getEntityType();
                final fetch<T> fm = fetchOnly(entityType).with(ACTIVE);
                final T ent = co.findByEntityAndFetch(fm, newValue);
                exists = (ent != null && ent.<Boolean>get(ACTIVE));
            } else {
                exists = co.entityExists(newValue);
            }

            if (!exists) {
                return Result.failure(entity, format("EntityExists validator: Could not find entity %s", newValue));
            } else {
                return Result.successful(entity);
            }
        } catch (final Exception e) {
            return Result.failure(entity, e);
        }
    }

}
