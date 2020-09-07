package ua.com.fielden.platform.web.view.master.api.actions.entity;

import ua.com.fielden.platform.entity.AbstractEntity;

public interface IEntityActionConfig4<T extends AbstractEntity<?>> extends IEntityActionConfig4AfterExecutionClose<T> {

    IEntityActionConfig4AfterExecutionClose<T> shortcut(final String shortcut);
}
