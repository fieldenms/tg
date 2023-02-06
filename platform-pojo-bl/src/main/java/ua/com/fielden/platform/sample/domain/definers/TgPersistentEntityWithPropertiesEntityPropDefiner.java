package ua.com.fielden.platform.sample.domain.definers;

import static ua.com.fielden.platform.error.Result.informative;

import ua.com.fielden.platform.entity.meta.IAfterChangeEventHandler;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.sample.domain.TgPersistentEntityWithProperties;

public class TgPersistentEntityWithPropertiesEntityPropDefiner implements IAfterChangeEventHandler<TgPersistentEntityWithProperties> {

    @Override
    public void handle(final MetaProperty<TgPersistentEntityWithProperties> prop, final TgPersistentEntityWithProperties newValue) {
        // Short-lived informative messages should be defined as part of validator (very similar to warnings).
        //   Such messages would live up until the entity gets successfully saved.
        // Long-lived informative messages should be defined in validator and in definer with '.isInitialising()' condition. This is a canonical way.
        //   Such messages would live even after saving up until integrity constraint would change.
        //   Alternatively, informative messages can be defined in definer only and without '.isInitialising()' condition (i.e. for both initialising and mutation phase).
        if (prop.getEntity().isInitialising() && newValue != null && "X".equals(newValue.getStringProp())) {
            prop.setDomainValidationResult(informative("Definer: value with str prop 'X'."));
        }
        // Yet another common case is where warning needs to be shown during editing of value, and informative -- after successful save.
        // We need to check whether the value has been changed (the same value setting may be enforced, especially in Web UI).
        // If value is not changed -- show informative message.
        if (prop.getEntity().isInitialising() && newValue != null && !newValue.getBooleanProp()) {
            prop.setDomainValidationResult(informative("Definer: value with bool prop 'false'."));
        }
    }

}
