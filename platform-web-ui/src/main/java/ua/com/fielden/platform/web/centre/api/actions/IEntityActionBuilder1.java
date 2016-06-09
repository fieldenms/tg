package ua.com.fielden.platform.web.centre.api.actions;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.view.master.api.actions.pre.IPreAction;

public interface IEntityActionBuilder1<T extends AbstractEntity<?>> extends IEntityActionBuilder2<T> {
    IEntityActionBuilder2<T> preAction(final IPreAction preAction);
}