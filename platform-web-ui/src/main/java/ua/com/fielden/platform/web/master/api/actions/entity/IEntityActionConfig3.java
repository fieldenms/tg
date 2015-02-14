package ua.com.fielden.platform.web.master.api.actions.entity;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.master.api.actions.EnabledState;

public interface IEntityActionConfig3<T extends AbstractEntity<?>> {
    IEntityActionConfig4<T> enabledWhen(final EnabledState state);
}
