package ua.com.fielden.platform.web.master.api.actions.entity;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.master.api.actions.post.IPostAction;

public interface IEntityActionConfig1<T extends AbstractEntity<?>> extends IEntityActionConfig3<T> {
    IEntityActionConfig2<T> postActionSuccess(final IPostAction preAction);
    IEntityActionConfig3<T> postActionError(final IPostAction preAction);
}
