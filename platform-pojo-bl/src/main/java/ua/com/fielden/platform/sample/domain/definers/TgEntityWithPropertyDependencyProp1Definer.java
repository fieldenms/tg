package ua.com.fielden.platform.sample.domain.definers;

import static ua.com.fielden.platform.utils.EntityUtils.equalsEx;

import ua.com.fielden.platform.entity.meta.IAfterChangeEventHandler;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.sample.domain.TgEntityWithPropertyDependency;

/**
 * Handles propagation of val0 / val1 values further to property <code>prop2</code> in a form of value0 / value1.
 * 
 * @author TG Team
 *
 */
public class TgEntityWithPropertyDependencyProp1Definer implements IAfterChangeEventHandler<String> {

    @Override
    public void handle(final MetaProperty<String> property, final String newValue) {
        final TgEntityWithPropertyDependency entity = (TgEntityWithPropertyDependency) property.getEntity();
        final String otherPropName = "prop2";
        if (!property.getEntity().isInitialising()) {
            // to support proper data flow in all edge-cases we need to enforce data propagation by using setter enforcement
            if (equalsEx(newValue, "val0")) {
                entity.getProperty(otherPropName).setValue("value0", true);
            } else if (equalsEx(newValue, "val1")) {
                entity.getProperty(otherPropName).setValue("value1", true);
            }
        }
    }

}
