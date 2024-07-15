package ua.com.fielden.platform.sample.domain.validators;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;
import static ua.com.fielden.platform.error.Result.informative;
import static ua.com.fielden.platform.error.Result.warning;
import static ua.com.fielden.platform.utils.EntityUtils.equalsEx;

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
        if (newValue != null && "X".equals(newValue.getStringProp())) {
            return informative("Validator: value with str prop 'X'.");
        }
        // Yet another common case is where warning needs to be shown during editing of value, and informative -- after successful save.
        // We need to check whether the value has been changed (the same value setting may be enforced, especially in Web UI).
        // If value is not changed -- show informative message.
        if (newValue != null && !newValue.getBooleanProp()) {
            final String msg = "Validator: value with bool prop 'false'.";
            return !equalsEx(newValue, property.getValue()) || !property.getEntity().isPersisted() ? warning(msg) : informative(msg);
        }
        return Result.successful(newValue);
    }

}