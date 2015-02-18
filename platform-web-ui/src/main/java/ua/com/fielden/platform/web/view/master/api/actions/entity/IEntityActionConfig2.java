package ua.com.fielden.platform.web.view.master.api.actions.entity;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.view.master.api.actions.post.IPostAction;

public interface IEntityActionConfig2<T extends AbstractEntity<?>> extends IEntityActionConfig3<T> {
    IEntityActionConfig3<T> postActionError(final IPostAction preAction);
}
