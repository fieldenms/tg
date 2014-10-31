package ua.com.fielden.platform.entity.validation;

import java.lang.annotation.Annotation;
import java.util.Set;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.ActivatableAbstractEntity;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.error.Result;

import com.google.inject.Inject;

/**
 * Validator that checks entity value for existence using an {@link IEntityDao} instance.
 *
 * IMPORTANT: value null is considered valid.
 *
 * @author TG Team
 *
 */
public class EntityExistsValidator<T extends AbstractEntity<?>> implements IBeforeChangeEventHandler<Object> {

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
    @Override
    public Result handle(final MetaProperty<Object> property, final Object newValue, final Object oldValue, final Set<Annotation> mutatorAnnotations) {
        final IEntityDao<T> co = coFinder.find(type);

        if (co == null) {
            throw new IllegalStateException("Entity exists validator is not fully initialise: companion object is missing");
        }
        final AbstractEntity<?> entity = property.getEntity();
        try {
            if (newValue == null) {
                return new Result(entity, "EntityExists validator : Entity " + newValue + " is null.");
            }

            // entity value should either be an actual entity instance or an instance of a corresponding key
            final boolean exists;
            if (newValue instanceof ActivatableAbstractEntity) {
                exists = co.entityExists((T) newValue);
            } else if (newValue instanceof AbstractEntity) {
                exists = co.entityExists((T) newValue);
            } else {
                exists = co.entityWithKeyExists(newValue);
            }

            if (!exists) {
                return new Result(entity, new Exception("EntityExists validator : Could not find entity " + newValue));
            } else {
                return new Result(entity, "EntityExists validator : Entity " + newValue + " is valid.");
            }
        } catch (final Exception e) {
            return new Result(entity, "EntityExists validator : Failed validation for property " + property.getName() + " on type " + entity.getType(), e);
        }
    }

}
