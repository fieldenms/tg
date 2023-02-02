package ua.com.fielden.platform.sample.domain.validators;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;
import static ua.com.fielden.platform.error.Result.informative;

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
    public Result handle(final MetaProperty<TgPersistentEntityWithProperties> property, final TgPersistentEntityWithProperties newValue, final Set<Annotation> mutatorAnnotations) {
        if (newValue != null) {
            final TgPersistentEntityWithProperties val = co.findById(newValue.getId(), fetchAll(TgPersistentEntityWithProperties.class));
            if (val.getRequiredValidatedProp() != null && val.getRequiredValidatedProp() < 5) {
                return Result.failure(newValue, "Unacceptable entity due to requiredValidatedProp < 5.");
            }
        }
        // Short-lived informative messages should be defined as part of validator (very similar to warnings).
        //   Such messages would live up until the entity gets successfully saved.
        // Long-lived informative messages should be defined in validator and in definer with '.isInitialising()' condition. This is a canonical way.
        //   Such messages would live even after saving up until integrity constraint would change.
        //   Alternatively, informative messages can be defined in definer only and without '.isInitialising()' condition (i.e. for both initialising and mutation phase).
        if (newValue != null && !newValue.getBooleanProp()) {
            return informative("Validator: value with bool prop 'false'.");
        }
        return Result.successful(newValue);
    }

}