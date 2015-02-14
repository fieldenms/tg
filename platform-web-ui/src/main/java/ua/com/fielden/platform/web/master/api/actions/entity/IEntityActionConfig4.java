package ua.com.fielden.platform.web.master.api.actions.entity;

import ua.com.fielden.platform.entity.AbstractEntity;

public interface IEntityActionConfig4<T extends AbstractEntity<?>> extends IEntityActionConfig5<T> {
    IEntityActionConfig5<T> icon(final String iconName);
}
