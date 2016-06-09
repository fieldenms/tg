package ua.com.fielden.platform.sample.domain.validators;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;

import java.lang.annotation.Annotation;
import java.util.Set;

import com.google.inject.Inject;

import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.validation.IBeforeChangeEventHandler;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.sample.domain.ITgPersistentEntityWithProperties;
import ua.com.fielden.platform.sample.domain.TgPersistentEntityWithProperties;

public class EntityValidator implements IBeforeChangeEventHandler<TgPersistentEntityWithProperties> {

    private final ITgPersistentEntityWithProperties co;
    
    @Inject
    public EntityValidator(final ITgPersistentEntityWithProperties co) {
        super();
        this.co = co;
    }

    @Override
    public Result handle(final MetaProperty<TgPersistentEntityWithProperties> property, final TgPersistentEntityWithProperties newValue, final TgPersistentEntityWithProperties oldValue, final Set<Annotation> mutatorAnnotations) {
        if (newValue != null) {
            final TgPersistentEntityWithProperties val = co.findById(newValue.getId(), fetchAll(TgPersistentEntityWithProperties.class));
            if (val.getRequiredValidatedProp() != null && val.getRequiredValidatedProp() < 5) {
                return Result.failure(newValue, "Unacceptable entity due to requiredValidatedProp < 5.");
            }
        }
        return Result.successful(newValue);
    }

}