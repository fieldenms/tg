package ua.com.fielden.platform.serialisation.jackson.entities;

import ua.com.fielden.platform.entity.meta.IAfterChangeEventHandler;
import ua.com.fielden.platform.entity.meta.MetaProperty;

import com.google.inject.Inject;

public class PropDefiner implements IAfterChangeEventHandler<String> {

    @Inject
    public PropDefiner() {
    }

    @Override
    public void handle(final MetaProperty<String> property, final String entityPropertyValue) {
        final EntityWithDefiner entity = (EntityWithDefiner) property.getEntity();
        entity.setProp2(entityPropertyValue + "_defined");
    }

}
