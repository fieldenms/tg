package ua.com.fielden.platform.web.view.master.api.actions.entity;

import ua.com.fielden.platform.entity.AbstractEntity;

public interface IEntityActionConfig7<T extends AbstractEntity<?>> extends IEntityActionConfig7AfterExecutionClose<T> {

    IEntityActionConfig7AfterExecutionClose<T> shortcut(final String shortcut);
}
