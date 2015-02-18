package ua.com.fielden.platform.web.view.master.api.actions.property;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.view.master.api.actions.post.IPostAction;

public interface IPropertyActionConfig2<T extends AbstractEntity<?>> extends IPropertyActionConfig3<T> {
    IPropertyActionConfig3<T> postActionError(final IPostAction preAction);
}
