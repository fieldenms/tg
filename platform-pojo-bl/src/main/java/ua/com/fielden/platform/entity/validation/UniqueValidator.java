package ua.com.fielden.platform.entity.validation;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.exceptions.EntityException;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.meta.impl.AbstractBeforeChangeEventHandler;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;

import java.lang.annotation.Annotation;
import java.util.Set;

import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.error.Result.failuref;
import static ua.com.fielden.platform.error.Result.successful;

/**
 * A validator to check that a passed in value is unique for an entity/property combination.
 *
 * IMPORTANT: value {@code null} is always considered valid and permissible for multiple entity instances (i.e., {@code NULL = NULL} is {@code false}).
 *
 * @author TG Team
 *
 */
public class UniqueValidator<E extends AbstractEntity<?>, T> extends AbstractBeforeChangeEventHandler<T> {

    public static final String ERR_VALIDATION_ERROR_TEMPLATE = "Value [%s] must be unique for property [%s] in entity [%s].";

    @Override
    public Result handle(final MetaProperty<T> property, final T newValue, final Set<Annotation> mutatorAnnotations) {
        @SuppressWarnings("unchecked")
        final E entity = (E) property.getEntity();
        // let's for now consider that multiple NULL values are permitted for columns with unique index...
        // TODO may need to revisit the above premise
        if (newValue == null) {
            return successful();
        }

        @SuppressWarnings("unchecked")
        final Class<E> type = (Class<E>) entity.getType();
        final IEntityDao<E> co = co(type);
        if (co == null) {
            throw new EntityException("Companion object is missing for entity [%s].".formatted(type.getName()));
        }

        final EntityResultQueryModel<E> query = entity.isPersisted() 
                ? select(type).where().prop(ID).ne().val(entity.getId()).and().prop(property.getName()).eq().val(newValue).model()
                : select(type).where().prop(property.getName()).eq().val(newValue).model();

        if (co.exists(query)) {
            final var entityTitle = TitlesDescsGetter.getEntityTitleAndDesc(type).getKey();
            final var propTitle = TitlesDescsGetter.getTitleAndDesc(property.getName(), type).getKey();
            return failuref(ERR_VALIDATION_ERROR_TEMPLATE, newValue, propTitle, entityTitle);
        }

        return successful();
    }

}
