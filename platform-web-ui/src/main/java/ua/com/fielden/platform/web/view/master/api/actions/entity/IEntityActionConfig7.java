package ua.com.fielden.platform.web.view.master.api.actions.entity;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.view.master.api.actions.IEntityActionConfig;
import ua.com.fielden.platform.web.view.master.api.helpers.IActionBarLayoutConfig0;

public interface IEntityActionConfig7<T extends AbstractEntity<?>> extends IEntityActionConfig<T>, IActionBarLayoutConfig0<T> {
    IEntityActionConfig8<T> shortcut(final String shortcut);
}
