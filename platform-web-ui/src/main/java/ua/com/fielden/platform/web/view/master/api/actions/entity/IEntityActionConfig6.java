package ua.com.fielden.platform.web.view.master.api.actions.entity;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.view.master.api.actions.IEntityActionConfig;
import ua.com.fielden.platform.web.view.master.api.helpers.IActionBarLayoutConfig0;

public interface IEntityActionConfig6<T extends AbstractEntity<?>> extends IEntityActionConfig<T>, IActionBarLayoutConfig0<T> {
    IEntityActionConfig7<T> longDesc(final String longDesc);
}
