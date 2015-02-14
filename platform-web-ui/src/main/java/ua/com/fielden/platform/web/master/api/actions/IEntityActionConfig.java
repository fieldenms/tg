package ua.com.fielden.platform.web.master.api.actions;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.master.api.actions.entity.IEntityActionConfig0;

public interface IEntityActionConfig<T extends AbstractEntity<?>> {
    IEntityActionConfig0<T> addAction(final Class<? extends AbstractEntity<?>> functionalEntity);
}
