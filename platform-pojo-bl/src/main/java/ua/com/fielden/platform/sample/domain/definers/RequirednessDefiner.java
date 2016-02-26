package ua.com.fielden.platform.sample.domain.definers;

import ua.com.fielden.platform.entity.meta.IAfterChangeEventHandler;
import ua.com.fielden.platform.entity.meta.MetaProperty;

public class RequirednessDefiner implements IAfterChangeEventHandler<Integer> {

    @Override
    public void handle(final MetaProperty<Integer> property, final Integer entityPropertyValue) {
        property.getEntity().getProperty("entityProp").setRequired(property.getValue() != null && property.getValue() > 100);

        property.getEntity().getProperty("entityProp").setEditable(property.getValue() == null || property.getValue() != 13);
    }

}
