package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.meta.IAfterChangeEventHandler;
import ua.com.fielden.platform.entity.meta.MetaProperty;

public class TgDefinersExecutorParentHandler implements IAfterChangeEventHandler<String> {

    @Override
    public void handle(final MetaProperty<String> property, final String entityPropertyValue) {
        System.out.println("handle [" + property + "] in type [" + property.getEntity().getClass().getSimpleName() + "] for value " + entityPropertyValue);
        
        final TgDefinersExecutorParent parent = (TgDefinersExecutorParent) property.getEntity();
        final TgDefinersExecutorCompositeKeyMember grandParent = parent.getKeyMember1();
        grandParent.addHandledProperty("keyMember1", property.getName());
    }

}
