package ua.com.fielden.platform.sample.domain;

import java.util.Set;

import ua.com.fielden.platform.entity.meta.IAfterChangeEventHandler;
import ua.com.fielden.platform.entity.meta.MetaProperty;

public class TgDefinersExecutorParentHandlerForCollection implements IAfterChangeEventHandler<Set<TgDefinersExecutorCollectionalChild>> {

    @Override
    public void handle(final MetaProperty<Set<TgDefinersExecutorCollectionalChild>> property, final Set<TgDefinersExecutorCollectionalChild> entityPropertyValue) {
        System.out.println("handle [" + property + "] in type [" + property.getEntity().getClass().getSimpleName() + "] for value " + entityPropertyValue);
    }

}
