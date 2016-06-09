package ua.com.fielden.platform.sample.domain.definers;

import ua.com.fielden.platform.entity.meta.IAfterChangeEventHandler;
import ua.com.fielden.platform.entity.meta.MetaProperty;

public class RequirednessDefiner implements IAfterChangeEventHandler<Integer> {

    @Override
    public void handle(final MetaProperty<Integer> prop, final Integer entityPropertyValue) {
        prop.getEntity().getPropertyIfNotProxy("entityProp").ifPresent(
                mp -> { 
                    mp.setRequired(prop.getValue() != null && prop.getValue() > 100);
                    mp.setEditable(prop.getValue() == null || prop.getValue() != 13);
                });
    }

}
