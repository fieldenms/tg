package ua.com.fielden.platform.entity.validation;

import static java.lang.String.format;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.lang.annotation.Annotation;
import java.util.Set;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.exceptions.EntityException;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.error.Result;

/**
 * A validator to check that a passed in value is unique for an entity/property combination.
 *
 * IMPORTANT: value <code>null</code> is always considered valid and permissible for multiple entity instances (i.e. <code>NULL = NULL</code> is <code>false</code>).
 *
 * @author TG Team
 *
 */
public class UniqueValidator<E extends AbstractEntity<?>, T> implements IBeforeChangeEventHandler<T> {

    public static final String validationErrorTemplate = "Value [%s] must be unique for property [%s] in entity [%s].";
    
    private final ICompanionObjectFinder coFinder;

    @Inject
    public UniqueValidator(final ICompanionObjectFinder coFinder) {
        this.coFinder = coFinder;
    }

    @Override
    public Result handle(final MetaProperty<T> property, final T newValue, final T oldValue, final Set<Annotation> mutatorAnnotations) {
        @SuppressWarnings("unchecked")
        final E entity = (E) property.getEntity();
        // let's for now consider that multiple NULL values are permitted for columns with unique index...
        // TODO may need to revisit the above premise
        if (newValue == null) {
            return Result.successful(entity);
        }
        
        @SuppressWarnings("unchecked")
        final Class<E> type = (Class<E>) entity.getType();
        final IEntityDao<E> co = coFinder.find(type);
        if (co == null) {
            throw new EntityException(format("Companion object is missing for entity [%s].", type.getName()));
        }
        
        final EntityResultQueryModel<E> query = entity.isPersisted() 
                ? select(type).where().prop(ID).ne().val(entity.getId()).and().prop(property.getName()).eq().val(newValue).model()
                : select(type).where().prop(property.getName()).eq().val(newValue).model();

        int count = co.count(query);
        if (count > 0) {
            return Result.failure(format(validationErrorTemplate, newValue, property.getName(), type.getSimpleName()));
        }
        
        return Result.successful(entity);
    }

}
