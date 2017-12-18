package ua.com.fielden.platform.sample.domain.definers;

import static ua.com.fielden.platform.utils.EntityUtils.equalsEx;

import ua.com.fielden.platform.entity.meta.IAfterChangeEventHandler;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.sample.domain.TgEntityWithPropertyDependency;

/**
 * Handles propagation of v0 / v1 values further to property <code>prop1</code> in a form of val0 / val1.
 * 
 * @author TG Team
 *
 */
public class TgEntityWithPropertyDependencyPropXAndPropYDefiner implements IAfterChangeEventHandler<String> {

    @Override
    public void handle(final MetaProperty<String> property, final String newValue) {
        final TgEntityWithPropertyDependency entity = (TgEntityWithPropertyDependency) property.getEntity();
        final String otherPropName = "prop1";
        if (!property.getEntity().isInitialising()) {
            // to support proper data flow in all edge-cases we need to enforce data propagation by using setter enforcement
            if (equalsEx(newValue, "v0")) {
                entity.getProperty(otherPropName).setValue("val0", true);
            } else if (equalsEx(newValue, "v1")) {
                entity.getProperty(otherPropName).setValue("val1", true);
            }
        }
    }

}
