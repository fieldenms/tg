package ua.com.fielden.platform.web.centre.api.actions;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.view.master.api.actions.post.IPostAction;

public interface IEntityActionBuilder2<T extends AbstractEntity<?>> extends IEntityActionBuilder4<T> {
    IEntityActionBuilder3<T> postActionSuccess(final IPostAction postAction);
    IEntityActionBuilder4<T> postActionError(final IPostAction postAction);
}