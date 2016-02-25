package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.meta.IAfterChangeEventHandler;
import ua.com.fielden.platform.entity.meta.MetaProperty;

public class TgDefinersExecutorCompositeKeyMemberHandler implements IAfterChangeEventHandler<String> {

    @Override
    public void handle(final MetaProperty<String> property, final String entityPropertyValue) {
        System.out.println("handle [" + property + "] in type [" + property.getEntity().getClass().getSimpleName() + "] for value " + entityPropertyValue);
        
        final TgDefinersExecutorCompositeKeyMember grandParent = (TgDefinersExecutorCompositeKeyMember) property.getEntity();
        grandParent.addHandledProperty("", property.getName());
    }

}
