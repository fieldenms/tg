package ua.com.fielden.platform.web.centre.api.actions;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.view.master.api.actions.post.IPostAction;

public interface IEntityActionBuilder3<T extends AbstractEntity<?>> extends IEntityActionBuilder4<T> {
    IEntityActionBuilder4<T> postActionError(final IPostAction preAction);
}