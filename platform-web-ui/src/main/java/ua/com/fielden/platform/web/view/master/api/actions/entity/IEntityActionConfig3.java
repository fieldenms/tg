package ua.com.fielden.platform.web.view.master.api.actions.entity;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.view.master.api.actions.EnabledState;

public interface IEntityActionConfig3<T extends AbstractEntity<?>> extends IEntityActionConfig4<T> {
    IEntityActionConfig4<T> enabledWhen(final EnabledState state);
}
