package ua.com.fielden.platform.web.master.api.actions.entity;

import ua.com.fielden.platform.entity.AbstractEntity;

public interface IEntityActionConfig5<T extends AbstractEntity<?>> extends IEntityActionConfig6<T> {
    IEntityActionConfig6<T> shortDesc(final String shortDesc);
}
