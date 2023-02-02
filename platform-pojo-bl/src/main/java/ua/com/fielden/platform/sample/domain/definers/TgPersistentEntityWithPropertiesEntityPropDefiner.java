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
        if (prop.getEntity().isInitialising() && newValue != null && !newValue.getBooleanProp()) {
            prop.setDomainValidationResult(informative("Definer: value with bool prop 'false'."));
        }
    }

}
