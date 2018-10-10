package ua.com.fielden.platform.entity.validation.test_entities.definers;

import ua.com.fielden.platform.entity.meta.IAfterChangeEventHandler;
import ua.com.fielden.platform.entity.meta.MetaProperty;

public class MakeProp1NotRequired implements IAfterChangeEventHandler<Integer> {

    @Override
    public void handle(final MetaProperty<Integer> property, final Integer entityPropertyValue) {
         property.getEntity().getProperty("prop1").setRequired(false);
    }

}
