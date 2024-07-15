package ua.com.fielden.platform.web.view.master.api.actions.entity;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.view.master.api.actions.EnabledState;

public interface IEntityActionConfig0<T extends AbstractEntity<?>> extends IEntityActionConfig1<T> {
    IEntityActionConfig1<T> enabledWhen(final EnabledState state);
}
