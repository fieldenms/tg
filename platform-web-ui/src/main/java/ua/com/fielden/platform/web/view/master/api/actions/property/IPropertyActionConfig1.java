package ua.com.fielden.platform.web.view.master.api.actions.property;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.view.master.api.actions.post.IPostAction;

public interface IPropertyActionConfig1<T extends AbstractEntity<?>> extends IPropertyActionConfig3<T> {
    IPropertyActionConfig2<T> postActionSuccess(final IPostAction preAction);
    IPropertyActionConfig3<T> postActionError(final IPostAction preAction);
}
