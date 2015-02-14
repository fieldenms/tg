package ua.com.fielden.platform.web.master.api.actions.entity;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.master.api.actions.pre.IPreAction;

public interface IEntityActionConfig0<T extends AbstractEntity<?>> extends IEntityActionConfig1<T> {
    IEntityActionConfig1<T> preAction(final IPreAction preAction);
}
