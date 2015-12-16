package ua.com.fielden.platform.sample.domain.definers;

import ua.com.fielden.platform.entity.meta.IAfterChangeEventHandler;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.sample.domain.TgEntityWithPropertyDependency;

public class PropDefiner implements IAfterChangeEventHandler<String> {

    @Override
    public void handle(final MetaProperty<String> property, final String newValue) {
        final String otherPropName = "dependentProp";
        final TgEntityWithPropertyDependency entity = (TgEntityWithPropertyDependency) property.getEntity();
        if (newValue.equals("IS")) {
            entity.set(otherPropName, "InService");
        }
    }

}
