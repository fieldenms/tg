package ua.com.fielden.platform.sample.domain.definers;

import ua.com.fielden.platform.entity.meta.IAfterChangeEventHandler;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.sample.domain.TgEntityWithPropertyDependency;
import ua.com.fielden.platform.utils.EntityUtils;

public class PropDefiner implements IAfterChangeEventHandler<String> {

    @Override
    public void handle(final MetaProperty<String> property, final String newValue) {
        final TgEntityWithPropertyDependency entity = (TgEntityWithPropertyDependency) property.getEntity();
        if (!entity.isPersisted()) {
            final String otherPropName = "dependentProp";
            if (EntityUtils.equalsEx(newValue, "IS")) {
                entity.set(otherPropName, "InService");
            }
        }
    }

}
