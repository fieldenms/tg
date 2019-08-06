package ua.com.fielden.platform.web.view.master.api.actions.entity;

import ua.com.fielden.platform.entity.AbstractEntity;

public interface IEntityActionConfig1<T extends AbstractEntity<?>> extends IEntityActionConfig2<T> {
    IEntityActionConfig2<T> icon(final String iconName);
}
