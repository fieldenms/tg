package ua.com.fielden.platform.entity.validation;

import static java.lang.String.*;
import java.lang.annotation.Annotation;
import java.util.Set;

import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.dao.IEntityAggregatesDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.validation.IBeforeChangeEventHandler;
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
    private final IEntityAggregatesDao coAggregates;
    private final IApplicationDomainProvider domainProvider;

    @Inject
    public ActivePropertyValidator(final IEntityAggregatesDao coAggregates, final IApplicationDomainProvider domainProvider) {
        this.coAggregates = coAggregates;
        this.domainProvider = domainProvider;
    }

    @Override
    public Result handle(final MetaProperty<Boolean> property, final Boolean newValue, final Boolean oldValue, final Set<Annotation> mutatorAnnotations) {
        final AbstractEntity<?> entity = property.getEntity();
        if (newValue || !entity.isPersisted()) {
            return Result.successful(newValue);
        } else {
            final long count = Validators.countActiveDependencies(domainProvider.entityTypes(), entity, coAggregates);

            if (count > 0) {
                final String entityTitle = TitlesDescsGetter.getEntityTitleAndDesc(entity.getType()).getKey();
                return Result.failure(count, format("Entity %s has active dependencies (%s).", entityTitle, count));
            } else {
                return Result.successful(newValue);
            }
        }
    }
}
