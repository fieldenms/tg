package ua.com.fielden.platform.entity.validation;

import java.lang.annotation.Annotation;
import java.util.Set;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.error.Result;

/**
 * Validator that checks entity value for existence using an {@link IEntityDao} instance.
 *
 * IMPORTANT: value null is considered valid.
 *
 * @author 01es
 *
 */
public class EntityExistsValidator implements IBeforeChangeEventHandler {

    private final IEntityDao dao;

    public EntityExistsValidator(final IEntityDao dao) {
	this.dao = dao;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Result handle(final MetaProperty property, final Object newValue, final Object oldValue, final Set<Annotation> mutatorAnnotations) {
	final AbstractEntity<?> entity = property.getEntity();
	try {
	    if (newValue == null) {
		return new Result(entity, "EntityExists validator : Entity " + newValue + " is null.");
	    }

	    final boolean exists = newValue instanceof AbstractEntity ? dao.entityExists((AbstractEntity<?>) newValue) : dao.entityWithKeyExists(newValue);
	    if (!exists) {
		return new Result(entity, new Exception("EntityExists validator : Could not find entity " + newValue));
	    } else {
		return new Result(entity, "EntityExists validator : Entity " + newValue + " is valid.");
	    }
	} catch (final RuntimeException e) {
	    return new Result(entity, "EntityExists validator : Failed validation for property " + property.getName() + " on type " + entity.getType(), e);
	}
    }

}
