package ua.com.fielden.platform.entity.validation;

import static java.lang.String.format;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.*;

import java.lang.annotation.Annotation;
import java.util.Set;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.ActivatableAbstractEntity;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.OrderingModel;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.utils.Validators;

import com.google.inject.Inject;


/**
 * A validator for property <code>active</code> on class {@link ActivatableAbstractEntity} to prevent deactivation of entities with active dependencies.
 *
 * @author TG Team
 *
 */

public class ActivePropertyValidator implements IBeforeChangeEventHandler<Boolean> {
    private final ICompanionObjectFinder coFinder;

    @Inject
    public ActivePropertyValidator(final ICompanionObjectFinder coFinder) {
        this.coFinder = coFinder;
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Result handle(final MetaProperty<Boolean> property, final Boolean newValue, final Boolean oldValue, final Set<Annotation> mutatorAnnotations) {
        final ActivatableAbstractEntity<?> entity = (ActivatableAbstractEntity<?>) property.getEntity();
        if (newValue || !entity.isPersisted()) {
            return Result.successful(newValue);
        } else {
            // let's check refCount... it could potentially be stale...
            final IEntityDao<?> co = coFinder.find(entity.getType());
            final long count;
            if (!co.isStale(entity.getId(), entity.getVersion())) {
                count = entity.getRefCount();
            } else {
                // need to retireve the latest refCount
                final fetch fetch = fetchOnly(entity.getType()).with("refCount");
                final ActivatableAbstractEntity<?> updatedEntity = (ActivatableAbstractEntity<?>) co.findById(entity.getId(), fetch);
                count = updatedEntity.getRefCount();
            }

            if (count == 0) {
                return Result.successful(newValue);
            } else {
                final String entityTitle = TitlesDescsGetter.getEntityTitleAndDesc(entity.getType()).getKey();
                return Result.failure(count, format("Entity %s has active dependencies (%s).", entityTitle, count));
            }
        }
    }
}
