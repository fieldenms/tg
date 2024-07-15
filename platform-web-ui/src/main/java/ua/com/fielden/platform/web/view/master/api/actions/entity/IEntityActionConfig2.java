package ua.com.fielden.platform.web.view.master.api.actions.entity;

import ua.com.fielden.platform.entity.AbstractEntity;

public interface IEntityActionConfig2<T extends AbstractEntity<?>> extends IEntityActionConfig3<T> {
    IEntityActionConfig3<T> shortDesc(final String shortDesc);
}
