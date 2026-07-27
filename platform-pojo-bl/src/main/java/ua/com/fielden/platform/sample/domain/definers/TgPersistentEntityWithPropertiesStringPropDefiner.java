package ua.com.fielden.platform.sample.domain.definers;

import ua.com.fielden.platform.entity.meta.IAfterChangeEventHandler;
import ua.com.fielden.platform.entity.meta.MetaProperty;

public class TgPersistentEntityWithPropertiesStringPropDefiner implements IAfterChangeEventHandler<String> {
    @Override
    public void handle(final MetaProperty<String> property, final String entityPropertyValue) {
        if (!property.getEntity().isInitialising()) {
            if (entityPropertyValue. contains("10")) {
                property.getEntity().set("integerProp", 20);
            } else {
                property.getEntity().set("integerProp", 5);
            }
        }
    }
}
